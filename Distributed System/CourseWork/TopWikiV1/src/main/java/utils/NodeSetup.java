package utils;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import parta.QueryPartA;
import parta.StreamPartA;
import partb.QueryPartB;
import partb.StreamPartB;
import partc.QueryPartC;
import partc.StreamPartC;

/**
 * Created by lyc08 on 2016/10/25.
 * Set up node
 */

public class NodeSetup {
    public static void main(String[] args) throws IgniteException{
        if(!(args.length == 2 || args.length == 4)) {
            System.out.println("Invaild command, please see readme.txt for information.");
            System.exit(-1);
        }

        Ignite ignite = Ignition.start();

        if("partA".equals(args[0])){
            // run partA
            if(args.length != 2){
                System.out.println("Usage: partA <log_filepath>");
                System.exit(-1);
            }

            StreamPartA streamPart = new StreamPartA(args[1]);
            QueryPartA queryPart = new QueryPartA("PartA");
            try {
                streamPart.start();
                queryPart.start();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        if("partB".equals(args[0])){
            if(args.length!=4){
                System.out.println("Usage: partB <log_filepath> <log_filepath> <log_filepath>");
                System.exit(-1);
            }

            StreamPartB streamPart1 = new StreamPartB(args[1]);
            StreamPartB streamPart2 = new StreamPartB(args[2]);
            StreamPartB streamPart3 = new StreamPartB(args[3]);
            QueryPartB queryPart = new QueryPartB("PartB");
            try {
                streamPart1.start();
                streamPart2.start();
                streamPart3.start();
                queryPart.start();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        if("partC".equals(args[0])){
            if(args.length!=4){
                System.out.println("Usage: partC <log_filepath> <log_filepath> <log_filepath>");
                System.exit(-1);
            }

            StreamPartC streamPart1 = new StreamPartC(args[1]);
            StreamPartC streamPart2 = new StreamPartC(args[2]);
            StreamPartC streamPart3 = new StreamPartC(args[3]);
            QueryPartC queryPart = new QueryPartC("PartC");
            try {
                streamPart1.start();
                streamPart2.start();
                streamPart3.start();
                queryPart.start();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
