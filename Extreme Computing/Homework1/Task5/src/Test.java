/**
 * Created by lyc08 on 2016/10/11.
 */
public class Test {
    public static void main(String[] args){
        String line = "mary had a \t2    ";
        String[] splits = line.split("\\s+");
        String l = splits[0] + " " + splits[1] + " " +splits[2];
        int i = Integer.parseInt(splits[3]);
        System.out.println(l);
        System.out.println(i);
    }
}
