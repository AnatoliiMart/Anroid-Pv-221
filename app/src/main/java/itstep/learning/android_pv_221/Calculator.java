package itstep.learning.android_pv_221;

public class Calculator {
    private double result;
    private double currentInput;
    private String operator;

    public void reset() {
        result = 0;
        currentInput = 0;
        operator = null;
    }

    public void inputDigit(double input) {
        currentInput = input;
    }

    public void setOperator(String op) {
        operator = op;
    }

    public void calculate(double input) {
        switch (operator) {
            case "+":
                result += currentInput + input;
                break;
            case "-":
                result += currentInput - input;
                break;
            case "×":
                result += currentInput * input;
                break;
            case "÷":
                if (input == 0) {
                    throw new ArithmeticException("Cannot divide by zero.");
                }
                result += currentInput / input;
                break;
            default:
                result = input;
                break;
        }
    }

    public double getResult() {
        return result;
    }

    public void applySquare() {
        result = Math.pow(currentInput, 2);
    }

    public void applySquareRoot() {
        result = Math.sqrt(currentInput);
    }

    public void applyInverse() {
        if (currentInput != 0) {
            result = 1 / currentInput;
        } else {
            throw new ArithmeticException("Cannot divide by zero.");
        }
    }

    public void applyPercent() {
        result = currentInput / 100;
    }

    public void changeSign() {
        currentInput = -currentInput;
        result = currentInput;
    }
}
