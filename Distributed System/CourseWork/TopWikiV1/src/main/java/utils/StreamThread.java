package utils;

/**
 * Created by lyc08 on 2016/10/31.
 */
public class StreamThread extends Thread {

    // path to the input file
    public String path = new String();

        public StreamThread() {

    }

    public StreamThread(String path) {
        this.path = path;
    }

}
