package kz.incubator.mss.reads.authentications;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import kz.incubator.mss.reads.MenuActivity;
import kz.incubator.mss.reads.R;

public class LoginByEmailPage extends AppCompatActivity implements View.OnClickListener {
    Button btnLogin;
    EditText email;
    EditText password;
    FirebaseAuth auth;
    ProgressBar progressBar;
    TextView reset;
    FirebaseAuth mAuth;

    @Nullable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        initWidgets();
        auth = FirebaseAuth.getInstance();
        btnLogin.setOnClickListener(this);
        reset.setOnClickListener(this);
    }

    public void initWidgets() {
        btnLogin = findViewById(R.id.btnLogin);
        email = findViewById(R.id.emailToLogin);
        password = findViewById(R.id.passwordToLogin);
        progressBar = findViewById(R.id.progressBarForLogin);
        reset = findViewById(R.id.forgotPassword);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                final String emails = email.getText().toString();
                String passwords = password.getText().toString();
                progressBar.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.GONE);

                if (TextUtils.isEmpty(emails) || TextUtils.isEmpty(passwords)) {

                    Snackbar.make(btnLogin, getString(R.string.fill_all), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setVisibility(View.VISIBLE);

                } else {
                    if (emails.contains("admin")) {
                        auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {

                                            Intent intent = new Intent(LoginByEmailPage.this, MenuActivity.class);
                                            startActivity(intent);

                                        } else {

                                            String subbed = getString(R.string.login_or_pass_error);
                                            Snackbar.make(btnLogin, subbed, Toast.LENGTH_SHORT).setActionTextColor(getResources().getColor(R.color.red)).show();
                                            progressBar.setVisibility(View.GONE);
                                            btnLogin.setVisibility(View.VISIBLE);

                                        }
                                    }
                                });
                    }else{
                        String subbed = getString(R.string.admin_email_error);
                        Snackbar.make(btnLogin, subbed, Toast.LENGTH_SHORT).setActionTextColor(getResources().getColor(R.color.red)).show();
                    }
                }
                break;

            case R.id.forgotPassword:
                AlertDialog.Builder alert = new AlertDialog.Builder(LoginByEmailPage.this);
                View second_view = getLayoutInflater().inflate(R.layout.forgot_password, null);
                alert.setView(second_view);
                final AlertDialog dialog = alert.create();
                dialog.show();
                final Button btn = second_view.findViewById(R.id.resetButton);
                final EditText email = second_view.findViewById(R.id.emailReset);
                final ProgressBar progressBarFor = second_view.findViewById(R.id.progressForReset);
                progressBarFor.setVisibility(View.GONE);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressBarFor.setVisibility(View.VISIBLE);
                        btn.setVisibility(View.GONE);
                        if (TextUtils.isEmpty(email.getText().toString())) {
                            Snackbar.make(btn, getString(R.string.fill_all), Snackbar.LENGTH_SHORT).show();
                            progressBarFor.setVisibility(View.GONE);
                            btn.setVisibility(View.VISIBLE);
                        } else {
                            auth.sendPasswordResetEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Snackbar.make(btn, getString(R.string.send_reset_password), Toast.LENGTH_LONG).show();
                                        dialog.dismiss();
                                    } else {
                                        Snackbar.make(btn, getString(R.string.railed_send_reset_email), Toast.LENGTH_SHORT).show();
                                        progressBarFor.setVisibility(View.GONE);
                                        btn.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                    }
                });
                break;
        }
    }
}
