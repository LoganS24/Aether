

public class Grouping {
    private final Object expression;

    public Grouping(Object expression) {
        this.expression = expression;
    }

    public String toString() {
        //returns string version of the LISP representation of the grouping expression
        return "(group " + expression.toString() + ")";
    }
}


