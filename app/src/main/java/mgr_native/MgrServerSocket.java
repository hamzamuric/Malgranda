package mgr_native;

import mgr.Interpreter;
import mgr.MgrCallable;
import mgr.MgrInstance;
import mgr.MgrNativeClass;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

public class MgrServerSocket extends MgrNativeClass {
    private ServerSocket serverSocket;

    public MgrServerSocket() {
        super("ServerSocket", 1);
        defineNativeMethod("accept", new MgrCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                try {
                    MgrSocket socket = new MgrSocket(serverSocket.accept());
                    return new MgrInstance(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        MgrInstance instance = new MgrInstance(this);
        int port;
        try {
            Double p = (Double)arguments.get(0);
            port = p.intValue();
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return instance;
    }
}
