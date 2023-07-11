package mgr;

import java.util.List;

public class MgrGetter implements MgrCallable {
    private final Stmt.Getter declaration;
    private final Environment clojure;

    MgrGetter(Stmt.Getter declaration, Environment clojure) {
        this.declaration = declaration;
        this.clojure = clojure;
    }

    MgrGetter bind(MgrInstance instance) {
        Environment environment = new Environment(clojure);
        environment.define("this", instance);
        return new MgrGetter(declaration, environment);
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(clojure);
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }
}
