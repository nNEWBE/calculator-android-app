package com.example.classwork_03;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    MaterialTextView alreadyHaveAccount;
    MaterialButton registerButton;
    TextInputEditText registerUsername,registerEmail,registerPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        alreadyHaveAccount = findViewById(R.id.alreadyHaveAccount);
        registerButton = findViewById(R.id.registerButton);
        registerUsername = findViewById(R.id.registerUsername);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);

        mAuth = FirebaseAuth.getInstance();

        alreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = String.valueOf(registerUsername.getText());
                String email = String.valueOf(registerEmail.getText());
                String password = String.valueOf(registerPassword.getText());

                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(RegisterActivity.this, "Registration successful.",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(user);
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }
                            }
                        });
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            registerUsername.setText("");
            registerEmail.setText("");
            registerPassword.setText("");
        }
    }
}
