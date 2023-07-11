package mgr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MgrNativeClass extends MgrClass {
    private final int arity;
    private final Map<String, MgrCallable> nativeMethods;

    protected MgrNativeClass(String name, int arity) {
        super(name, null, new HashMap<>(), new HashMap<>());
        this.arity = arity;
        nativeMethods = new HashMap<>();
    }

    protected void defineNativeMethod(String name, MgrCallable method) {
        nativeMethods.put(name, method);
    }

    MgrCallable findNativeMethod(String name) {
        return nativeMethods.get(name);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return new MgrInstance(this);
    }

    @Override
    public int arity() {
        return this.arity;
    }
}
