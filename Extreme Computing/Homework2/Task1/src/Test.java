/**
 * Created by lyc08 on 2016/11/1.
 */
public class Test {
    public static void main(String[] args){
        String lint = "hdfs://scutter01.inf.ed.ac.uk:8020/data/incredibly/long/path/d1.txt";
        int lastIndex = lint.lastIndexOf("/");

        System.out.println(lint.substring(lastIndex+1, lint.length()));
    }
}
