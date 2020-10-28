import com.sun.tools.javac.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    private static final Integer noTasks = 5;

    public static void main(String[] args) {
        MatrixCustom a = new MatrixCustom("./input/matrix1.in");
        MatrixCustom b = new MatrixCustom("./input/matrix2.in");
        System.out.println("A\n"+a);
        System.out.println("B\n"+b);
        if(a.getColumnNo()!=b.getLineNo()) {
            System.out.println("Can not multiply A and B");
            return;
        }

        System.out.println("TASK 1 - THREADS");
        threadsTask1(a,b);
        System.out.println("TASK 1 - THREAD POOL");
        threadPoolTask1(a,b);
        System.out.println("TASK 2 - THREADS");
        threadsTask2(a,b);
        System.out.println("TASK 2 - THREAD POOL");
        threadPoolTask2(a,b);
        System.out.println("TASK 3 - THREADS");
        threadsTask3(a,b);
        System.out.println("TASK 3 - THREAD POOL");
        threadPoolTask3(a,b);
    }

    /*ROW BY ROW*/

    private static void threadsTask1(MatrixCustom A, MatrixCustom B){
        try {
            MatrixCustom c = new MatrixCustom(A.getLineNo(), B.getColumnNo());
            List<Thread> threads = new ArrayList<>();
            int noElements = A.getLineNo()*B.getColumnNo();
            int howMany = noElements / noTasks;
            int order = 1;
            for(int i=0; i<noTasks; i++){
                if(i+1==noTasks)
                    howMany += noElements%noTasks;
                Pair<Integer,Integer> startPos = getElementCoordinatesTask1(c.getColumnNo(), order);
                int finalHowMany = howMany;
                Thread t = new Thread(() -> taskType1(startPos.fst, startPos.snd, finalHowMany, A, B, c));
                threads.add(t);
                order += howMany;
            }
            for (Thread t : threads) {
                t.start();
            }
            for (Thread t : threads) {
                t.join();
            }
            System.out.println(c);
        }
        catch (Exception e){
            System.out.println(e);
        }

    }

    private static void threadPoolTask1(MatrixCustom A, MatrixCustom B){
        try {
            MatrixCustom c = new MatrixCustom(A.getLineNo(), B.getColumnNo());
            ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Future<String>> results = new ArrayList<>();

            int noElements = A.getLineNo()*B.getColumnNo();
            int howMany = noElements / noTasks;
            int order = 1;

            for(int i=0; i<noTasks; i++){
                if(i+1==noTasks)
                    howMany += noElements%noTasks;
                Pair<Integer,Integer> startPos = getElementCoordinatesTask1(c.getColumnNo(), order);
                int finalHowMany = howMany;
                results.add(service.submit(() -> {
                    taskType1(startPos.fst, startPos.snd, finalHowMany, A,B,c);
                    return "";
                }));

                order += howMany;
            }

            // not necessary but just to make sure everything is ok
            for(Future<String> f: results)
                f.get(); // gets the result from the submitted task and forces execution
            // if not yet executed
            service.shutdownNow();
            System.out.println(c);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }


    private static void taskType1(Integer startRow, Integer startColumn, Integer noElements, MatrixCustom a, MatrixCustom b, MatrixCustom c){
        int computedElements = 0;
        int line = startRow;
        int column = startColumn;
        /*System.out.println("noELems "+noElements );
        System.out.println("Started at "+startRow+","+startColumn);*/
        while (computedElements < noElements && line < c.getLineNo()){
            if(column == c.getColumnNo()){
                column = 0;
                line++;
                if(line == c.getLineNo())
                    break;
            }
            double val = MatrixOperations.getProductValue(a,b,line,column);
            c.setElement(line,column,val);
            column++;
            computedElements++;
        }
        //System.out.println("Ended at "+line+","+column);
    }

    private static Pair<Integer,Integer> getElementCoordinatesTask1(Integer noColumns, Integer orderNo){
        /*
        9*9

                  c0 c1  c2  c3  c4  c5  c6  c7  c8
             l1    1   2   3   4   5   6   7   8   9
             l2   10  11  12  13  14  15  16  17  18
             l3   19  20  21  22  23  24  25  26  27

          if(n%nrCols==0)
                i = n/nrCols-1
                j=nrCols-1
                return
           i = n/nrCols
           j = n%nrCols-1

         */
        if(orderNo%noColumns==0)
            return new Pair<>(orderNo/noColumns-1, noColumns-1);
        return new Pair<>(orderNo/noColumns, orderNo%noColumns-1);
    }
    /*
    COLUMN BY COLUMN
     */

    private static Pair<Integer,Integer> getElementCoordinatesTask2(Integer noLines, Integer orderNo){
        /*
        9*9

                    c0   c1   c2
               l0    1   11   19
               l1    2   12   20
               l2    3   13   21
               l3    4   14   22
               l5    5   15   23
               l6    7   16   24
               l7    8   17   25
               l8    9   18


          if(n%nrCols==0)
                j = n/nrLines-1
                i=nrLines-1
                return
           j = n/nrLines
           i = n%nrLines-1
         */
        if(orderNo%noLines==0)
            return new Pair<>(noLines-1, orderNo/noLines-1);
        return new Pair<>(orderNo%noLines-1,orderNo/noLines);
    }

    private static void taskType2( Integer startRow, Integer startColumn, Integer noElements, MatrixCustom a, MatrixCustom b, MatrixCustom c){
        int computedElements = 0;
        int line = startRow;
        int column = startColumn;
        while (computedElements < noElements) {
            if (line == c.getLineNo()) {
                line = 0;
                column++;
                if(column == c.getColumnNo())
                    break;
            }
            double val = MatrixOperations.getProductValue(a, b, line, column);
            c.setElement(line, column, val);
            line++;
            computedElements++;
        }
    }

    private static void threadsTask2(MatrixCustom A, MatrixCustom B){
        try {
            MatrixCustom c = new MatrixCustom(A.getLineNo(), B.getColumnNo());
            List<Thread> threads = new ArrayList<>();
            int noElements = A.getLineNo()*B.getColumnNo();
            int howMany = noElements / noTasks;
            int order = 1;
            for(int i=0; i<noTasks; i++){
                if(i+1==noTasks)
                    howMany += noElements%noTasks;
                Pair<Integer,Integer> startPos = getElementCoordinatesTask2(c.getLineNo(), order);
                int finalHowMany = howMany;
                Thread t = new Thread(() -> taskType2(startPos.fst, startPos.snd, finalHowMany, A, B, c));
                threads.add(t);
                order += howMany;
            }
            for (Thread t : threads) {
                t.start();
            }
            for (Thread t : threads) {
                t.join();
            }
            System.out.println(c);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    private static void threadPoolTask2(MatrixCustom A, MatrixCustom B){
        try {
            MatrixCustom c = new MatrixCustom(A.getLineNo(), B.getColumnNo());
            ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Future<String>> results = new ArrayList<>();

            int noElements = A.getLineNo()*B.getColumnNo();
            int howMany = noElements / noTasks;
            int order = 1;

            for(int i=0; i<noTasks; i++){
                if(i+1==noTasks)
                    howMany += noElements%noTasks;
                Pair<Integer,Integer> startPos = getElementCoordinatesTask2(c.getLineNo(), order);
                int finalHowMany = howMany;
                results.add(service.submit(() -> {
                    taskType2(startPos.fst, startPos.snd, finalHowMany, A,B,c);
                    return "";
                }));

                order += howMany;
            }

            // not necessary but just to make sure everything is ok
            for(Future<String> f: results)
                f.get(); // gets the result from the submitted task and forces execution
            // if not yet executed
            service.shutdownNow();
            System.out.println(c);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    /* By 3rd criteria */

    private static void taskType3(Pair<Integer,Integer> start, Integer orderNo, MatrixCustom a, MatrixCustom b, MatrixCustom c){
        while (true){
            double val = MatrixOperations.getProductValue(a,b,start.fst, start.snd);
            c.setElement(start.fst, start.snd, val);
            orderNo += noTasks;
            start = getElementCoordinatesTask1(c.getColumnNo(), orderNo);
            if(start.fst>=c.getLineNo())
                break;
        }
    }

    private static void threadsTask3(MatrixCustom A, MatrixCustom B){
        try {
            MatrixCustom c = new MatrixCustom(A.getLineNo(), B.getColumnNo());
            List<Thread> threads = new ArrayList<>();
            for(int i=0; i<noTasks; i++){
                int finalI = i+1;
                Pair<Integer,Integer> startPos = getElementCoordinatesTask1(c.getColumnNo(), i+1);
                Thread t = new Thread(() -> taskType3(startPos, finalI, A, B, c));
                threads.add(t);
            }
            for (Thread t : threads) {
                t.start();
            }
            for (Thread t : threads) {
                t.join();
            }
            System.out.println(c);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    private static void threadPoolTask3(MatrixCustom A, MatrixCustom B){
        try {
            MatrixCustom c = new MatrixCustom(A.getLineNo(), B.getColumnNo());
            ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Future<String>> results = new ArrayList<>();
            for(int i=0; i<noTasks; i++){
                Pair<Integer,Integer> startPos = getElementCoordinatesTask1(c.getColumnNo(), i+1);
                int finalI = i+1;
                results.add(service.submit(() -> {
                    taskType3(startPos, finalI, A, B, c);
                    return "";
                }));
            }

            // not necessary but just to make sure everything is ok
            for(Future<String> f: results)
                f.get(); // gets the result from the submitted task and forces execution
            // if not yet executed
            service.shutdownNow();
            System.out.println(c);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}

