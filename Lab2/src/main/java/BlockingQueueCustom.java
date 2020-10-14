import com.sun.tools.javac.util.Pair;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/*         The blocking queue (FIFO)
              __________________________
          <---__|__|__|__|__|__|__|__|__ <---
             |                    |
           take()              put()
             |                    |
        Consumer              Producer

 */

public class BlockingQueueCustom<Element> {
    Queue<Pair<Element,String>> queue;
    Integer size; // max capacity
    ReentrantLock lock;
    /*
    Condition notEmpty;
    Condition notFull;
    */
    Condition singleCond;

    public BlockingQueueCustom(Integer size, ReentrantLock lock){
        this.queue = new LinkedList<>();
        this.size = size; // represents the maximum size
        this.lock = lock;
        /*
        notEmpty = lock.newCondition();
        notFull = lock.newCondition();*/
        singleCond = lock.newCondition();
    }

    public void putElement(Element element, String message){
        lock.lock();
        try {
            while (queue.size() == this.size){
                singleCond.await();
            }
            queue.add(new Pair<>(element, message));
            //notEmpty.signalAll()
            singleCond.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public Pair<Element, String> getElement(){
        lock.lock();
        try {
            while (queue.size() == 0){
                singleCond.await();
            }
            Pair<Element, String> received = queue.remove();
            // notFull.signalAll();
            singleCond.signalAll();
            return received;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            lock.unlock();
        }
    }
}
