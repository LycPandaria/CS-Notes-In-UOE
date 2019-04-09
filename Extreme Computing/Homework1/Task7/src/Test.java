import java.util.ArrayList;

/**
 * Created by lyc08 on 2016/10/14.
 */
public class Test {
    public static void main(String[] args){
        ArrayList<String> strings = new ArrayList<String>();
        strings.add("(EXC,70)");
        strings.add("(HCI,90)");
        strings.add("(EXC,70)");
        strings.add("(HCI,90)");
        System.out.println(strings.toString());
    }
}
