package kz.incubator.mss.reads.authentications;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import kz.incubator.mss.reads.MenuActivity;
import kz.incubator.mss.reads.R;

public class RegisterPage extends Fragment {
    View view;
    EditText usernames;
    EditText emails;
    EditText passwords;
    EditText confirms;
    Button btnSignUp;
    FirebaseAuth auth;
    ProgressBar progressBar;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = LayoutInflater.from(container.getContext()).inflate(R.layout.register_pager,container,false);

        initWidgets();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = emails.getText().toString().trim();
                final String password = passwords.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getActivity().getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getActivity().getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                else if (password.length() < 6) {
                    Toast.makeText(getActivity().getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(!password.equals(confirms.getText().toString())){
                    Toast.makeText(getActivity().getApplicationContext(), "Password is not same", Toast.LENGTH_SHORT).show();
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    btnSignUp.setVisibility(View.GONE);
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(getActivity(), "Authentication failed." + task.getException(),
                                                Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                        btnSignUp.setVisibility(View.VISIBLE);
                                    } else {
                                        FirebaseUser user = auth.getCurrentUser();
                                        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(usernames.getText().toString())
                                                .setPhotoUri(Uri.parse("https://s3.amazonaws.com/37assets/svn/1065-IMG_2529.jpg"))
                                                .build();
                                        if(user != null){
                                            HashMap<String,String> user_hash = new HashMap<>();
                                            user_hash.put("username",usernames.getText().toString());
                                            user_hash.put("email",email);
                                            user_hash.put("image","https://s3.amazonaws.com/37assets/svn/1065-IMG_2529.jpg");
                                            user_hash.put("password",password);
                                            String key = FirebaseDatabase.getInstance().getReference().child("users").push().getKey();
                                            user_hash.put("key",key);
                                            FirebaseDatabase.getInstance().getReference().child("users").child(key).setValue(user_hash);
                                            user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    startActivity(new Intent(getActivity(), MenuActivity.class));
                                                }
                                            });
                                        }

                                    }
                                }
                            });
                }

            }
        });
        return view;
    }

    public void initWidgets() {
        usernames = view.findViewById(R.id.usernameToRegister);
        emails = view.findViewById(R.id.emailToRegister);
        passwords = view.findViewById(R.id.passwordToRegister);
        confirms = view.findViewById(R.id.confirmPassword);
        btnSignUp = view.findViewById(R.id.btnRegister);
        auth = FirebaseAuth.getInstance();
        progressBar = view.findViewById(R.id.progressBarForRegister);
    }
}
