public class PrintStatement {
    private Object expression;

    public PrintStatement(Object expression) {
        this.expression = expression;
    }

    public String toString() {
        //returns string version of the LISP representation of the print statement
        return "(print " + expression.toString() + ")";
    }
}
