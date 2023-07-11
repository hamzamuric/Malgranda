package mgr;

class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return parenthesize(expr.name.lexeme, expr.value);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        return parenthesize(expr.callee.accept(this), (Expr[])expr.arguments.toArray());
    }

    @Override
    public String visitGetExpr(Expr.Get expr) {
        return "(:" + expr.name.lexeme + " " + expr.object.accept(this) + ")";
    }

    @Override
    public String visitNilGetExpr(Expr.NilGet expr) {
        return "(?. " + expr.name.lexeme + " " + expr.object.accept(this) + ")";
    }

    @Override
    public String visitSetExpr(Expr.Set expr) {
        return "(= :" + expr.name.lexeme + " " + expr.object.accept(this) + expr.value.accept(this) + ")";
    }

    @Override
    public String visitThisExpr(Expr.This expr) {
        return "@";
    }

    @Override
    public String visitSuperExpr(Expr.Super expr) {
        return "^@";
    }

    public String visitTernaryExpr(Expr.Ternary expr) {
        return parenthesize(expr.operator1.lexeme + expr.operator2.lexeme, expr.first, expr.second, expr.third);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return expr.value == null ? "nil" : expr.value.toString();
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitLambdaExpr(Expr.Lambda expr) {
        return "(lambda)";
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}
