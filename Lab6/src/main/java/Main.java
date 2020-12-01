import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static Integer NO_THREADS = 3;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
       DirectedGraph g1 = new DirectedGraph(6);
       g1.addEdge(0,3);
       g1.addEdge(0,1);
       g1.addEdge(0,5);
       g1.addEdge(1,2);
       g1.addEdge(2,5);
       g1.addEdge(3,4);
       g1.addEdge(4,2);
       g1.addEdge(5,1);
       g1.addEdge(1,0);
       ExecutorService threadPool = Executors.newFixedThreadPool(NO_THREADS);
       Lock lock = new ReentrantLock();
       List<Integer> result = new ArrayList<>();
       HamiltonianOperations ho = new HamiltonianOperations(g1, lock, result, threadPool, 2);
//       ho.sequentialFindHamiltonianCycle();
       ho.threadPoolHasHamiltonianCycle();
       if(result.size() == 0)
          System.out.println("No such cycles found");
       else {
          System.out.println("Cycles:");
          System.out.println(result);
       }
       threadPool.shutdown();
       threadPool.awaitTermination(10, TimeUnit.SECONDS);
    }
}
