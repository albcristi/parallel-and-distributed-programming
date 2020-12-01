import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

public class HamiltonianOperations {
    private static AtomicInteger activeThreads = new AtomicInteger(0);
    private static AtomicInteger noThreads = new AtomicInteger(Main.NO_THREADS-1);

    private DirectedGraph graph;
    private Integer startNode;
    private Lock lock;
    private List<Integer> result;
    private ExecutorService threadPool;


    public HamiltonianOperations(DirectedGraph g, Lock lock, List<Integer> result, ExecutorService threadPool, Integer snode){
        this.graph = g;
        this.lock = lock;
        this.result = result;
        this.startNode = snode;
        this.threadPool = threadPool;
    }

    public void setResult(Stack<Integer> path){
        lock.lock();
        if(result.size() != 0){
            lock.unlock();
            return;
        }
        path.add(startNode);
        this.result.addAll(path);
        lock.unlock();
    }

    public void  sequentialFindHamiltonianCycle(){
        sequentialFindHamiltonianCycleInternal(startNode, new Stack<>());
    }
    private Boolean sequentialFindHamiltonianCycleInternal(int node, Stack<Integer> path){
        path.push(node);
        if(path.size() == graph.getSize()) {
            if (graph.getNeighbours(node).contains(startNode)) {
                setResult(path);
                return true;
            }
            else {
                return false;
            }
        }
        for(Integer destination: graph.getNeighbours(node))
            if(!path.contains(destination)) {
                if (sequentialFindHamiltonianCycleInternal(destination, path)) {
                    return true;
                }
                else{
                    while (!path.peek().equals(node))
                        path.pop();
                }
            }
        return false;
    }


    public void threadPoolHasHamiltonianCycle() throws ExecutionException, InterruptedException {
        threadPoolHasHamiltonianCycleInternal(startNode, new Stack<>());
    }

    private Boolean threadPoolHasHamiltonianCycleInternal(int node, Stack<Integer> path) throws ExecutionException, InterruptedException {
        path.push(node);
        if(path.size() == graph.getSize()) {
            if (graph.getNeighbours(node).contains(startNode)) {
                setResult(path);
                return true;
            }
            else {
                return false;
            }
        }
        Integer index = 0;
        List<Future<Boolean>> results = new ArrayList<>();
        while (activeThreads.get()<noThreads.get() && index < graph.getNeighbours(node).size()){
            activeThreads.set(activeThreads.get()+1);
            Stack<Integer> copy = new Stack<>();
            copy.addAll(path);
            while (path.contains(graph.getNeighbours(node).get(index)))
                index++;
            Integer finalIndex = index;
            results.add(threadPool.submit(()->
                    threadPoolHasHamiltonianCycleInternal(graph.getNeighbours(node).get(finalIndex), copy)));
        }
        while (index < graph.getNeighbours(node).size()){
            Stack<Integer> copy = new Stack<>();
            copy.addAll(path);
            while (path.contains(graph.getNeighbours(node).get(index)))
                index++;
            if(sequentialFindHamiltonianCycleInternal(graph.getNeighbours(node).get(index), copy)) {
                return true;
            }
        }

        for(Future<Boolean> result: results) {
            if (result.get())
                return true;
            activeThreads.set(activeThreads.get()-1);
        }
        return false;
    }
}
