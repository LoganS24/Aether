public class VarDecl {
    private String name;
    private Object expression;

    public VarDecl(String name, Object expression) {
        this.name = name;
        this.expression = expression;
    }
    
    public VarDecl(String name) {
        this.name = name;
        this.expression = null;
    }

    public String toString() {
        if (expression != null) {
            return "(" + name + " = " + expression + ")";
        } else {
            return "(" + name + ")";
        }
    }
}
