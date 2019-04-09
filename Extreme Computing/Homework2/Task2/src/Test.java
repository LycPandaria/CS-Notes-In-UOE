

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

/**
 * Created by lyc08 on 2016/11/2.
 */
public class Test {
    public static void main(String[] args){
        /*
        String idString = parse(line, "Id");
        String countString = parse(line, "ViewCount");
        if(idString != null && countString != null){
            System.out.printf("%s : %s\n", idString, countString);

        } */

        Queue<CountIdPair> priorityQueue = new PriorityQueue<>(5, new CompareCount());
        Random rand = new Random();
        for(int i=0;i<5;i++){
            CountIdPair cp = new CountIdPair(rand.nextLong(), "haha");
            priorityQueue.offer(cp);
        }
        /*
        for(int i = 0; i< 5; i++){
            CountIdPair poll = priorityQueue.poll();
            System.out.println(poll.getCount() + ":" + poll.getId());
        }*/
        CountIdPair cp2 = new CountIdPair(50L, "hheh");
        if(cp2.compareTo(priorityQueue.peek()) >= 0 ){
            priorityQueue.poll();
            priorityQueue.add(cp2);
        }
        for(int i = 0; i< 5; i++){
            CountIdPair poll = priorityQueue.poll();
            System.out.println(poll.getCount() + ":" + poll.getId());
        }
        /*
        for(int i=0;i<7;i++){
            Integer in = integerPriorityQueue.poll();
            System.out.println("Processing Integer1:"+in);
        }

        // add
        int next = 50;
        if(next >= integerPriorityQueue.peek()){
            integerPriorityQueue.poll();
            integerPriorityQueue.add(next);
        }
        int next2 = 101;
        if(next >= integerPriorityQueue.peek()){
            integerPriorityQueue.poll();
            integerPriorityQueue.add(next2);
        }
        for(int i=0;i<7;i++){
            Integer in = integerPriorityQueue.poll();
            System.out.println("Processing Integer3:"+in);
        }*/







    }

    private static class CountIdPair implements Comparable<CountIdPair>{
        // view count and question id
        private Long count;
        private String id;

        public CountIdPair() {
            count = 0L;
            id = "";
        }

        public CountIdPair(Long count, String id) {
            this.count = count;
            this.id = id;
        }

        @Override
        public int compareTo(CountIdPair o) {
            return this.count.compareTo(o.getCount());
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    private static class CompareCount implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            // compare by view count
            CountIdPair cp1 = (CountIdPair)o1;
            CountIdPair cp2 = (CountIdPair)o2;

            return cp1.getCount().compareTo(cp2.getCount());
        }
    }
}
