

public class Unary {
    private final Token operator;
    private final Object right;

    public Unary(Token operator, Object right) {
        this.operator = operator;
        this.right = right;
    }

    public String toString() {
        //returns string version of the LISP representation of the unary expression
        return "(" + operator.getLexeme() + " " + right.toString() + ")";
    }
}
