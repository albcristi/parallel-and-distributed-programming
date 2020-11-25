import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main {
    private static Integer BOUND_VALUE = 5;
    private static Integer NO_THREADS = 9;

    public static void main(String[] args) {
        Polynomial p1 = new Polynomial(generatePolynomialCoefficients(4095));
        System.out.println("Polynomial P is:");
        System.out.println(p1);
        Polynomial p2 = new Polynomial(generatePolynomialCoefficients(4095));
        System.out.println("Polynomial Q is:");
        System.out.println(p2);
        System.out.println("Results:");
        System.out.println("Sequential O(n2)");
        System.out.println(PolynomialOperations.sequentialMultiplySequential(p1, p2));
        /*System.out.println("Parallel simple:");
        try {
            System.out.println(PolynomialOperations.threadSimplePolyMultiplication(p1,p2,5));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        System.out.println("Karatsuba simple");
        System.out.println(PolynomialOperations.sequentialKaratsuba(p1,p2));
        System.out.println("Karatsuba with threads");
        try {
            System.out.println(PolynomialOperations.parallelKaratsuba(p1,p2, NO_THREADS));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static List<Integer> generatePolynomialCoefficients(Integer degree){
        List<Integer> coefficients = new ArrayList<>();
        for(int i=0; i<degree; i++)
            coefficients.add((int) ((Math.random() * (BOUND_VALUE + BOUND_VALUE)) - BOUND_VALUE));
        Integer nonZero = (int) ((Math.random() * (BOUND_VALUE + BOUND_VALUE)) - BOUND_VALUE);
        if(nonZero == 0)
            nonZero = 1;
        coefficients.add(nonZero);
        return coefficients;
    }
}
