package mgr_native;

import mgr.Interpreter;
import mgr.MgrCallable;
import mgr.MgrInstance;
import mgr.MgrNativeClass;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class MgrSocket extends MgrNativeClass {
    private Socket socket;
    private PrintWriter pw;
    private Scanner sc;

    public MgrSocket(Socket socket) {
        this();
        this.socket = socket;
        try {
            this.pw = new PrintWriter(socket.getOutputStream(), true);
            this.sc = new Scanner(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public MgrSocket() {
        super("Socket", 2);
        defineNativeMethod("send", new MgrCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String data = arguments.get(0).toString();
                pw.println(data);
                return null;
            }
        });
        defineNativeMethod("receive", new MgrCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return sc.nextLine();
            }
        });

        defineNativeMethod("close", new MgrCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                return null;
            }
        });
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        MgrInstance instance = new MgrInstance(this);
        String address;
        int port;
        try {
            address = (String)arguments.get(0);
            Double p = (Double)arguments.get(1);
            port = p.intValue();
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
        try {
            this.socket = new Socket(address, port);
            this.pw = new PrintWriter(this.socket.getOutputStream(), true);
            this.sc = new Scanner(this.socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return instance;
    }
}
