import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;

/**
 * Created by lyc08 on 2016/10/24.
 */
public class NodeSetup {
    public static void main(String[] args) throws IgniteException{
        Ignition.start();
    }
}
