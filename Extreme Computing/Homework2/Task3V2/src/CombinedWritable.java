import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by lyc08 on 2016/11/14.
 */
public class CombinedWritable implements WritableComparable<CombinedWritable> {
    private Text qusId;
    private IntWritable count;

    public CombinedWritable() {
        qusId = new Text();
        count = new IntWritable();
    }

    @Override
    public int compareTo(CombinedWritable o) {
        return 0;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        this.qusId.write(dataOutput);
        this.count.write(dataOutput);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.qusId.readFields(dataInput);
        this.count.readFields(dataInput);
    }

    @Override
    public String toString() {
        return "(" + qusId + "," + count + ")";
    }

    public Text getQusId() {
        return qusId;
    }

    public void setQusId(Text qusId) {
        this.qusId = qusId;
    }

    public void setCount(IntWritable count) {
        this.count = count;
    }

    public IntWritable getCount() {
        return count;
    }
}
