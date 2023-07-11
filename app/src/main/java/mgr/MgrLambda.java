package mgr;

import java.util.List;

public class MgrLambda implements MgrCallable {
    private final Expr.Lambda expression;
    private final Environment clojure;

    MgrLambda(Expr.Lambda expression, Environment clojure) {
        this.expression = expression;
        this.clojure = clojure;
    }

    @Override
    public int arity() {
        return expression.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(clojure);
        for (int i = 0; i < expression.params.size(); i++) {
            environment.define(expression.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(expression.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }

    @Override
    public String toString() {
        return "<lambda>";
    }
}
