import java.util.Random;

/**
 * Created by lyc08 on 2016/11/11.
 */
public class Test {
    public static void main(String[] args){
        Random random = new Random();


        for(int i= 0; i < 20; i++){
            System.out.println(random.nextInt(10));
        }
    }
}
