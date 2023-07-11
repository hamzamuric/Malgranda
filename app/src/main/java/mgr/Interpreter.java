package mgr;

import mgr_native.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();

    Interpreter() {
        globals.define("input", new Input());
        globals.define("clock", new Clock());
        globals.define("reverse", new Reverse());
        globals.define("Socket", new MgrSocket());
        globals.define("ServerSocket", new MgrServerSocket());
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Mgr.runtimeError(error);
        }
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        Object sup = null;
        if (stmt.superclass != null) {
            sup = evaluate(stmt.superclass);
            if (!(sup instanceof MgrClass)) {
                throw new RuntimeError(stmt.superclass.name, "Superclass must be a class");
            }
        }

        MgrClass superclass = (MgrClass)sup;

        environment.define(stmt.name.lexeme, null);

        if (stmt.superclass != null) {
            environment = new Environment(environment);
            environment.define("super", superclass);
        }

        Map<String, MgrFunction> methods = new HashMap<>();
        for (Stmt.Function method : stmt.methods) {
            MgrFunction function = new MgrFunction(method, environment, method.name.lexeme.equals("init"));
            methods.put(method.name.lexeme, function);
        }

        Map<String, MgrGetter> getters = new HashMap<>();
        for (Stmt.Getter getterStmt : stmt.getters) {
            MgrGetter getter = new MgrGetter(getterStmt, environment);
            getters.put(getterStmt.name.lexeme, getter);
        }

        MgrClass klass = new MgrClass(stmt.name.lexeme, superclass, methods, getters);
        if (superclass != null) {
            environment = environment.enclosing;
        }
        environment.assign(stmt.name, klass);
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.name, expr);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        Object value = distance != null
                ? environment.getAt(distance, name.lexeme)
                : globals.get(name);

        if (value instanceof Undefined) {
            throw new RuntimeError(name, "Accessing undefined value.");
        }

        return value;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = Undefined.getInstance();
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);

        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }

        return value;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        MgrFunction function = new MgrFunction(stmt, environment, false);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitGetterStmt(Stmt.Getter stmt) {
        MgrGetter getter = new MgrGetter(stmt, environment);
        environment.define(stmt.name.lexeme, getter);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        try {
            while (isTruthy(evaluate(stmt.condition))) {
                execute(stmt.body);
            }
        } catch (BreakException ignored) { }
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new BreakException();
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
            case GREATER:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left > (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return ((String)left).compareTo((String)right) > 0;
                }

                if (left instanceof Boolean && right instanceof Boolean) {
                    return (boolean)left && !(boolean)right;
                }

                throw new RuntimeError(expr.operator, "Both operands must be numbers, strings or booleans.");
            case GREATER_EQUAL:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left >= (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return ((String)left).compareTo((String)right) >= 0;
                }

                if (left instanceof Boolean && right instanceof Boolean) {
                    return (boolean)left || ((boolean)left == (boolean)right);
                }

                throw new RuntimeError(expr.operator, "Both operands must be numbers, strings or booleans.");
            case LESS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left < (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return ((String)left).compareTo((String)right) < 0;
                }

                if (left instanceof Boolean && right instanceof Boolean) {
                    return !(boolean)left && (boolean)right;
                }

                throw new RuntimeError(expr.operator, "Both operands must be numbers, strings or booleans.");
            case LESS_EQUAL:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left <= (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return ((String)left).compareTo((String)right) <= 0;
                }

                if (left instanceof Boolean && right instanceof Boolean) {
                    return !(boolean)left || ((boolean)left == (boolean)right);
                }

                throw new RuntimeError(expr.operator, "Both operands must be numbers, strings or booleans.");
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if ((double)right == 0) {
                    throw new RuntimeError(expr.operator, "Zero division error");
                }
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }

                if (left instanceof String) {
                    return (String)left + stringify(right);
                }

                if (right instanceof String) {
                    return stringify(left) + (String)right;
                }

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case ELVIS:
                return left != null ? left : right;
        }

        // Unreachable.
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof MgrCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }

        MgrCallable function = (MgrCallable)callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren,
        "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }
        return function.call(this, arguments);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof MgrInstance) {
            Object property = ((MgrInstance)object).get(expr.name);
            if (property instanceof MgrGetter) {
                return ((MgrGetter)property).call(this, null);
            }
            return property;
        }

        throw new RuntimeError(expr.name, "Only instances have properties.");
    }

    @Override
    public Object visitNilGetExpr(Expr.NilGet expr) {
        Object object = evaluate(expr.object);
        if (object == null) {
            return null;
        } else if (object instanceof MgrInstance) {
            return ((MgrInstance)object).get(expr.name);
        }

        throw new RuntimeError(expr.name, "Only instances have properties.");
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);

        if (!(object instanceof MgrInstance)) {
            throw new RuntimeError(expr.name, "Only instances have fields.");
        }

        Object value = evaluate(expr.value);
        ((MgrInstance)object).set(expr.name, value);
        return value;
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariable(expr.keyword, expr);
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        int distance = locals.get(expr);
        MgrClass superclass = (MgrClass)environment.getAt(distance, "super");
        MgrInstance object = (MgrInstance)environment.getAt(distance - 1, "this");
        MgrFunction method = superclass.findMethod(expr.method.lexeme);

        if (method == null) {
            throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme + "'.");
        }

        return method.bind(object);
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        return evaluate(expr.first) == Boolean.TRUE
            ? evaluate(expr.second)
            : evaluate(expr.third);
    }

    public Object visitLambdaExpr(Expr.Lambda expr) {
        return new MgrLambda(expr, environment);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
        }

        // Unreachable.
        return null;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }
}
