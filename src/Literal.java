

public class Literal {
    private final Object Value;

    public Literal(Object value) {
        this.Value = value;
    }

    public String toString() {
        return Value.toString();
    }
}
