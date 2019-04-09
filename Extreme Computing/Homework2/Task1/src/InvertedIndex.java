import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lyc08 on 2016/11/1.
 */
public class InvertedIndex {

    public static class InvertedIndexMapper extends Mapper<LongWritable, Text, CombinedWritable, CombinedValueWritable> {

        private CombinedWritable cw = new CombinedWritable();
        private Text term = new Text();
        private Text file = new Text();
        private IntWritable count = new IntWritable();
        private CombinedValueWritable cvw = new CombinedValueWritable();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            // use to store term and count
            Map<String, Integer> map = new HashMap<String, Integer>();
            // get file name
            InputSplit inputSplit = context.getInputSplit();
            String fileFullName = ((FileSplit) inputSplit).getPath().toString();

            String filename = fileFullName.substring(fileFullName.lastIndexOf("/")+1,
                    fileFullName.length());

            if(value.toString().isEmpty())
                return;

            String[] split = value.toString().trim().split("\\s+");
            for(String term:split){
                if(map.containsKey(term)){
                    int count = map.get(term);
                    map.put(term, ++count);
                } else
                    map.put(term, new Integer(1));
            }

            // loop map
            for(Map.Entry<String, Integer> entry: map.entrySet()){
                term.set(entry.getKey());
                file.set(filename);
                count.set(entry.getValue());
                cw.setTerm(term);
                cw.setFile(file);
                cvw.setFile(file);
                cvw.setCount(count);
                context.write(cw, cvw);
            }
        }
    }

    public static class InvertedIndexCombiner extends Reducer<CombinedWritable, CombinedValueWritable,
            CombinedWritable, CombinedValueWritable>{
        CombinedValueWritable cvw = new CombinedValueWritable();
        // combiner word count from same file
        @Override
        protected void reduce(CombinedWritable key, Iterable<CombinedValueWritable> values, Context context)
                throws IOException, InterruptedException {
            int count = 0;
            for(CombinedValueWritable value:values){
                count += value.count.get();
            }
            cvw.setFile(key.file);
            cvw.setCount(new IntWritable(count));
            context.write(key, cvw);
        }
    }

    /**
     * The partitioner will divide the output results from combiner to
     * different reduce with same key, we define that if the the term in two CombinedWritable varibles
     * are same, they will processed by same reduce
     */
    public static class InvertedIndexPartitioner extends Partitioner<CombinedWritable, CombinedValueWritable>{
        @Override
        public int getPartition(CombinedWritable combinedWritable, CombinedValueWritable intWritable, int i) {
            return Math.abs(combinedWritable.hashCode() % i);
        }
    }

    public static class InvertedIndexGroupingComparator extends WritableComparator{
        public InvertedIndexGroupingComparator() {
            super(CombinedWritable.class, true);
        }

        // This function will group the value based on term
        @Override
        public int compare(WritableComparable a, WritableComparable b) {
            CombinedWritable a1 = (CombinedWritable)a;
            CombinedWritable b1 = (CombinedWritable)b;
            return a1.term.compareTo(b1.term);
        }
    }

    public static class InvertedIndexReducer extends Reducer<CombinedWritable, CombinedValueWritable, Text, NullWritable>{
        private Text output = new Text();

        @Override
        protected void reduce(CombinedWritable key, Iterable<CombinedValueWritable> values, Context context)
                throws IOException, InterruptedException {
            boolean first = true;
            int count = 0;
            int fileCount = 0;
            String fileString  = "";    // record the file the term appears
            String prev = "";
            for(CombinedValueWritable cvw:values){
                // get current file name
                String current = cvw.file.toString();
                // if this file
                if(first) {
                    prev = current;
                    first = false;
                    count += cvw.count.get();
                    fileCount++;
                    continue;   // jump to another loop
                }
                if(current.equals(prev)){
                    // this is an same file record
                    count += cvw.count.get();
                }else {
                    // this is a new word, give to prev and and filecount++
                    // reset count
                    fileString = fileString + "(" + prev +"," + count + ")" + ",";
                    prev = current;
                    fileCount++;
                    count = 0;
                    count+=cvw.count.get();
                }
            }
            // add the last record
            fileString = fileString + "(" + prev +"," + count + ")";
            String line = key.term.toString() + " : " + fileCount + " : " + "{" + fileString + "}";
            output.set(line);
            context.write(output, NullWritable.get());
        }
    }

    public static void main(String[] args) throws Exception{

        if(args.length != 2){
            System.out.println("Usage: InvertedIndex <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "s1616245-Task1");

        // set job
        job.setJarByClass(InvertedIndex.class);

        // set input format
        job.setInputFormatClass(TextInputFormat.class);

        // set Reducer number
        //job.setNumReduceTasks(1);

        // set input and output paths
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // set mapper and reducer and combiner
        job.setMapperClass(InvertedIndexMapper.class);
        job.setCombinerClass(InvertedIndexCombiner.class);
        job.setReducerClass(InvertedIndexReducer.class);

        // set grouping comparator
        job.setGroupingComparatorClass(InvertedIndexGroupingComparator.class);
        // set partitioner
        job.setPartitionerClass(InvertedIndexPartitioner.class);

        // set Mapper output type
        job.setMapOutputKeyClass(CombinedWritable.class);
        job.setMapOutputValueClass(CombinedValueWritable.class);

        // set Reducer output type
        job.setOutputKeyClass(CombinedWritable.class);
        job.setOutputValueClass(NullWritable.class);

        System.exit(job.waitForCompletion(true)?0:1);
    }

    // two custom class
    public static class CombinedWritable implements WritableComparable<CombinedWritable>{
        private Text term;
        private Text file;

        public CombinedWritable() {
            term = new Text();
            file = new Text();
        }

        @Override
        public String toString() {
            return term + ":" + file;
        }

        @Override
        public void write(DataOutput dataOutput) throws IOException {
            this.term.write(dataOutput);
            this.file.write(dataOutput);
        }

        @Override
        public void readFields(DataInput dataInput) throws IOException {
            this.term.readFields(dataInput);
            this.file.readFields(dataInput);
        }

        @Override
        public int compareTo(CombinedWritable o) {
            if(term.compareTo(o.term) != 0)
                return term.compareTo(o.term);
            else if(file.compareTo(o.file) != 0)
                return file.compareTo(o.file);
            else
                return 0;
        }

        @Override
        public int hashCode() {
            return term.hashCode();
        }

        public void setTerm(Text term) {
            this.term = term;
        }

        public void setFile(Text file) {
            this.file = file;
        }
    }
    public static class CombinedValueWritable implements WritableComparable<CombinedValueWritable>{
        private Text file;
        private IntWritable count;

        public CombinedValueWritable() {
            file = new Text();
            count = new IntWritable();
        }

        @Override
        public String toString() {
            return file + ":" + count;
        }

        @Override
        public int compareTo(CombinedValueWritable o) {
            return 0;
        }

        @Override
        public void write(DataOutput dataOutput) throws IOException {
            this.file.write(dataOutput);
            this.count.write(dataOutput);
        }

        @Override
        public void readFields(DataInput dataInput) throws IOException {
            this.file.readFields(dataInput);
            this.count.readFields(dataInput);
        }

        public Text getFile() {
            return file;
        }

        public void setFile(Text file) {
            this.file = file;
        }

        public IntWritable getCount() {
            return count;
        }

        public void setCount(IntWritable count) {
            this.count = count;
        }
    }

}
