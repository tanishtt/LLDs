package com.tanish.lld.composite.expressionTree;

import java.util.Map;

interface Expression{
    double evaluate();
    String print();
}

//LEAF NODE
class Number implements Expression{

    private final double value;

    Number(double value) {
        this.value = value;
    }

    @Override
    public double evaluate() {
        return value;
    }

    @Override
    public String print() {
        return String.valueOf(value);
    }
}
class Variable implements Expression{

    private final String variableName;
    private final Map<String, Double> context;

    Variable(String variableName, Map<String, Double> context) {
        this.variableName = variableName;
        this.context = context;
    }

    @Override
    public double evaluate() {
        if(!context.containsKey(variableName)){
            throw new RuntimeException("Variable not found "+variableName);
        }
        return context.get(variableName);
    }

    @Override
    public String print() {
        return variableName;
    }
}

//COMPOSITE NODE
//BINARY
abstract class BinaryExpression implements Expression{
    protected final Expression left;
    protected final Expression right;

    protected BinaryExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }
}

class DivideExpression extends BinaryExpression{

    protected DivideExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public double evaluate() {
        if(right.evaluate() == 0){
            throw new ArithmeticException("divide by zero.");
        }
        return left.evaluate() / right.evaluate();
    }

    @Override
    public String print() {
        return "("+left.print()+"/"+right.print()+")";
    }
}
class AddExpression extends BinaryExpression{

    protected AddExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public double evaluate() {
        return left.evaluate() + right.evaluate();
    }

    @Override
    public String print() {
        return "("+left.print()+"+"+right.print()+")";
    }
}
class SubstractExpression extends BinaryExpression{

    protected SubstractExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public double evaluate() {
        return left.evaluate() - right.evaluate();
    }

    @Override
    public String print() {
        return "("+left.print()+"-"+right.print()+")";
    }
}
class MultiplyExpression extends BinaryExpression{

    protected MultiplyExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public double evaluate() {
        return left.evaluate()*right.evaluate();
    }

    @Override
    public String print() {
        return "("+left.print()+"*"+right.print()+")";
    }
}
class MaxExpression extends BinaryExpression{

    protected MaxExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public double evaluate() {
        return Math.max(left.evaluate(), right.evaluate());
    }

    @Override
    public String print() {
        return "MAX("+left.print()+","+right.print()+")";
    }
}
class MinExpression extends BinaryExpression{

    protected MinExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public double evaluate() {
        return Math.min(left.evaluate(), right.evaluate());
    }

    @Override
    public String print() {
        return "MIN("+left.print()+","+right.print()+")";
    }
}

//UNARY
abstract class UnaryExpression implements Expression{
    protected final Expression expression;

    UnaryExpression(Expression expression) {
        this.expression = expression;
    }
}
class NegateExpression extends UnaryExpression{

    NegateExpression(Expression expression) {
        super(expression);
    }

    @Override
    public double evaluate() {
        return -expression.evaluate();
    }

    @Override
    public String print() {
        return "-("+expression.print()+")";
    }
}
//postfix
//prefix etc...


public class expressionTreeDriver {
    public static void main(String[] args) {
        Expression expression=new DivideExpression(
                new MultiplyExpression(
                        new AddExpression(new Number(2), new Number(40)),
                        new SubstractExpression(new Number(5), new Number(1))
                ),
                new Number(23)
        );
        System.out.println(expression.print() + " : " + expression.evaluate());

        Expression exp =
                new MaxExpression(
                        new Number(10),
                        new Number(20)
                );

        System.out.println(exp.print()+" : "+exp.evaluate());

        Expression exp2 =
                new NegateExpression(
                        new Number(10)
                );

        System.out.println(exp2.print()+" : "+exp2.evaluate());

        Map<String, Double> variables = Map.of(
                "x", 10.0,
                "y", 20.0,
                "z", 5.0
        );

        Expression exp4 =
                new MultiplyExpression(

                        new AddExpression(
                                new Variable("x", variables),
                                new Variable("y", variables)
                        ),

                        new Variable("z", variables)
                );

        System.out.println(exp4.print()+" : "+exp4.evaluate());
    }
}
