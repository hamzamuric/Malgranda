package mgr;

public class Undefined {
    private static final Undefined instance = new Undefined();

    private Undefined() { }

    public static Undefined getInstance() {
        return instance;
    }
}
