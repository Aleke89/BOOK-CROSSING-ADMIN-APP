package kz.incubator.mss.reads.groups_menu.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import kz.incubator.mss.reads.R;
import kz.incubator.mss.reads.groups_menu.module.User;


public class EditUser extends AppCompatActivity {
    Toolbar toolbar;
    CircleImageView putPhoto;
    File file;
    Uri fileUri;
    Button saveUser;
    TextView changeIt;
    DatabaseReference databaseReference;
    String version;
    ProgressBar progressBar;
    TextView expDate;
    private static final int PERMISSION_REQUEST_CODE = 200;
    boolean photoSelected = false;
    StorageReference storageReference;
    String uName;
    User user;

    CircleImageView userPhoto;
    String photoUrl;
    CardView changePhoto;

    String downloadUri;
    String imgStorageName;
    EditText nameOfUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_user);
        initView();

        initIncreaseVersion();

    }


    public void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.edit_user));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        changeIt = findViewById(R.id.changeIt);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        file = null;
        fileUri = null;

        progressBar = findViewById(R.id.ProgressBar);
        progressBar.setVisibility(View.GONE);


        changePhoto = findViewById(R.id.takePhoto);

        saveUser = findViewById(R.id.saveUser);
        userPhoto = findViewById(R.id.userPhoto);
        changeIt = findViewById(R.id.changeIt);

        nameOfUser = findViewById(R.id.nameOfUser);

        Bundle bundle = getIntent().getExtras();
        user = (User) bundle.getSerializable("user");

        if (user != null) {

            nameOfUser.setText(user.getInfo());
            photoUrl = user.getPhoto();

            Glide.with(getApplicationContext())
                    .load(user.getPhoto())
                    .crossFade()
                    .dontAnimate()
                    .placeholder(R.drawable.user_def)
                    .into(userPhoto);
        }

        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission()) {
                    startTakeImage();
                } else {
                    requestPermission();
                }
            }
        });

        saveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUserChanges();
            }
        });
    }

    public void checkUserChanges() {
        uName = nameOfUser.getText().toString();

        if (uName.trim().equals("")) {
            nameOfUser.setError(getString(R.string.fill_info));
            return;
        }

        if (photoSelected) {
            uploadImage();
        } else {
            saveUserChanges();
        }

    }

    public void saveUserChanges() {

        user.setInfo(uName);
        databaseReference.child("user_ver").setValue(getIncreasedVersion());
        databaseReference.child("user_list").child(user.getPhoneNumber()).child("info").setValue(uName);

        saveUser.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(EditUser.this, getString(R.string.user_saved), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user);
        intent.putExtras(bundle);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public void startTakeImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    private String uploadImage() {
        if (fileUri != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getResources().getString(R.string.photoLoading));
            progressDialog.show();
            imgStorageName = UUID.randomUUID().toString();
            final String photoPath = "users/" + imgStorageName;
            final StorageReference ref = storageReference.child(photoPath);
            ref.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            downloadUri = taskSnapshot.getDownloadUrl().toString();

                            if (downloadUri != null) {

                                databaseReference.child("user_list").child(user.getPhoneNumber()).child("photo").setValue(downloadUri);
                                databaseReference.child("user_list").child(user.getPhoneNumber()).child("imgStorageName").setValue(imgStorageName);

                                user.setPhoto(downloadUri);
                                user.setImgStorageName(imgStorageName);

                                saveUserChanges();

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditUser.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage(getString(R.string.uploaded) + (int) progress + "%");
                        }
                    });
        }
        return downloadUri;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                fileUri = result.getUri();
                userPhoto.setImageURI(fileUri);
                userPhoto.setVisibility(View.VISIBLE);
                changeIt.setText(getString(R.string.change_image));

                photoSelected = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "You have been given permission.Now you can use CAMERA.", Toast.LENGTH_SHORT).show();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions to take user image",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermission();
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(EditUser.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void initIncreaseVersion() {
        databaseReference.child("user_ver").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                version = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public int getIncreasedVersion() {
        int ver = Integer.parseInt(version);
        ver += 1;
        return ver;
    }
}
