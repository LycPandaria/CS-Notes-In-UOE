/**
 * Created by lyc08 on 2016/10/8.
 */
public class Test {
    public static void main(String[] args){
        String line = "pen apple pen pine apple applepen";
        String[] splits = line.split(" ");
        String l = new String("");
        if(splits.length > 2){
            for(int i = 0; i < splits.length-2; i ++){
                l = new String("");
                l = splits[i] + " " +splits[i+1] + " " + splits[i+2];
                System.out.println(l);
            }
        }
        else if(splits.length == 2){
            l = l + splits[0] + " " + splits[1];
            System.out.println(l);
        }
        else if(splits.length == 1){
            l = l + splits[0];
            System.out.println(l);
        }

    }
}
