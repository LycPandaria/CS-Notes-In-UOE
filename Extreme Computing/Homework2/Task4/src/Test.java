import java.util.HashMap;
import java.util.Map;

/**
 * Created by lyc08 on 2016/11/6.
 */
public class Test {
    public static class StringPair{
        private String acceptid = "";
        private String userid = "";
        private int state = 0;

        public StringPair(String acceptid, String userid) {
            this.acceptid = acceptid;
            this.userid = userid;
            this.state = 0;
        }

        public String getAcceptid() {
            return acceptid;
        }

        public void setAcceptid(String acceptid) {
            this.acceptid = acceptid;
        }

        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }
    }
    public static void main(String[] args) {
        /*
        StringPair stringPair1 = new StringPair("ans2","leo");
        StringPair stringPair2 = new StringPair("ans3","zaka");
        StringPair stringPair3 = new StringPair("ans4","cheny");

        Map<String, StringPair> map = new HashMap<>();
        map.put("ans2", stringPair1);
        map.put("ans3", stringPair2);
        map.put("ans4", stringPair3);

        System.out.println(map.containsKey("ans2"));
        */

        String l = "";
        System.out.println(l.isEmpty());
    }
}
