package example.helloworld;

import org.apache.ignite.*;
import org.apache.ignite.lang.IgniteRunnable;

public class HelloWorld {
    public static void main(String[] args) throws IgniteException {
        try
        {
            final Ignite ignite = Ignition.start("examples/config/example-ignite.xml");

            // Broadcast 'Hello World' on all the nodes in the cluster.
            IgniteCluster cluster = ignite.cluster();
            IgniteCompute compute = ignite.compute(cluster.forRemotes());
            compute.broadcast(new IgniteRunnable() {
                 public void run() {
                    // Print ID of the node on which this runnable is executing.
                    System.out.println(">>> Hello Node: " + ignite.cluster().localNode().id());
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}