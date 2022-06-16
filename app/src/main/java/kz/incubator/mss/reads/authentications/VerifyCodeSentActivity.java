package kz.incubator.mss.reads.authentications;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import kz.incubator.mss.reads.MenuActivity;
import kz.incubator.mss.reads.R;

public class VerifyCodeSentActivity extends AppCompatActivity implements View.OnClickListener {

    String TAG = "VerifyCodeSentActivity";
    FirebaseAuth mAuth;
    String mVerificationId;
    TextView verifyPhone;
    Button btnVerify;
    EditText verifyCode;
    String phoneNumber;
    ProgressBar btnVerifyProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code_sent);
        mAuth = FirebaseAuth.getInstance();
        verifyPhone = findViewById(R.id.verifyPhone);
        verifyCode = findViewById(R.id.verifyCode);
        btnVerify = findViewById(R.id.btnVerify);
        btnVerifyProgress = findViewById(R.id.btnVerifyProgress);
        btnVerify.setOnClickListener(this);

        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");
        verifyPhone.setText(getString(R.string.code_sent) + phoneNumber);

        phoneVerification(phoneNumber);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnVerify:
                btnVerifyProgress.setVisibility(View.VISIBLE);
                btnVerify.setVisibility(View.GONE);

                String code = verifyCode.getText().toString().trim();

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                signInWithPhoneAuthCredential(credential);

                break;
        }
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.


            Log.i(TAG, "onVerificationCompleted:" + credential);
            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.i(TAG, "onVerificationFailed", e);
            Toast.makeText(VerifyCodeSentActivity.this, "onVerificationFailed" + e, Toast.LENGTH_SHORT).show();

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(VerifyCodeSentActivity.this, getString(R.string.incorrect_phone_number), Toast.LENGTH_SHORT).show();

            } else if (e instanceof FirebaseTooManyRequestsException) {

            }
        }

        @Override
        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.

            Log.i(TAG, "onCodeSent:" + verificationId);
            Log.i(TAG, "token:" + token);


            mVerificationId = verificationId;
        }
    };

    public void phoneVerification(String phoneNumber) {
        Log.i(TAG, "phoneVerification");

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerifyCodeSentActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d(TAG, "signInWithCredential:success");
                            Intent menuActivity = new Intent(VerifyCodeSentActivity.this, MenuActivity.class);
                            menuActivity.putExtra("userLoginType", "phone");
                            startActivity(menuActivity);

                        } else {

                            btnVerifyProgress.setVisibility(View.GONE);
                            btnVerify.setVisibility(View.VISIBLE);

                            Toast.makeText(VerifyCodeSentActivity.this, getString(R.string.incorrect_pin_number), Toast.LENGTH_SHORT).show();

                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            }
                        }
                    }
                });
    }

}
