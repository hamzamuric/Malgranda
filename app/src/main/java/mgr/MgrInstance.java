package mgr;

import java.util.HashMap;
import java.util.Map;

public class MgrInstance {
    private final MgrClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    public MgrInstance(MgrClass klass) {
        this.klass = klass;
    }

    Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        MgrGetter getter = klass.findGetter(name.lexeme);
        if (getter != null) return getter.bind(this);

        MgrFunction method = klass.findMethod(name.lexeme);
        if (method != null) return method.bind(this);

        if (klass instanceof MgrNativeClass) {
            MgrNativeClass nativeClass = (MgrNativeClass)klass;
            MgrCallable nativeMethod = nativeClass.findNativeMethod(name.lexeme);
            if (nativeMethod != null) return nativeMethod;
        }

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    protected void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }
}
