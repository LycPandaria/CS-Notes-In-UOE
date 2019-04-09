/**
 * Created by lyc08 on 2016/11/16.
 */
public class Test {
    public static void main(String[] args){
        double lineN = 1000000;
        int tmp =  (int)Math.ceil(Math.log(lineN*10 / Math.log(2)) / Math.log(2)) -1;
        System.out.println(tmp);
    }
}
