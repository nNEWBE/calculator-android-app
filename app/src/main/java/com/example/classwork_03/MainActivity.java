package com.example.classwork_03;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView resultTextView, expressionTextView;
    MaterialButton logoutButton;
    ConstraintLayout mainLayout;

    private String firstOperand = "";
    private String secondOperand = "";
    private String operator = "";
    private boolean isNewCalculation = true;

    private Handler cursorHandler;
    private boolean isCursorVisible = false;
    private final String CURSOR_CHAR = "|";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MainActivity.this, "Logout successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        resultTextView = findViewById(R.id.resultTextView);
        expressionTextView = findViewById(R.id.expressionTextView);
        mainLayout = findViewById(R.id.main);

        setupButtonListeners();

        cursorHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBlinkingCursor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBlinkingCursor();
    }

    private void setupButtonListeners() {
        int[] buttonIds = { R.id.button_c, R.id.button_plus_minus, R.id.button_percent, R.id.button_divide,
                R.id.button_7, R.id.button_8, R.id.button_9, R.id.button_multiply,
                R.id.button_4, R.id.button_5, R.id.button_6, R.id.button_subtract,
                R.id.button_1, R.id.button_2, R.id.button_3, R.id.button_add,
                R.id.button_dot, R.id.button_0, R.id.button_backspace, R.id.button_equals };
        for (int id : buttonIds) {
            findViewById(id).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        MaterialButton button = (MaterialButton) view;
        String buttonText = button.getText().toString();

        if (!buttonText.equals("=") && !buttonText.equals("C")) {
            startBlinkingCursor();
        }

        switch (buttonText) {
            case "C":
                clearAll();
                break;
            case "=":
                handleEquals();
                break;
            case "+": case "-": case "x": case "÷":
                handleOperator(buttonText);
                break;
            case ".":
                handleDecimal();
                break;
            case "⌫":
                handleBackspace();
                break;
            case "%":
                handlePercent();
                break;
            case "+/-":
                handlePlusMinus();
                break;
            default:
                handleNumber(buttonText);
                break;
        }
    }

    private final Runnable cursorRunnable = new Runnable() {
        @Override
        public void run() {
            isCursorVisible = !isCursorVisible;
            updateDisplay();
            cursorHandler.postDelayed(this, 500);
        }
    };

    private void startBlinkingCursor() {
        cursorHandler.removeCallbacks(cursorRunnable);
        cursorHandler.post(cursorRunnable);
    }

    private void stopBlinkingCursor() {
        cursorHandler.removeCallbacks(cursorRunnable);
        isCursorVisible = false;
        updateDisplay();
    }

    private void handleNumber(String number) {
        if (isNewCalculation) {
            firstOperand = "";
            isNewCalculation = false;
        }

        if (operator.isEmpty()) {
            firstOperand += number;
        } else {
            secondOperand += number;
        }
        updateLiveResult();
    }

    private void handleOperator(String op) {
        if (!firstOperand.isEmpty()) {
            if (!secondOperand.isEmpty()) {
                calculateLiveResult(); // Calculate intermediate result first
            }
            operator = op;
            isNewCalculation = false;
        }
    }

    private void handleDecimal() {
        if (operator.isEmpty()) {
            if (!firstOperand.contains(".")) firstOperand += ".";
        } else {
            if (!secondOperand.contains(".")) secondOperand += ".";
        }
        updateLiveResult();
    }

    private void handleBackspace() {
        if (!secondOperand.isEmpty()) {
            secondOperand = secondOperand.substring(0, secondOperand.length() - 1);
        } else if (!operator.isEmpty()) {
            operator = "";
        } else if (!firstOperand.isEmpty()) {
            firstOperand = firstOperand.substring(0, firstOperand.length() - 1);
        }
        updateLiveResult();
    }

    private void handlePercent() {
        if (!firstOperand.isEmpty()) {
            try {
                double value = Double.parseDouble(firstOperand) / 100;
                firstOperand = String.valueOf(value);
                isNewCalculation = true;
                updateLiveResult();
            } catch (NumberFormatException e) { /* Ignore */ }
        }
    }

    private void handlePlusMinus() {
        String targetOperand = secondOperand.isEmpty() ? firstOperand : secondOperand;
        if (!targetOperand.isEmpty()) {
            if (targetOperand.startsWith("-")) {
                targetOperand = targetOperand.substring(1);
            } else {
                targetOperand = "-" + targetOperand;
            }

            if (secondOperand.isEmpty()) {
                firstOperand = targetOperand;
            } else {
                secondOperand = targetOperand;
            }
            updateLiveResult();
        }
    }

    private void handleEquals() {
        if (firstOperand.isEmpty() || secondOperand.isEmpty() || operator.isEmpty()) {
            return;
        }
        stopBlinkingCursor();
        String finalResult = calculate(firstOperand, operator, secondOperand);
        if (finalResult != null) {
            animateResultToExpression(finalResult);
        }
    }

    private void updateLiveResult() {
        if (operator.isEmpty()) {
            resultTextView.setText(firstOperand.isEmpty() ? "0" : firstOperand);
        } else {
            if (!secondOperand.isEmpty()) {
                String liveResult = calculate(firstOperand, operator, secondOperand);
                if (liveResult != null) {
                    resultTextView.setText(liveResult);
                }
            }
        }
    }

    private void calculateLiveResult() {
        if (!firstOperand.isEmpty() && !secondOperand.isEmpty() && !operator.isEmpty()) {
            String result = calculate(firstOperand, operator, secondOperand);
            if (result != null) {
                firstOperand = result;
                secondOperand = "";
            }
        }
    }

    private String calculate(String op1, String opr, String op2) {
        try {
            double num1 = Double.parseDouble(op1);
            double num2 = Double.parseDouble(op2);
            double result = 0;

            switch (opr) {
                case "+": result = num1 + num2; break;
                case "-": result = num1 - num2; break;
                case "x": result = num1 * num2; break;
                case "÷":
                    if (num2 == 0) return "Error";
                    result = num1 / num2;
                    break;
            }

            if (result == (long) result) {
                return String.format("%d", (long) result);
            } else {
                return String.valueOf(result);
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void clearAll() {
        firstOperand = "";
        secondOperand = "";
        operator = "";
        resultTextView.setText("");
        expressionTextView.setText("");
        isNewCalculation = true;
        startBlinkingCursor();
    }

    private void updateDisplay() {
        String expression = firstOperand + operator + secondOperand;
        if (isCursorVisible && !isNewCalculation) {
            expressionTextView.setText(expression + CURSOR_CHAR);
        } else {
            expressionTextView.setText(expression + " ");
        }
    }

    private void animateResultToExpression(String finalResult) {
        final TextView animationView = new TextView(this);
        animationView.setText(finalResult);
        animationView.setTextSize(40);
        animationView.setTextColor(resultTextView.getCurrentTextColor());
        animationView.setGravity(resultTextView.getGravity());
        
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        animationView.setLayoutParams(params);
        mainLayout.addView(animationView);

        animationView.post(() -> {
            resultTextView.setVisibility(View.INVISIBLE);
            expressionTextView.setVisibility(View.INVISIBLE);
            
            float startX = resultTextView.getX() + (resultTextView.getWidth() - animationView.getWidth());
            float startY = resultTextView.getY();
            float endX = expressionTextView.getX() + (expressionTextView.getWidth() - animationView.getWidth());
            float endY = expressionTextView.getY();

            animationView.setX(startX);
            animationView.setY(startY);
            
            ObjectAnimator animX = ObjectAnimator.ofFloat(animationView, "x", startX, endX);
            ObjectAnimator animY = ObjectAnimator.ofFloat(animationView, "y", startY, endY);

            AnimatorSet animSet = new AnimatorSet();
            animSet.playTogether(animX, animY);
            animSet.setDuration(400);

            animSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    expressionTextView.setText(finalResult);
                    expressionTextView.setVisibility(View.VISIBLE);
                    
                    mainLayout.removeView(animationView);
                    resultTextView.setText("");
                    resultTextView.setVisibility(View.VISIBLE);
                    
                    firstOperand = finalResult;
                    secondOperand = "";
                    operator = "";
                    isNewCalculation = true;
                }
            });
            animSet.start();
        });
    }
}
