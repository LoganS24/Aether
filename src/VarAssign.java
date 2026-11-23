public class VarAssign {
    private String name;
    private Object expression;
    
    public VarAssign(String name, Object expression) {
        this.name = name;
        this.expression = expression;
    }

    public String toString() {
        return "(" + name + " = " + expression + ")";
    }
}
