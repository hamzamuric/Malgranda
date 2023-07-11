package mgr_native;

import mgr.Interpreter;
import mgr.MgrCallable;

import java.util.List;

public class Reverse implements MgrCallable {
    @Override
    public int arity() {
        return 1;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        String s = arguments.get(0).toString();
        StringBuilder reversed = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            reversed.append(s.charAt(s.length() - i - 1));
        }
        return reversed.toString();
    }

    @Override
    public String toString() {
        return "<native fn reverse>";
    }
}
