package mihailris.oiscript.parsing;

import mihailris.oiscript.*;

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
            case "in": {
                if (rightValue instanceof Collection) {
                    Collection<?> collection = (Collection<?>) rightValue;
                    return collection.contains(leftValue);
                }
                if (rightValue instanceof String) {
                    String string = (String) rightValue;
                    return string.contains(String.valueOf(leftValue));
                }
                if (rightValue instanceof Range) {
                    Range range = (Range) rightValue;
                    long value = ((Number) leftValue).longValue();
                    return range.contains(value);
                }
                if (rightValue instanceof OiObject) {
                    OiObject object = (OiObject) rightValue;
                    return object.has(leftValue);
                }
            }
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
                return new BinaryOperator(new BinaryOperator(left, operator, rightBinOp.left), rightBinOp.operator, rightBinOp.right);
            }
        }
        return this;
    }
}
