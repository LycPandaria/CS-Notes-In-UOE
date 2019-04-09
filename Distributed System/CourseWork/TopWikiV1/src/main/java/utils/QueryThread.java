package utils;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by lyc08 on 2016/10/31.
 */
public class QueryThread extends Thread{

    public QueryThread() {

    }

    public static void printQueryResults(List<?> res, MyLogger log) {
        Long unixTime = System.currentTimeMillis();
        DecimalFormat dft = new DecimalFormat("0.00");
        if (res == null || res.isEmpty())
            System.out.println("Query result set is empty.");
        else {
            for (Object row : res) {
                if (row instanceof List) {
                    String line = unixTime + ":";

                    List<?> l = (List)row;

                    if(l.size() == 2){
                        line = line + l.get(1) + ":" + l.get(0);
                    }
                    else
                        continue;

                    System.out.println(line);
                    log.getLog().info(line);
                }
                else
                    System.out.println("  " + row);
            }
        }
    }
}
