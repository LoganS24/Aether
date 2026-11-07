public class ExpressionStatement {
    private Object expression;

    public ExpressionStatement(Object expression) {
        this.expression = expression;
    }

    public String toString() {
        //returns string version of the LISP representation of the expression statement
        return "(expr " + expression.toString() + ")";
    }
    
}
