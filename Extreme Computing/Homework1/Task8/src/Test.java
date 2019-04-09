/**
 * Created by lyc08 on 2016/10/13.
 */
public class Test {
    public static void main(String[] args){
        String line = "(BIO2,71)(SLIP,40)(AR,82)(DAPA,64)";
        line = line.trim().substring(1,line.length()-1);
        String[] splits = line.trim().split(",|\\)\\(+");
        //String mark = splits[0].substring(1, splits[0].length());
        for(int i = 1; i < splits.length / 2 + 1; i++){
            System.out.println(Long.parseLong(splits[i*2-1]));
        }
        //for(String s:splits)
        //    System.out.println(s);
    }
}
