public class Variable {
    public final Token name;

    public Variable(Token name) {
        this.name = name;
    }

    public String toString() {
        return name.getLexeme();
    }
}
