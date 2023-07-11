package mgr;

import java.util.List;

public class MgrFunction implements MgrCallable {
    private final Stmt.Function declaration;
    private final Environment clojure;
    private final boolean isInitializer;

    MgrFunction(Stmt.Function declaration, Environment clojure, boolean isInitializer) {
        this.declaration = declaration;
        this.clojure = clojure;
        this.isInitializer = isInitializer;
    }

    MgrFunction bind(MgrInstance instance) {
        Environment environment = new Environment(clojure);
        environment.define("this", instance);
        return new MgrFunction(declaration, environment, isInitializer);
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(clojure);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            if (isInitializer) return clojure.getAt(0, "this");
            return returnValue.value;
        }

        if (isInitializer) return clojure.getAt(0, "this");
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
