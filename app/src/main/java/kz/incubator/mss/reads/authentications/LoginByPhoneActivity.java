package kz.incubator.mss.reads.authentications;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import kz.incubator.mss.reads.MenuActivity;
import kz.incubator.mss.reads.R;

public class LoginByPhoneActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth mAuth;
    Button btnLogin;
    TextView btnLoginByEmail;
    EditText phoneNumber;
    ProgressBar progressBarLogin;
    LinearLayout loginLayout;
    TextInputLayout inputLayoutEmail;
    String phoneNumberStr;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_by_phone);

        checkInternetConnection();
        initWidgets();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail().contains("admin")) {
            Intent intent = new Intent(LoginByPhoneActivity.this, MenuActivity.class);
            startActivity(intent);
        }

        btnLogin.setOnClickListener(this);
        btnLoginByEmail.setOnClickListener(this);

    }

    public void initWidgets() {
        btnLogin = findViewById(R.id.btnLogin);
        btnLoginByEmail = findViewById(R.id.btnLoginByEmail);

        progressBarLogin = findViewById(R.id.progressBarLogin);
        phoneNumber = findViewById(R.id.phoneNumber);
        loginLayout = findViewById(R.id.loginLayout);
        inputLayoutEmail = findViewById(R.id.inputLayoutEmail);

        phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int numberLength = phoneNumber.getText().toString().length();

                if (numberLength == 12) {
                    btnLogin.setBackground(ContextCompat.getDrawable(LoginByPhoneActivity.this, R.drawable.border2));
                } else {
                    btnLogin.setBackground(ContextCompat.getDrawable(LoginByPhoneActivity.this, R.drawable.border_grey));
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:

                progressVisible(true);
                phoneNumberStr = phoneNumber.getText().toString();

                if (checkInternetConnection()) {

                    Intent verifyActivity = new Intent(LoginByPhoneActivity.this, VerifyCodeSentActivity.class);
                    verifyActivity.putExtra("phoneNumber", phoneNumberStr);
                    startActivity(verifyActivity);

                }

                break;

            case R.id.btnLoginByEmail:
                startActivity(new Intent(LoginByPhoneActivity.this, LoginByEmailPage.class));

                break;
        }

    }

    public void progressVisible(boolean yes) {
        if (yes) {

            progressBarLogin.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.GONE);

        } else {

            progressBarLogin.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);

        }
    }

    public void logInAdmin() {
        String email = "admin@reading.club";
        String password = "123456";

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginByPhoneActivity.this, MenuActivity.class));

                        } else {
                            Log.w("LoginByPhoneActivity", "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginByPhoneActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }




    public boolean checkInternetConnection() {
        if (isNetworkAvailable()) {
            return true;
        }

        Toast.makeText(this, getString(R.string.inetConnection), Toast.LENGTH_SHORT).show();

        progressBarLogin.setVisibility(View.GONE);
        btnLogin.setVisibility(View.VISIBLE);

        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }
}
