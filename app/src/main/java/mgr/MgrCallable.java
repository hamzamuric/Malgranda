package mgr;

import java.util.List;

public interface MgrCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
