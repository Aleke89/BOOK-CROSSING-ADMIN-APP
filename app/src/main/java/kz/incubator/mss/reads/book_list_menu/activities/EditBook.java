package kz.incubator.mss.reads.book_list_menu.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.Date;
import java.util.UUID;

import kz.incubator.mss.reads.R;
import kz.incubator.mss.reads.book_list_menu.module.Book;


public class EditBook extends AppCompatActivity {
    Toolbar toolbar;
    CardView cardView;
    private final int BOOK_QR_SCANNER = 101;
    StorageReference storageReference;
    ImageView bookImage;
    File file;
    Uri fileUri;
    Button saveBook, bookQrCode;
    TextView changeIt;
    EditText bookName, bookAuthor, bookDesc, bookPNumber, bookCount;
    private static final int PERMISSION_REQUEST_CODE = 200;
    DatabaseReference databaseReference;
    String version;
    ProgressBar progressBar;
    Uri last_file_uri;
    boolean book_scannered = false;
    String book_qr_code = "empty";
    Book book;
    String bId;
    String downloadUri;
    boolean photoSelected = false;
    String bRating;
    String bReserved;
    String imgStorageName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_book);
        initView();
        increaseVersion();

    }

    public void initView() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.edit_book));
        databaseReference = FirebaseDatabase.getInstance().getReference();

        changeIt = findViewById(R.id.changeIt);

        cardView = findViewById(R.id.takePhoto);
        bookImage = findViewById(R.id.bookImage);
        saveBook = findViewById(R.id.saveBook);
        bookQrCode = findViewById(R.id.bookQrCode);

        bookName = findViewById(R.id.bookName);
        bookAuthor = findViewById(R.id.bookAuthor);
        bookDesc = findViewById(R.id.bookDesc);
        bookPNumber = findViewById(R.id.bookPNumber);
        bookCount = findViewById(R.id.bookCount);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        storageReference = FirebaseStorage.getInstance().getReference();

        Bundle bundle = getIntent().getExtras();
        book = (Book) bundle.getSerializable("book");

        if (bundle != null && book != null) {

            bId = book.getFirebaseKey();
            String bName = book.getName();
            String bAuthor = book.getAuthor();
            String bDesc = book.getDesc();
            int bPage_number = book.getPage_number();
            bRating = book.getRating();
            bReserved = book.getReserved();
            book_qr_code = book.getQr_code();
            imgStorageName = book.getImgStorageName();

            Glide.with(getApplicationContext())
                    .load(book.getPhoto())
                    .placeholder(R.drawable.item_book)
                    .into(bookImage);

            bookName.setText(bName);
            bookAuthor.setText(bAuthor);
            bookDesc.setText(bDesc);
            bookPNumber.setText("" + bPage_number);
            bookQrCode.setText(getString(R.string.scan_code) + book_qr_code);
        }

        file = null;
        fileUri = null;
        photoSelected = false;

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission()) {
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(EditBook.this);
                } else {
                    requestPermission();
                }
            }
        });

        bookQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent t = new Intent(EditBook.this, ScannerActivity.class);
                startActivityForResult(t, BOOK_QR_SCANNER);
            }
        });

        saveBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBookChanges();
            }
        });

    }

    public void checkBookChanges() {
        boolean bookFilled = true;

        if (bookName.getText().toString().trim().equals("")) {
            bookName.setError(getString(R.string.b_fill_name));
            bookFilled = false;
        }
        if (bookAuthor.getText().toString().trim().equals("")) {
            bookAuthor.setError(getString(R.string.b_fill_author));
            bookFilled = false;
        }
        if (bookDesc.getText().toString().trim().equals("")) {
            bookDesc.setError(getString(R.string.b_fill_desc));
            bookFilled = false;
        }
        if (bookPNumber.getText().toString().trim().equals("")) {
            bookPNumber.setError(getString(R.string.b_fill_page_number));
            bookFilled = false;
        }
        if (bookCount.getText().toString().trim().equals("")) {
            bookCount.setError(getString(R.string.b_fill_amount));
            bookFilled = false;
        }

        if(bookFilled) {
            if (photoSelected) {
                uploadImage();
            } else {
                saveBookChanges();
            }
        }

    }

    public void saveBookChanges(){
        String bName = bookName.getText().toString();
        String bAuthor = bookAuthor.getText().toString();
        String bDesc = bookDesc.getText().toString();
        int bPNumber = Integer.parseInt(bookPNumber.getText().toString());

        Book b = new Book(bId, bName, bAuthor, bDesc, bPNumber, bRating, book.getPhoto(), bReserved, book_qr_code, imgStorageName);

        databaseReference.child("book_list_ver").setValue(getIncreasedVersion());
        databaseReference.child("book_list").child(bId).setValue(b);

        saveBook.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(EditBook.this, getString(R.string.book_saved), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("edited_book", b);
        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private String uploadImage() {
        if (fileUri != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getResources().getString(R.string.photoLoading));
            progressDialog.show();

            imgStorageName = UUID.randomUUID().toString();
            final String photoPath = "book_images/" + imgStorageName;
            final StorageReference ref = storageReference.child(photoPath);
            ref.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            downloadUri = taskSnapshot.getDownloadUrl().toString();

                            if (downloadUri != null) {

                                String bName = bookName.getText().toString();
                                String bAuthor = bookAuthor.getText().toString();
                                String bDesc = bookDesc.getText().toString();
                                int bPNumber = Integer.parseInt(bookPNumber.getText().toString());

                                Book b = new Book(bId, bName, bAuthor, bDesc, bPNumber, bRating, downloadUri, bReserved, book_qr_code, imgStorageName);

                                databaseReference.child("book_list_ver").setValue(getIncreasedVersion());
                                databaseReference.child("book_list").child(bId).setValue(b);

                                saveBook.setVisibility(View.GONE);
                                progressBar.setVisibility(View.VISIBLE);
                                Toast.makeText(EditBook.this, getString(R.string.book_saved), Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("edited_book", b);
                                intent.putExtras(bundle);
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditBook.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    public void increaseVersion() {
        databaseReference.child("book_list_ver").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                version = dataSnapshot.getValue().toString();

                if (dataSnapshot.exists()) {
                    version = dataSnapshot.getValue().toString();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public long getIncreasedVersion() {
        long ver = Long.parseLong(version);
        ver += 1;
        return ver;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BOOK_QR_SCANNER) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle bundle = data.getExtras();
                book_qr_code = (String) bundle.getSerializable("book_qr_code");
                bookQrCode.setText(getString(R.string.scan_code) + book_qr_code);
                book_scannered = true;

            }

            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, getString(R.string.scan_error), Toast.LENGTH_SHORT).show();
                book_scannered = false;
            }

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                fileUri = result.getUri();
                bookImage.setImageURI(fileUri);
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

    public String getFId() {
        Date date = new Date();
        String idN = "i" + date.getTime();
        return idN;
    }
}
