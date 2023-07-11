package mgr_native;

import mgr.Interpreter;
import mgr.MgrCallable;

import java.util.List;

public class Clock implements MgrCallable {
    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double)System.currentTimeMillis() / 1000.0;
    }

    @Override
    public String toString() {
        return "<native fn clock>";
    }
}
