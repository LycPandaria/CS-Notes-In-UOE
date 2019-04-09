import java.util.ArrayList;

/**
 * Created by lyc08 on 2016/10/11.
 */
public class Test {
    public static void main(String[] args){
        String countString = " 7 5 3 1 2 3 ";
        ArrayList<Double> list = new ArrayList<Double>();
        String[] splits = countString.trim().split("\\s+");
        for(String s:splits)
            System.out.println(s);
        for(int i=0; i<splits.length; i++)
            list.add(Double.parseDouble(splits[i]));
        for(Double l:list)
            System.out.println(l);
    }


}
