package mihailris.oiscript.parsing;

import mihailris.oiscript.*;
import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.jit.OiType;

import java.util.Collection;

public class BinaryOperator extends Value {
    private Value left;
    private Value right;
    private final String operator;
    public BinaryOperator(Value leftOperand, String operator, Value rightOperand) {
        this.left = leftOperand;
        this.right = rightOperand;
        this.operator = operator;
    }

    @Override
    public Value build(SemanticContext context) throws ParsingException {
        left = left.build(context);
        right = right.build(context);
        return super.build(context);
    }

    @Override
    public Value optimize() {
        left = left.optimize();
        right = right.optimize();
        if (left instanceof NumberValue && right instanceof NumberValue) {
            double lvalue = ((NumberValue) left).value;
            double rvalue = ((NumberValue) right).value;
            double value;
            switch (operator) {
                case "+": value = lvalue + rvalue; break;
                case "-": value = lvalue - rvalue; break;
                case "*": value = lvalue * rvalue; break;
                case "/": value = lvalue / rvalue; break;
                case "to":
                    return this;
                default:
                    throw new IllegalStateException("not implemented for operator '"+operator+"'");
            }
            return NumberValue.choose(value);
        }
        return this;
    }

    @Override
    public Object eval(Context context) {
        Object leftValue = left.eval(context);
        Object rightValue = right.eval(context);

        switch (operator) {
            case "+": return Arithmetics.add(leftValue, rightValue);
            case "-": return Arithmetics.subtract(leftValue, rightValue);
            case "*": return Arithmetics.multiply(leftValue, rightValue);
            case "/": return Arithmetics.divide(leftValue, rightValue);
            case "//": return Arithmetics.divideInteger(leftValue, rightValue);
            case "%": return Arithmetics.modulo(leftValue, rightValue);
            case "**": return Arithmetics.power(leftValue, rightValue);
            case "==": return Logics.equals(leftValue, rightValue);
            case ">": return Logics.greather(leftValue, rightValue);
            case "<": return Logics.less(leftValue, rightValue);
            case "!=": return !Logics.equals(leftValue, rightValue);
            case ">=": return Logics.gequals(leftValue, rightValue);
            case "<=": return Logics.lequals(leftValue, rightValue);
            case "and": return Logics.and(leftValue, rightValue);
            case "or": return Logics.or(leftValue, rightValue);
            case "to": return new Range(((Number)leftValue).longValue(), ((Number)rightValue).longValue());
            case "in": return Logics.in(leftValue, rightValue);
            case ">>": return (((Number)leftValue).longValue() >> ((Number)rightValue).longValue());
            case "<<": return (((Number)leftValue).longValue() << ((Number)rightValue).longValue());
            case ">>>": return (((Number)leftValue).longValue() >>> ((Number)rightValue).longValue());
            case "&": return (((Number)leftValue).longValue() & ((Number)rightValue).longValue());
            case "|": return (((Number)leftValue).longValue() | ((Number)rightValue).longValue());
            default:
                throw new IllegalStateException("not implemented for operator '"+operator+"'");
        }
    }

    @Override
    public String toString() {
        return "("+left+" "+operator+" "+right+")";
    }

    /**
     * Binary operations tree priorety correction
     * @return new BinaryOperation based on this and right suboperation
     */
    public BinaryOperator correction() {
        if (left instanceof BinaryOperator) {
            left = ((BinaryOperator) left).correction();
        }
        if (right instanceof BinaryOperator) {
            BinaryOperator rightBinOp = (BinaryOperator) right;
            right = rightBinOp.correction();
            int priorety = Operators.operatorPriorety(operator);
            int otherPriorety = Operators.operatorPriorety(rightBinOp.operator);
            if (otherPriorety <= priorety) {
                Value rbleft = rightBinOp.left;
                Value rbright = rightBinOp.right;
                return new BinaryOperator(new BinaryOperator(left, operator, rbleft), rightBinOp.operator, rbright).correction();
            }
        }
        return this;
    }

    public String getOperator() {
        return operator;
    }

    public Value getLeft() {
        return left;
    }

    public Value getRight() {
        return right;
    }
}
