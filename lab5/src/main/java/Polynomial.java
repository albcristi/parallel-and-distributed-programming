import java.util.ArrayList;
import java.util.List;

public class Polynomial {
    // element at pos 2 --> a*x^2
    private List<Integer> coefficients;
    private Integer degree;

    public Polynomial(List<Integer> coefficients) {
        this.coefficients = coefficients;
        this.degree = coefficients.size() - 1;
    }

    public Polynomial(Integer degree){
        this.degree = degree;
        initializeWithZeros(degree);
    }

    public Integer getDegree() {
        return this.degree;
    }

    public List<Integer> getCoefficients() {
        return this.coefficients;
    }

    public void initializeWithZeros(Integer degree){
        this.coefficients = new ArrayList<>();
        for(int i=0; i<=degree; i++)
            coefficients.add(0);
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("");
        for(int power=this.coefficients.size()-1; power>=0; power--) {
            string.append(coefficients.get(power));
            if(power!=0)
                string.append("x^").append(power).append("+");
        }
        return string.toString();
    }
}

