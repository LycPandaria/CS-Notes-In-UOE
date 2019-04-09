import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by lyc08 on 2016/11/13.
 */
public class LossyCounting {

    private static final double ERROR_RATE = 0.001; // 0.1%
    private static final double SUPPORT = 0.01;
    private static final int WINDOW_SIEZE = (int)(1/ERROR_RATE);
    private static Map<String, Entity> map = new HashMap<>();

    public static void main(String[] args) throws Exception{

        if(args.length != 1){
            System.out.println("Usage: LossyCounting <input_file>");
            System.exit(0);
        }

        int bcurrent = 1;   // starts from 1
        int currentLineNumber = 0;  // line starts from 0

        Path input = Paths.get(args[0]);
        File output = new File("AllOutput.txt");
        if(!output.exists())
            output.createNewFile();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(output));

        // stream input
        List<String> lines = Files.readAllLines(input);
        for(String line:lines){

            // current line number increase
            currentLineNumber++;

            // look up this line
            if(map.containsKey(line)){
                // add frequency by one
                Entity entity = map.get(line);
                int currentF = entity.getFrequency();
                entity.setFrequency(currentF+1);
                map.put(line, entity);
            } else {
                // if not, make a new entity with (line, f=1, mpe=bcurrent-1)
                Entity newEntity = new Entity(line, 1, bcurrent-1);
                map.put(line, newEntity);
            }

            // if reach the block boundary
            if(currentLineNumber % WINDOW_SIEZE == 0){
                bcurrent = (int)Math.ceil((currentLineNumber) / WINDOW_SIEZE);
                // delete all the entitywith f+maxPossibleError <= bcurrent

                Iterator<Map.Entry<String, Entity>> iterator = map.entrySet().iterator();
                while(iterator.hasNext()){
                    Map.Entry<String, Entity> entry = iterator.next();
                    Entity entity = entry.getValue();
                    if(entity.getFrequency() + entity.getMaxPosibleError() <= bcurrent)
                        iterator.remove();  // use iterator to delete it
                }
            }
        }

        // finally, output entities with f >= (s-e)*currentLineNumber
        int hold = (int) ((SUPPORT-ERROR_RATE)*currentLineNumber);
        for(Entity entity:map.values()){
            if(entity.getFrequency() >= hold) {
                bufferedWriter.write(entity.getLine());
                bufferedWriter.newLine();
            }
        }
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    public static class Entity {
        private String line;
        private int frequency;
        private int maxPosibleError;

        public Entity() {
            line="";
            frequency=0;
            maxPosibleError=0;
        }

        public Entity(String line, int maxPosibleError, int frequency) {
            this.line = line;
            this.maxPosibleError = maxPosibleError;
            this.frequency = frequency;
        }

        public String getLine() {
            return line;
        }

        public void setLine(String line) {
            this.line = line;
        }

        public int getFrequency() {
            return frequency;
        }

        public void setFrequency(int frequency) {
            this.frequency = frequency;
        }

        public int getMaxPosibleError() {
            return maxPosibleError;
        }

        public void setMaxPosibleError(int maxPosibleError) {
            this.maxPosibleError = maxPosibleError;
        }
    }
}
