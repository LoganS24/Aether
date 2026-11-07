import java.lang.reflect.Field;
import java.util.*;

public class Evaluator {

    public void interpret(List<Object> statements) {
        try{
            for (Object statement : statements) {
                evaluate(statement);
            }
        } catch (RuntimeException error) {
            System.err.println(error.getMessage());
        }
    }

    public Object evaluate(Object expr) {
        if (expr == null) return null;

        if (expr instanceof PrintStatement) {
            Object value = evaluate(getPrivateField(expr, "expression"));
            System.out.println(stringify(value));
            return null; // Print statements do not return a value
        }

        if (expr instanceof ExpressionStatement) {
            return evaluate(getPrivateField(expr, "expression"));
        }

        if (expr instanceof Literal) {
            return evaluateLiteral((Literal) expr);
        }

        if (expr instanceof Grouping) {
            return evaluateGrouping((Grouping) expr);
        }

        if (expr instanceof Unary) {
            return evaluateUnary((Unary) expr);
        }

        if (expr instanceof Binary) {
            return evaluateBinary((Binary) expr);
        }

        throw new RuntimeException("Unknown expression type: " + expr.getClass().getName());
    }

    private Object evaluateLiteral(Literal literal) {
        String value = literal.toString();

        if (value.equals("true")) return true;
        if (value.equals("false")) return false;
        if (value.equals("nil")) return null;

        // Try parsing numeric literal
        Double num = coerceToNumber(value);
        if (num != null) return num;

        return value;
    }

    private Object evaluateGrouping(Grouping grouping) {
        return evaluate(getPrivateField(grouping, "expression"));
    }

    private Object evaluateUnary(Unary unary) {
        Token operator = (Token) getPrivateField(unary, "operator");
        Object right = evaluate(getPrivateField(unary, "right"));

        switch (operator.getType()) {
            case MINUS:
                Double numRight = coerceToNumber(right);
                if (numRight == null) {
                    throw new RuntimeException("[line " + operator.getLine() + "] Operand must be a number or numeric string.");
                }
                return -numRight;
            case BANG:
                return !isTruthy(right);
            default:
                break;
        }

        return null;
    }

    private Object evaluateBinary(Binary binary) {
        Object rawLeft = evaluate(getPrivateField(binary, "Left"));
        Token operator = (Token) getPrivateField(binary, "Operator");
        Object rawRight = evaluate(getPrivateField(binary, "Right"));

        // Attempt numeric coercion on operands
        Double leftNum = coerceToNumber(rawLeft);
        Double rightNum = coerceToNumber(rawRight);

        switch (operator.getType()) {
            // Relational comparisons (coerce to numeric comparison if possible)
            case GREATER:
                checkNumericCoercion(operator, leftNum, rightNum);
                return leftNum > rightNum;
            case GREATER_EQUAL:
                checkNumericCoercion(operator, leftNum, rightNum);
                return leftNum >= rightNum;
            case LESS:
                checkNumericCoercion(operator, leftNum, rightNum);
                return leftNum < rightNum;
            case LESS_EQUAL:
                checkNumericCoercion(operator, leftNum, rightNum);
                return leftNum <= rightNum;

            // Equality
            case BANG_EQUAL:
                return !isEqual(rawLeft, rawRight);
            case EQUAL_EQUAL:
                return isEqual(rawLeft, rawRight);

            // Arithmetic Operations
            case MINUS:
                checkNumericCoercion(operator, leftNum, rightNum);
                return leftNum - rightNum;
            case STAR:
                checkNumericCoercion(operator, leftNum, rightNum);
                return leftNum * rightNum;
            case SLASH:
                checkNumericCoercion(operator, leftNum, rightNum);
                if (rightNum == 0.0) {
                    throw new RuntimeException("[line " + operator.getLine() + "] Division by zero.");
                }
                return leftNum / rightNum;

            case PLUS:
                // Coerce to numeric addition if both can be converted to numbers
                if (leftNum != null && rightNum != null) {
                    return leftNum + rightNum;
                }
                // Otherwise fall back to String concatenation
                return stringify(rawLeft) + stringify(rawRight);

            default:
                break;
        }

        return null;
    }

    // Attempt to convert objects (including numeric Strings) into Double
    private Double coerceToNumber(Object object) {
        if (object == null) return null;
        if (object instanceof Double) return (Double) object;
        if (object instanceof Integer) return ((Integer) object).doubleValue();

        if (object instanceof String) {
            try {
                return Double.parseDouble((String) object);
            } catch (NumberFormatException e) {
                return null; // Non-numeric string
            }
        }
        return null;
    }

    // Truthiness with Coercion
    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (Boolean) object;

        // Coerce string numbers or numbers
        Double num = coerceToNumber(object);
        if (num != null) {
            return num != 0.0;
        }

        // Non-numeric string coercion rules
        if (object instanceof String) {
            return !((String) object).isEmpty();
        }

        return true;
    }

    // Equality with numeric coercion check
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        Double numA = coerceToNumber(a);
        Double numB = coerceToNumber(b);

        // If both coerce to numbers, compare numerically (e.g., "5" == 5)
        if (numA != null && numB != null) {
            return numA.equals(numB);
        }

        return a.equals(b);
    }

    private void checkNumericCoercion(Token operator, Double left, Double right) {
        if (left != null && right != null) return;
        throw new RuntimeException("[line " + operator.getLine() + "] Operands must be numbers or numeric strings.");
    }

    public String stringify(Object object) {
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

    private Object getPrivateField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access field '" + fieldName + "' on " + target.getClass().getName(), e);
        }
    }
}