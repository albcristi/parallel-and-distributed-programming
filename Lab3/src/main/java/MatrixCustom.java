import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

public class MatrixCustom {
    private Integer lines;
    private Integer columns;
    private double[][] matrix;

    public MatrixCustom(String pathToMatrix){
        readMatrix(pathToMatrix);
    }

    public MatrixCustom(Integer lines, Integer columns){
        this.lines = lines;
        this.columns = columns;
        matrix = new double[lines][columns];
    }

    public Integer getLineNo(){return this.lines;}

    public Integer getColumnNo(){return this.columns;}

    private void readMatrix(String pathToMatrix){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(pathToMatrix));
            this.lines = Integer.parseInt(reader.readLine());
            this.columns = Integer.parseInt(reader.readLine());
            matrix = new double[lines][columns];
            for(int i=0; i<this.lines; i++){
                List<String> elements = Arrays.asList(reader.readLine().split(","));
                for(int j=0; j<this.columns; j++)
                    matrix[i][j] = Integer.parseInt(elements.get(j));
            }
            reader.close();

        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public double getElement(Integer line, Integer column){
        return matrix[line][column];
    }

    public void setElement(Integer line, Integer column, double value){
        matrix[line][column] = value;
    }

    public double[] getLine(Integer lineNumber){
        double[] line = new double[columns];
        if (columns >= 0)
            System.arraycopy(matrix[lineNumber], 0, line, 0, columns);
        return line;
    }

    public double[] getColumn(Integer columnNumber){
        double[] column = new double[lines];
        for(int i=0; i<this.lines; i++)
            column[i] = matrix[i][columnNumber];
        return column;
    }


    @Override
    public String toString() {
        String str = "";
        for(int i=0; i<lines; i++){
            for(int j=0; j<columns; j++)
                str += matrix[i][j]+" ";
            str += "\n";
        }
        return str;
    }
}
