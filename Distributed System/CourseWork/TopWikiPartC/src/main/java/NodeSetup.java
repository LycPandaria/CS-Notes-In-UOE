import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;

/**
 * Created by lyc08 on 2016/10/25.
 * Set up node
 */

public class NodeSetup {
    public static void main(String[] args) throws IgniteException{
        Ignition.start();
    }
}
