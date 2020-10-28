import java.util.Arrays;

public class MatrixOperations {

    public static double getProductValue(MatrixCustom a, MatrixCustom b, Integer line, Integer column){
        double[] lineFromA = a.getLine(line);
        double[] columnFromB = b.getColumn(column);

        double productValue = 0;
        for(int i=0; i<a.getColumnNo(); i++) {
            productValue += lineFromA[i] * columnFromB[i];
        }
        return productValue;
    }
}
