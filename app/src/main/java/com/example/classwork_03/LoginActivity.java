package com.example.classwork_03;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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

import javax.annotation.Nonnull;


public class LoginActivity extends AppCompatActivity {
    MaterialTextView dontHaveAccount;
    MaterialButton loginButton;
    TextInputEditText emailInputLogin;
    TextInputEditText passInputLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();
        emailInputLogin = findViewById(R.id.emailInputLogin);
        passInputLogin = findViewById(R.id.passInputLogin);
        loginButton = findViewById(R.id.loginButton);
        dontHaveAccount = findViewById(R.id.dontHaveAccount);

        
        dontHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = String.valueOf(emailInputLogin.getText());
                String password = String.valueOf(passInputLogin.getText());

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(LoginActivity.this, "Login successful.",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(user);
                                } else {
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
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
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    updateUI(currentUser);
                } else {
                    Log.e(TAG, "Error reloading user", task.getException());
                }
            });
        }
    }
}