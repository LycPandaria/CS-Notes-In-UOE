
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by lyc08 on 2016/10/6.
 */
public class test {
    public static void main(String args[]){
        String l1 = "bob had a little lamb and a small cat";
        String l2 = "bob had a little lamb and a small cat";
        String l3 = "alice had one tiger";
        String l4 = "mary had some small dogs and a rabbit";
        String l5 = "mary had some small dogs and a rabbit";
        String l6 = "bob had a little lamb and a small cat";

        int v = 0;
        List<Integer> list = Arrays.asList(new Integer[]{1});
        Iterator<Integer> lte = list.iterator();

        for(int i = 0; i < 1; i++)
            v=lte.next();
        if(lte.hasNext())
            System.out.println(lte.next());
        System.out.println(l1.hashCode());
        System.out.println(l2.hashCode());
        System.out.println(l3.hashCode());
        System.out.println(l4.hashCode());
        System.out.println(l5.hashCode());
        System.out.println(l6.hashCode());
    }
}
