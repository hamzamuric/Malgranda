package mgr_native;

import mgr.Interpreter;
import mgr.MgrCallable;

import java.util.List;
import java.util.Scanner;

public class Input implements MgrCallable {
    private static final Scanner scn = new Scanner(System.in);

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return scn.nextLine();
    }

    @Override
    public String toString() {
        return "<native fn input>";
    }
}
