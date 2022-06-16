package kz.incubator.mss.reads.groups_menu.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kz.incubator.mss.reads.R;
import kz.incubator.mss.reads.database.StoreDatabase;
import kz.incubator.mss.reads.groups_menu.module.User;
import kz.incubator.mss.reads.groups_menu.profile_fragments.ReadedBookListFragment;
import kz.incubator.mss.reads.groups_menu.profile_fragments.ReadingBookListFragment;
import kz.incubator.mss.reads.groups_menu.profile_fragments.RecommendationBookListFragment;
import kz.incubator.mss.reads.groups_menu.profile_fragments.ReviewsForBookFragment;

public class UserProfileActivity extends AppCompatActivity {
    Toolbar toolbar;
    AppBarLayout appBarLayout;
    TextView username, pointSum, phoneNumber, subTime, userEmail, userRating;
    TextView readBookCount;
    CircleImageView userImage;
    ImageView userTypeIcon;
    ViewPager viewPager;
    TabLayout tabLayout;
    int USER_EDIT = 97;

    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    StorageReference storageReference;
    DatabaseReference mDatabase;

    public static User user;
    Bundle bundle;
    ReadingBookListFragment readingBookListFragment;
    ReadedBookListFragment readedBookListFragment;
    ReviewsForBookFragment reviewsForBookFragment;
    RecommendationBookListFragment recommendationBookListFragment;

    LinearLayout subscriptionL, phoneL;
    Dialog subDialog;
    Button saveBtn;
    final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 77;
    Bundle bundleFragment;

    private int[] tabIcons = {
            R.drawable.ic_class_black_24dp,
            R.drawable.ic_cloud_done_black_24dp,
            R.drawable.ic_receipt_black_24dp,
            R.drawable.ic_assistant_black_24dp,
    };
    String userId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_profile);

        bundle = getIntent().getExtras();
        user = (User) bundle.getSerializable("user");

        initWidgets();
        loadSubSpinner();
    }

    public void initWidgets() {
        toolbar = findViewById(R.id.toolbars);
        appBarLayout = findViewById(R.id.app_bar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.user_profile));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        storageReference = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        storeDb = new StoreDatabase(this);
        sqdb = storeDb.getWritableDatabase();

        username = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        phoneNumber = findViewById(R.id.phoneNumber);
        userRating = findViewById(R.id.userRating);
        pointSum = findViewById(R.id.pointSum);
        readBookCount = findViewById(R.id.readBookCount);
        userImage = findViewById(R.id.userImage);
        userTypeIcon = findViewById(R.id.userTypeIcon);
        viewPager = findViewById(R.id.viewPager);
        subscriptionL = findViewById(R.id.subscriptionL);
        phoneL = findViewById(R.id.phoneL);

        initializeUser(bundle, user);
        initFragments();
        bookReadedCountListener();

        setupViewPager(viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        setupTabIcons();
        phoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPhoneCallDialog();
            }
        });
    }

    @SuppressLint("DefaultLocale")
    public void initializeUser(Bundle bundle, User u) {

        if (bundle != null && u != null) {

            Glide.with(getApplicationContext())
                    .load(u.getPhoto())
                    .placeholder(R.drawable.user_def)
                    .dontAnimate()
                    .into(userImage);

            username.setText(u.getInfo());
            userEmail.setText(u.getEmail());
            phoneNumber.setText(u.getPhoneNumber());
            pointSum.setText(String.format("%d", u.getPoint()));
            readBookCount.setText("0");
            userRating.setText("" + u.getRatingInGroups());

            userId = u.getPhoneNumber();

            int uTypeIcon = R.color.transparent;

            switch (user.getUserType()) {
                case "gold":
                    uTypeIcon = R.drawable.ic_gold;
                    break;
                case "silver":
                    uTypeIcon = R.drawable.ic_silver;
                    break;
                case "bronze":
                    uTypeIcon = R.drawable.ic_bronze;
                    break;
            }

            userTypeIcon.setImageResource(uTypeIcon);
        }
    }

    public void initFragments() {
        readingBookListFragment = new ReadingBookListFragment();
        readedBookListFragment = new ReadedBookListFragment();
        reviewsForBookFragment = new ReviewsForBookFragment();
        recommendationBookListFragment = new RecommendationBookListFragment();

        bundleFragment = new Bundle();
        bundleFragment.putSerializable("user", user);

        readingBookListFragment.setArguments(bundleFragment);
        readedBookListFragment.setArguments(bundleFragment);
        reviewsForBookFragment.setArguments(bundleFragment);
        recommendationBookListFragment.setArguments(bundleFragment);
    }

    public void loadPhoneCallDialog() {

        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle(getString(R.string.user) + user.getInfo())
                .setMessage(getString(R.string.phone_number) + user.getPhoneNumber())
                .setPositiveButton(getString(R.string.call), new DialogInterface.OnClickListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + user.getPhoneNumber()));
                        if (ContextCompat.checkSelfPermission(UserProfileActivity.this,
                                Manifest.permission.CALL_PHONE)
                                != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(UserProfileActivity.this,
                                    new String[]{Manifest.permission.CALL_PHONE},
                                    MY_PERMISSIONS_REQUEST_CALL_PHONE);

                        } else {
                            try {
                                startActivity(callIntent);
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                })
                .setNeutralButton(getString(R.string.sms), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Uri uri = Uri.parse("smsto:" + user.getPhoneNumber());
                        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, uri);
                        smsIntent.putExtra("sms_body", "Dear, " + user.getInfo() + "! MDS Reads!");
                        startActivity(smsIntent);
                    }
                })
                .show();

    }

    public static void setPoint(String point) {

    }

    public void loadSubSpinner() {
        subDialog = new Dialog(UserProfileActivity.this);
        subDialog.setContentView(R.layout.dialog_subscription);
        subTime = subDialog.findViewById(R.id.subTime);
        saveBtn = subDialog.findViewById(R.id.saveBtn);
        saveBtn.setVisibility(View.INVISIBLE);

    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        tabLayout.getTabAt(3).setIcon(tabIcons[3]);
    }

    int userReadBooksCount = 0;
    int userReviewsCount = 0;

    public void bookReadedCountListener() {
        mDatabase.child("user_list").child(userId).child("readed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userReadBooksCount = (int) dataSnapshot.getChildrenCount();
                    readBookCount.setText("" + userReadBooksCount);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("user_list").child(userId).child("point").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    pointSum.setText("" + dataSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mDatabase.child("user_list").child(userId).child("reviews").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userReviewsCount = (int) dataSnapshot.getChildrenCount();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setupViewPager(ViewPager viewPager) {
        SimplePageFragmentAdapter adapter = new SimplePageFragmentAdapter(getSupportFragmentManager());

        adapter.addFragment(readingBookListFragment, getString(R.string.reading));
        adapter.addFragment(readedBookListFragment, getString(R.string.readed));
        adapter.addFragment(reviewsForBookFragment, getString(R.string.reviews));
        adapter.addFragment(recommendationBookListFragment, getString(R.string.recommendations));

        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user_profile, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.edit_book:

                Intent intent = new Intent(this, EditUser.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", user);
                intent.putExtras(bundle);
                startActivityForResult(intent, USER_EDIT);

                break;

            case R.id.delete_book:

                new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                        .setTitle(getString(R.string.delete_user))
                        .setMessage(getString(R.string.sure_delete_user) + user.getInfo())
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDatabase.child("user_list").child(user.getPhoneNumber()).removeValue();

                                mDatabase.child("book_list").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot books : dataSnapshot.getChildren()) {
                                            mDatabase.child("book_list").child(books.getKey()).child("reading").child(user.getPhoneNumber()).removeValue();
                                            mDatabase.child("book_list").child(books.getKey()).child("readed").child(user.getPhoneNumber()).removeValue();
                                        }

                                        String tableName = "other";
                                        switch (user.getUserType()) {
                                            case "gold":
                                                tableName = "a_gold";
                                                break;
                                            case "silver":
                                                tableName = "b_silver";
                                                break;
                                            case "bronze":
                                                tableName = "b_bronze";
                                                break;
                                        }

                                        mDatabase.child("rating_users").child(tableName).child(user.getPhoneNumber()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                decreasePersonCount(user.getGroup_id());
                                                increaseUserVersion();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                StorageReference desertRef = storageReference.child("users").child(user.getImgStorageName());
                                desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(UserProfileActivity.this, getString(R.string.user_deleted), Toast.LENGTH_SHORT).show();
                                        onBackPressed();
                                    }

                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception exception) {
                                        Log.d("info", "onFailure: did not delete file");
                                        onBackPressed();
                                    }
                                });

                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void decreasePersonCount(final String groupId){
        mDatabase.child("group_list").child(groupId).child("person_count").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = Integer.parseInt(dataSnapshot.getValue().toString());
                mDatabase.child("group_list").child(groupId).child("person_count").setValue((count-1));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void increaseUserVersion() {
        mDatabase.child("user_ver").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String version;

                if (dataSnapshot.exists()) {
                    version = dataSnapshot.getValue().toString();
                    long ver = Long.parseLong(version);
                    ver += 1;
                    mDatabase.child("user_ver").setValue(ver);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == USER_EDIT) {
            if (resultCode == Activity.RESULT_OK) {

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the phone call

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onBackPressed() {
        Intent t = new Intent();
        setResult(RESULT_OK, t);
        finish();
    }

    public class SimplePageFragmentAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private List<String> titles = new ArrayList<>();

        public SimplePageFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return mFragmentList.get(i);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String one) {
            mFragmentList.add(fragment);
            titles.add(one);
        }
    }
}