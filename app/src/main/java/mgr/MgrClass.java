package mgr;

import java.util.List;
import java.util.Map;

public class MgrClass implements MgrCallable {
    final String name;
    final MgrClass superclass;
    protected final Map<String, MgrFunction> methods;
    protected final Map<String, MgrGetter> getters;

    protected MgrClass(String name, MgrClass superclass, Map<String, MgrFunction> methods, Map<String, MgrGetter> getters) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
        this.getters = getters;
    }

    MgrFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        if (superclass != null) {
            return superclass.findMethod(name);
        }

        return null;
    }

    MgrGetter findGetter(String name) {
        if (getters.containsKey(name)) {
            return getters.get(name);
        }

        return null;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        MgrInstance instance = new MgrInstance(this);
        MgrFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    @Override
    public int arity() {
        MgrFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }

    @Override
    public String toString() {
        return name;
    }
}
