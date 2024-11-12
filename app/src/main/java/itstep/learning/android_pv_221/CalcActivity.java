package itstep.learning.android_pv_221;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Objects;

public class CalcActivity extends AppCompatActivity {
    private static final int MAX_DIGITS = 11;
    private String zeroSign;
    private TextView tvResult;
    private TextView tvHistory;
    private Calculator calculator;
    private String currentOperation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calc);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvResult = findViewById(R.id.calc_tv_result);
        tvHistory = findViewById(R.id.calc_tv_history);
        zeroSign = getString(R.string.calc_btn_digit_0);
        calculator = new Calculator();

        initializeButtons();
        btnClickC(null);
    }

    private void initializeButtons() {
        for (int i = 0; i < 10; i++) {
            String btnIdName = "calc_btn_digit_" + i;
            @SuppressLint("DiscouragedApi")
            int btnId = getResources().getIdentifier(btnIdName, "id", getPackageName());
            findViewById(btnId).setOnClickListener(this::btnClickDigit);
        }

        findViewById(R.id.calc_btn_c).setOnClickListener(this::btnClickC);
        findViewById(R.id.calc_btn_ce).setOnClickListener(this::btnClickCE);
        findViewById(R.id.calc_btn_backspace).setOnClickListener(this::btnClickBackspace);
        findViewById(R.id.calc_btn_plus).setOnClickListener(this::btnClickOperator);
        findViewById(R.id.calc_btn_minus).setOnClickListener(this::btnClickOperator);
        findViewById(R.id.calc_btn_multiply).setOnClickListener(this::btnClickOperator);
        findViewById(R.id.calc_btn_divide).setOnClickListener(this::btnClickOperator);
        findViewById(R.id.calc_btn_equal).setOnClickListener(this::btnClickEqual);
        findViewById(R.id.calc_btn_percent).setOnClickListener(this::btnClickPercent);
        findViewById(R.id.calc_btn_square).setOnClickListener(this::btnClickSquare);
        findViewById(R.id.calc_btn_sqrt).setOnClickListener(this::btnClickSqrt);
        findViewById(R.id.calc_btn_inverse).setOnClickListener(this::btnClickInverse);
        findViewById(R.id.calc_btn_pm).setOnClickListener(this::btnClickChangeSign);
        findViewById(R.id.calc_btn_comma).setOnClickListener(this::btnClickComma);
    }

    private void btnClickDigit(View view) {
        try {
            String resText = tvResult.getText().toString();
            String buttonText = ((Button) view).getText().toString();

            if (buttonText.equals(zeroSign)) {
                buttonText = zeroSign;
            }
            if (resText.equals(zeroSign)) {
                resText = "";
            }

            if (resText.length() >= MAX_DIGITS) {
                Toast.makeText(this, R.string.calc_msg_too_long, Toast.LENGTH_SHORT).show();
                return;
            }

            resText += buttonText;
            tvResult.setText(resText);
        } catch (Exception e) {
            handleError("Error processing digit input.");
        }
    }

    private void btnClickOperator(View view) {
        try {
            String resText = tvResult.getText().toString().replace(zeroSign, "0");
            if (resText.isEmpty()) return;

            double input = evaluate(resText);
            calculator.inputDigit(input);

            String operator = ((Button) view).getText().toString();
            calculator.setOperator(operator);

            currentOperation = resText + " " + operator;
            updateHistory(currentOperation);

            tvResult.setText(zeroSign);
        } catch (NumberFormatException e) {
            handleError("Invalid number format.");
        } catch (Exception e) {
            handleError("Error processing operator.");
        }
    }

    private void btnClickEqual(View view) {
        try {
            String resText = tvResult.getText().toString().replace(zeroSign, "0");
            if (resText.isEmpty()) return;

            double input = evaluate(resText);
            calculator.calculate(input);

            double result = calculator.getResult();
            tvResult.setText(formatResult(result));
            updateHistory(currentOperation + " " + resText + " = " + formatResult(result));
            calculator.reset();
            currentOperation = "";
        } catch (ArithmeticException e) {
            handleError("Math error.");
        } catch (NumberFormatException e) {
            handleError("Invalid number format.");
        } catch (Exception e) {
            handleError("Error processing result.");
        }
    }

    private void btnClickC(View view) {
        calculator.reset();
        tvResult.setText(zeroSign);
        tvHistory.setText("");
        currentOperation = "";
    }

    private void btnClickCE(View view) {
        tvResult.setText(zeroSign);
    }

    private void btnClickComma(View view) {
        String resText = tvResult.getText().toString();
        if (!resText.contains(",")) {
            resText += ",";
            tvResult.setText(resText);
        }
    }

    private void btnClickBackspace(View view) {
        try {
            String resText = tvResult.getText().toString();
            if (resText.length() > 1) {
                resText = resText.substring(0, resText.length() - 1);
            } else {
                resText = zeroSign;
            }
            tvResult.setText(resText);
        } catch (Exception e) {
            handleError("Error processing backspace.");
        }
    }

    private void btnClickPercent(View view) {
        try {
            if (calculator.getResult() == 0) {
                calculator.inputDigit(evaluate(tvResult.getText().toString().replace(zeroSign, "0")));
            }
            calculator.applyPercent();
            updateResult();
            calculator.reset();
        } catch (Exception e) {
            handleError("Error processing percent.");
        }
    }

    private void btnClickSquare(View view) {
        try {
            if (calculator.getResult() == 0) {
                calculator.inputDigit(evaluate(tvResult.getText().toString().replace(zeroSign, "0")));
            }
            calculator.applySquare();
            updateResult();
            calculator.reset();
        } catch (Exception e) {
            handleError("Error processing square.");
        }
    }

    private void btnClickSqrt(View view) {
        try {
            if (calculator.getResult() == 0) {
                calculator.inputDigit(evaluate(tvResult.getText().toString().replace(zeroSign, "0")));
            }
            calculator.applySquareRoot();
            updateResult();
            calculator.reset();
        } catch (ArithmeticException e) {
            handleError("Cannot calculate square root of negative number.");
        } catch (Exception e) {
            handleError("Error processing square root.");
        }
    }

    private void btnClickInverse(View view) {
        try {
            if (calculator.getResult() == 0) {
                calculator.inputDigit(evaluate(tvResult.getText().toString().replace(zeroSign, "0")));
            }
            calculator.applyInverse();
            updateResult();
            calculator.reset();
        } catch (ArithmeticException e) {
            handleError("Cannot divide by zero.");
        } catch (Exception e) {
            handleError("Error processing inverse.");
        }
    }

    private void btnClickChangeSign(View view) {
        try {
            if (calculator.getResult() == 0) {
                calculator.inputDigit(evaluate(tvResult.getText().toString().replace(zeroSign, "0")));
            }
            calculator.changeSign();
            updateResult();
            calculator.reset();
        } catch (Exception e) {
            handleError("Error changing sign.");
        }
    }

    private void updateResult() {
        try {
            tvResult.setText(formatResult(calculator.getResult()).replace("0", zeroSign));
        } catch (Exception e) {
            handleError("Error updating result.");
        }
    }

    private void updateHistory(String operation) {
        tvHistory.setText(operation.replace("0", zeroSign));
    }

    private String formatResult(double result) {
        String resultStr;

        if (Math.abs(result) >= 1e10 || (Math.abs(result) > 0 && Math.abs(result) < 1e-3)) {
            resultStr = String.format("%.8e", result);

            if (resultStr.length() > MAX_DIGITS) {
                String[] parts = resultStr.split("e");
                String coefficient = parts[0];
                String exponent = parts.length > 1 ? parts[1] : "";

                if (coefficient.length() > MAX_DIGITS - 3) {
                    coefficient = coefficient.substring(0, MAX_DIGITS - 3);
                }

                resultStr = coefficient + "e" + exponent;
            }
        } else {
            resultStr = BigDecimal.valueOf(result)
                    .setScale(MAX_DIGITS - 1, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString().replace("0", zeroSign).replace(".", ",");
        }

        return resultStr.length() > MAX_DIGITS ? resultStr.substring(0, MAX_DIGITS) : resultStr;
    }

    private void handleError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        btnClickC(null);
        tvHistory.setText("Error");
    }

    private double evaluate(String value) {
        try {
            return Objects.requireNonNull(NumberFormat.getInstance(Locale.ROOT).parse(value)).doubleValue();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
