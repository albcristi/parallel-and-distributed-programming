import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class PolynomialOperations {
    private static Integer MIN_SIZE = 15;

    public static Polynomial sequentialMultiplySequential(Polynomial p1, Polynomial p2){
        /*
        Simple multiplication of 2 polynomials.
        We parse each possible comb of two elements from the two
        polynomials multiply them and add them to the corresponding coefficient
        from the result polynomial (indexP1+indexP2)
         */
        Polynomial result = new Polynomial(p1.getDegree()+p2.getDegree());
        for(int i=0; i<= p1.getDegree(); i++)
            for(int j=0; j<= p2.getDegree(); j++) {
                Integer multiplication = p1.getCoefficients().get(i)*p2.getCoefficients().get(j);
                result.getCoefficients().set(i + j, result.getCoefficients().get(i + j)+multiplication);
            }
        return result;
    }

    public static Polynomial threadSimplePolyMultiplication(Polynomial p1, Polynomial p2,Integer noTasks) throws InterruptedException {
        Polynomial result = new Polynomial(p1.getDegree()+p2.getDegree());
        // split the number of computations we will make for each Task
        Integer noOperationsPerTask = result.getCoefficients().size()/noTasks;
        if(noOperationsPerTask == 0)
            noOperationsPerTask = 1;
        // create a thread pool
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(noTasks);
        // now we start submitting the tasks to the threadPool
        for(int startPoint=0; startPoint<result.getCoefficients().size(); startPoint+=noOperationsPerTask){
            Integer endPoint =  startPoint+noOperationsPerTask;
            int finalStartPoint = startPoint;
            threadPool.execute( () -> {
                for(int i = finalStartPoint; i<endPoint; i++){
                    if(i > result.getDegree())
                        return;
                    for(int val = 0; val<=i; val++)
                        if(val <= p1.getDegree() && (i-val) <= p2.getDegree()){
                            Integer multiplication = p1.getCoefficients().get(val)*p2.getCoefficients().get(i-val);
                            result.getCoefficients().set(i, result.getCoefficients().get(i)+multiplication);
                        }
                }
            });
        }
        threadPool.shutdown();
        threadPool.awaitTermination(50, TimeUnit.SECONDS);
        return result;
    }

    public static Polynomial sequentialKaratsuba(Polynomial p1, Polynomial p2){
        List<Integer> coefficients = sequentialKaratsubaInternal(p1.getCoefficients(), p2.getCoefficients());
        coefficients.remove(coefficients.size()-1);
        return new Polynomial(coefficients);
    }

    private static List<Integer> sequentialKaratsubaInternal(List<Integer> P, List<Integer> Q){
        List<Integer> coefficients = initializeList(P.size()*2);
        if(P.size()==1){
            coefficients.set(0,P.get(0)*Q.get(0));
            return coefficients;
        }
        //P1(X)* Q1(X)*X^2n + (P1(X)*Q2(X)+P2(X)*Q1(X))*X^n + P2(X)*Q2(X)
        //(P1(X)+P2(X)) * (Q1(X)+Q2(X)) - P1(X)* Q1(X) - P2(X)*Q2(X)
        Integer size = P.size()/2;
        List<Integer> p1 = initializeList(size);
        List<Integer> p2 = initializeList(size);
        List<Integer> q1 = initializeList(size);
        List<Integer> q2 = initializeList(size);
        List<Integer> p1PLUSp2 = initializeList(size);
        List<Integer> q1PLUSq2 = initializeList(size);

        for(int i=0; i<size; i++) {
            p2.set(i, P.get(i));
            q2.set(i, Q.get(i));
            p1.set(i, P.get(size + i));
            q1.set(i, Q.get(size + i));
            p1PLUSp2.set(i, p1.get(i) + p2.get(i)); // (P1(X)+P2(X))
            q1PLUSq2.set(i, q1.get(i) + q2.get(i)); // (Q1(X)+Q2(X))
        }

        //P2(X)*Q2(X)
        List<Integer> prod2 = sequentialKaratsubaInternal(p2, q2);
        // P1(X)* Q1(X)
        List<Integer> prod1 = sequentialKaratsubaInternal(p1, q1);
        // (P1(X)+P2(X)) * (Q1(X)+Q2(X))
        List<Integer> prod12 = sequentialKaratsubaInternal(p1PLUSp2, q1PLUSq2);

        List<Integer> prod = initializeList(P.size());
        ///(P1(X)+P2(X)) * (Q1(X)+Q2(X)) - P1(X)* Q1(X) - P2(X)*Q2(X)
        for(int i=0; i<P.size(); i++)
            prod.set(i,prod12.get(i)-prod1.get(i)-prod2.get(i));
        coefficients = initializeList(P.size()*2);
        for(int i=0; i<P.size(); i++){
            coefficients.set(i, coefficients.get(i)+prod2.get(i));
            coefficients.set(i+P.size(),coefficients.get(i+P.size())+prod1.get(i));
            coefficients.set(i+P.size()/2, coefficients.get(i+P.size()/2)+prod.get(i));
        }
        return coefficients;
    }


    public static Polynomial parallelKaratsuba(Polynomial P, Polynomial Q, Integer noThreads) throws ExecutionException, InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(noThreads);
        List<Integer> result = parallelKaratsubaInternal(P.getCoefficients(), Q.getCoefficients(),
                0, threadPool, noThreads);
        threadPool.shutdownNow();
        result.remove(result.size()-1);
        return new Polynomial(result);
    }

    private static double customLog(double base, double logNumber) {
        return Math.log(logNumber) / Math.log(base);
    }

    private static List<Integer> parallelKaratsubaInternal(List<Integer> P, List<Integer> Q,
                                                           Integer currentDepth, ExecutorService threadPool,
                                                           Integer noThreads) throws ExecutionException, InterruptedException {
        List<Integer> coefficients = initializeList(P.size()*2);

        // daca degree <=10 (sau un treshold) ==> inmultire secventiala
        if(P.size()-1 <= MIN_SIZE){
           List<Integer> lst = sequentialMultiplySequential(new Polynomial(P), new Polynomial(Q)).getCoefficients();
           lst.add(0);
           return lst;
        }

        //(P1(X)+P2(X)) * (Q1(X)+Q2(X)) - P1(X)* Q1(X) - P2(X)*Q2(X)
        Integer size = P.size()/2;
        List<Integer> p1 = initializeList(size);
        List<Integer> p2 = initializeList(size);
        List<Integer> q1 = initializeList(size);
        List<Integer> q2 = initializeList(size);
        List<Integer> p1PLUSp2 = initializeList(size);
        List<Integer> q1PLUSq2 = initializeList(size);

        for(int i=0; i<size; i++) {
            p2.set(i, P.get(i));
            q2.set(i, Q.get(i));
            p1.set(i, P.get(size + i));
            q1.set(i, Q.get(size + i));
            p1PLUSp2.set(i, p1.get(i) + p2.get(i)); // (P1(X)+P2(X))
            q1PLUSq2.set(i, q1.get(i) + q2.get(i)); // (Q1(X)+Q2(X))
        }

        List<Integer> prod, prod2,prod1,prod12;
        if((int) (customLog(3,noThreads)) == currentDepth+1) {
            //P2(X)*Q2(X)
            Future<List<Integer>> fprod2 = threadPool.
                    submit(() -> parallelKaratsubaInternal(p2, q2, currentDepth + 1, threadPool, noThreads));
            // P1(X)* Q1(X)
            Future<List<Integer>> fprod1 = threadPool
                    .submit(() -> parallelKaratsubaInternal(p1, q1, currentDepth + 1, threadPool, noThreads));

            // (P1(X)+P2(X)) * (Q1(X)+Q2(X))
            Future<List<Integer>> fprod12 = threadPool
                    .submit(() -> parallelKaratsubaInternal(p1PLUSp2, q1PLUSq2, currentDepth + 1, threadPool, noThreads));

            //P2(X)*Q2(X)
            prod2 = fprod2.get();
            // P1(X)* Q1(X)
            prod1 = fprod1.get();
            // (P1(X)+P2(X)) * (Q1(X)+Q2(X))
            prod12 = fprod12.get();
        }
        else{
            prod1 = parallelKaratsubaInternal(p1,q1,currentDepth+1,threadPool,noThreads);
            prod2 = parallelKaratsubaInternal(p2,q2,currentDepth+1, threadPool, noThreads);
            prod12 = parallelKaratsubaInternal(p1PLUSp2, q1PLUSq2, currentDepth+1, threadPool, noThreads);
        }
        prod = initializeList(P.size());
        for(int i=0; i<P.size(); i++)
            prod.set(i,prod12.get(i)-prod1.get(i)-prod2.get(i));
        coefficients = initializeList(P.size()*2);
        for(int i=0; i<P.size(); i++){
            coefficients.set(i, coefficients.get(i)+prod2.get(i));
            coefficients.set(i+P.size(),coefficients.get(i+P.size())+prod1.get(i));
            coefficients.set(i+P.size()/2, coefficients.get(i+P.size()/2)+prod.get(i));
        }
        return coefficients;
    }

    private static List<Integer> initializeList(Integer size){
        List<Integer> lst = new ArrayList<>();
        for(int i=0; i<size; i++)
            lst.add(0);
        return lst;
    }
}
