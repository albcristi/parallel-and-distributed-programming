import com.sun.tools.javac.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static Integer QUEUE_MAX_SIZE=1;
    /*
    Create two threads, a producer and a consumer, with the producer feeding the consumer.

    Requirement: Compute the scalar product of two vectors.

    Create two threads. The first thread (producer) will compute the products of pairs of elements
     - one from each vector - and will feed the second thread. The second thread (consumer) will
     sum up the products computed by the first one. The two threads will behind synchronized with
     a condition variable and a mutex. The consumer will be cleared to use each product as soon
     as it is computed by the producer thread.

    What is the scalar product of two vectors?
    let v=(v1,v2,...,vn)
    and w=(w1,w2,...,wn)
    two vectors with n elements
        v*w = v1*w1+v2*w2+...+vn*wm
     Conditions: the vectors should have the same length

     Idea for implementation:
        - We need to use a FIFO in order to make possible the communication
        between the Producer and the Consumer ==> we will create a blocking
        queue for this. We will use mutexes (ReentrantLock) in order to
        protect the access of the shared data and condition variables in order
        to manage the case when the queue is full/empty
     */
    public static void main(String[] args) {
        try {
            runTest();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            Integer size;
            int[] v, w;
            System.out.println("Enter size of the vectors");
            size = Integer.parseInt(reader.readLine());
            if(size<1){
                System.out.println("Size should be a positive integer");
                return;
            }
            runTest();
            v = new int[size];
            w = new int[size];
            System.out.println("Elements of v");
            for(int i=0; i<size; i++){
                System.out.println("Enter element");
                v[i] = Integer.parseInt(reader.readLine());
            }
            System.out.println("Elements of w");
            for(int i=0; i<size; i++){
                System.out.println("Enter element");
                w[i] = Integer.parseInt(reader.readLine());
            }
            ReentrantLock lock = new ReentrantLock();
            BlockingQueueCustom<Integer> fifo =  new BlockingQueueCustom<>(Main.QUEUE_MAX_SIZE, lock);
            Runnable consumer = () ->{
                Integer sum=0;
                String message="not ended";
                while (!message.equals("last")){
                    Pair<Integer, String> received = fifo.getElement();
                    if(received != null){
                        sum += received.fst;
                        message = received.snd;
                    }
                }
                System.out.println("The scalar product is "+sum);
            };

            Runnable producer = () ->{
                for(int i=0;i<size; i++){
                    String msg = i+1==size? "last": "not last";
                    fifo.putElement(v[i]*w[i], msg);
                }
            };

            Thread consumerThread = new Thread(consumer);
            Thread producerThread = new Thread(producer);
            consumerThread.start();
            producerThread.start();
            consumerThread.join();
            producerThread.join();
            int sum =0;
            for(int i=0; i<size; i++)
                sum += v[i]*w[i];
            System.out.println("seq. result: "+sum);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }


    private static void runTest(){
        int[] a, b;
        a = new int[2000];
        b = new int[2000];
        for(int i=0; i<2000; i++){
            a[i] = new Random().nextInt(500);
            b[i] = new Random().nextInt(500);
        }
        ReentrantLock lock = new ReentrantLock();
        BlockingQueueCustom<Integer> fifo =  new BlockingQueueCustom<>(Main.QUEUE_MAX_SIZE, lock);
        Runnable consumer = () ->{
            Integer sum=0;
            String message="not ended";
            while (!message.equals("last")){
                Pair<Integer, String> received = fifo.getElement();
                if(received != null){
                    sum += received.fst;
                    message = received.snd;
                }
            }
            System.out.println("The scalar product is "+sum);
        };

        Runnable producer = () ->{
            for(int i=0;i<2000; i++){
                String msg = i+1==2000? "last": "not last";
                fifo.putElement(a[i]*b[i], msg);
            }
        };

        Thread consumerThread = new Thread(consumer);
        Thread producerThread = new Thread(producer);
        consumerThread.start();
        producerThread.start();
        try {
            consumerThread.join();
            producerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int s =0;
        for(int i=0; i<2000; i++)
            s += a[i]*b[i];
        System.out.println("Serial result: "+s);
        System.out.println("<<<< TEST HAS FINISHED >>>>");
    }
}
