import java.util.List;
import java.util.ArrayList;

public class Block {
    public final List<Object> declarations;

    public Block(List<Object> declarations) {
        this.declarations = declarations;
    }

    public String toString() {
        return "(block " + declarations + ")";
    }
}
