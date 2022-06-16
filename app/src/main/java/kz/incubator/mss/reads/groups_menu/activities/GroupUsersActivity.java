package kz.incubator.mss.reads.groups_menu.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import kz.incubator.mss.reads.R;
import kz.incubator.mss.reads.book_list_menu.one_book_fragments.RecyclerItemClickListener;
import kz.incubator.mss.reads.database.StoreDatabase;
import kz.incubator.mss.reads.groups_menu.GetUsersAsyncTask;
import kz.incubator.mss.reads.groups_menu.adapters.UserListAdapter;
import kz.incubator.mss.reads.groups_menu.module.Groups;
import kz.incubator.mss.reads.groups_menu.module.User;

import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_BCOUNT;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_EMAIL;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_ENTER_DATE;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_GROUP;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_GROUP_ID;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_IMG_STORAGE_NAME;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_INFO;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_PHONE;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_PHOTO;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_POINT;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_RAINTING_IN_GROUPS;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_REVIEW_SUM;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_USER_TYPE;

public class GroupUsersActivity extends AppCompatActivity implements View.OnClickListener {

    //Views
    Toolbar toolbar;
    AppBarLayout appBarLayout;
    SearchView searchView;
    LayoutAnimationController animation;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager linearLayoutManager;
    FloatingActionButton fabBtn;
    ProgressBar progressBar;
    View progressLoading;
    View sortDialogView;

    //Lists
    ArrayList<User> userListCopy;
    ArrayList<User> userList;
    public ArrayList<Groups> groupList;

    //Database
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    DatabaseReference mDatabaseRef, userRef;

    //Adapters
    UserListAdapter listAdapter;

    //Variables
    String TABLE_USER = "user_store";
    AlertDialog sortDialog;
    String gId, gName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        Intent intent = getIntent();
        gName = intent.getStringExtra("groupName");
        gId = intent.getStringExtra("groupId");

        initView();
        getUsersFromDB();
        addUserListListener();
        sortDialog();
    }

    public void initView() {
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        storeDb = new StoreDatabase(this);
        sqdb = storeDb.getWritableDatabase();

        toolbar = findViewById(R.id.toolbars);
        appBarLayout = findViewById(R.id.app_bar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(gName+" "+getString(R.string.users));
        userRef = mDatabaseRef.child("user_list");
        userList = new ArrayList<>();
        groupList = new ArrayList<>();
        progressBar = findViewById(R.id.ProgressBar);

        recyclerView = findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_anim);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutAnimation(animation);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        progressLoading = findViewById(R.id.llProgressBar);
        fabBtn = findViewById(R.id.fabBtn);
        fabBtn.setOnClickListener(this);

        searchView = findViewById(R.id.searchView);
        userListCopy = new ArrayList<>();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                filter(s);
                return false;
            }
        });

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int pos) {
                        Intent intent = new Intent(GroupUsersActivity.this, UserProfileActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("user", userList.get(pos));
                        intent.putExtras(bundle);
                        startActivityForResult(intent, 101);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                })
        );


        setupSwipeRefresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { }

    @Override
    public void onClick(View v) {
        userList.clear();
        userList.addAll(userListCopy);

        switch (v.getId()) {
            case R.id.fabBtn:
                startActivity(new Intent(GroupUsersActivity.this, AddUser.class));
                break;

            case R.id.sort_name:

                Collections.sort(userList, User.userNameComprator);
                sortDialog.dismiss();

                break;
            case R.id.sort_readed_books:

                Collections.sort(userList, User.userReadedBooks);
                sortDialog.dismiss();

                break;

            case R.id.sort_point:

                Collections.sort(userList, User.userPoint);
                sortDialog.dismiss();
                break;


        }

        listAdapter.notifyDataSetChanged();
    }

    public void getUsersFromDB() {
        new BackgroundTaskForUserFill(this, gId, recyclerView, progressBar).execute();
        mSwipeRefreshLayout.setRefreshing(false);
        progressLoading.setVisibility(View.GONE);
        listAdapter = new UserListAdapter(this, userList);
    }

    public void refreshUsersFromFirebase(String version) {
        new GetUsersAsyncTask(this, version, mSwipeRefreshLayout, progressLoading).execute();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    int c = 0;
    public void addUserListListener() {
        userRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                User user = dataSnapshot.getValue(User.class);
                for(int i = 0; i < userList.size(); i++) {
                    User userInList = userList.get(i);

                    assert user != null;
                    if(userInList!=null && userInList.getPhoneNumber().equals(user.getPhoneNumber())) {
                        userList.set(i, user);

                        Collections.sort(userList, User.userPoint);
                        listAdapter.notifyDataSetChanged();
                        break;
                    }
                }
                storeDb.updateUser(sqdb, user);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {


            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_card_read, menu);

        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.login_page:

                break;

            case R.id.filter_user:

                sortDialog.show();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public String checkAbone(String ticketDay) {
        String abone = "norm";

        DateFormat day = new SimpleDateFormat("dd");
        DateFormat month = new SimpleDateFormat("MM");
        DateFormat year = new SimpleDateFormat("yyyy");
        int first_slash = ticketDay.indexOf('/');
        int last_slash = ticketDay.lastIndexOf('/');

        String period_day = ticketDay.substring(0, first_slash);
        String period_month = ticketDay.substring(first_slash + 1, last_slash);
        String period_year = ticketDay.substring(last_slash + 1, ticketDay.length());

        Calendar cal = Calendar.getInstance();

        if (period_year.equals(year.format(cal.getTime()))) {
            if (period_month.equals(month.format(cal.getTime()))) {
                if (period_day.equals(day.format(cal.getTime()))) {
                    abone = "last day";
                } else if (Integer.parseInt(period_day) > Integer.parseInt(day.format(cal.getTime()))) {
                    int counter = Integer.parseInt(period_day) - Integer.parseInt(day.format(cal.getTime()));
                    if (counter <= 7) {
                        abone = counter + " days left";
                    }
                } else {
                    abone = "Your subscription is up";

                }
            } else if (Integer.parseInt(period_month) < Integer.parseInt(month.format(cal.getTime()))) {
                abone = "Your subscription is up";

            }
        } else if (Integer.parseInt(period_year) < Integer.parseInt(year.format(cal.getTime()))) {
            abone = "Your subscription is up";

        }

        return abone;
    }

    public void checkVersion() {
        Query myTopPostsQuery = mDatabaseRef.child("user_ver");

        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    String newVersion = dataSnapshot.getValue().toString();
                    if (!getDayCurrentVersion().equals(newVersion)) {
                        refreshUsersFromFirebase(newVersion);
                    } else {
                        onItemsLoadComplete();
                    }
                } else {
                    Toast.makeText(GroupUsersActivity.this, "Can not find user_ver firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public String getDayCurrentVersion() {
        Cursor res = sqdb.rawQuery("SELECT user_ver FROM versions ", null);
        res.moveToNext();
        return res.getString(0);
    }
    public void sortDialog() {
        LayoutInflater factory = LayoutInflater.from(this);

        sortDialogView = factory.inflate(R.layout.dialog_user_filter, null);
        sortDialog = new AlertDialog.Builder(this).create();

        LinearLayout sort_name = sortDialogView.findViewById(R.id.sort_name);
        LinearLayout sort_readed = sortDialogView.findViewById(R.id.sort_readed_books);
        LinearLayout sort_point = sortDialogView.findViewById(R.id.sort_point);

        sort_name.setOnClickListener(this);
        sort_readed.setOnClickListener(this);
        sort_point.setOnClickListener(this);

        sortDialog.setView(sortDialogView);

    }

    public void filter(String text) {
        userList.clear();
        if (text.isEmpty()) {
            userList.addAll(userListCopy);
        } else {
            text = text.toLowerCase();
            for (User item : userListCopy) {
                if (item.getInfo().toLowerCase().contains(text) || item.getInfo().toLowerCase().contains(text) ||
                        item.getPhoneNumber().toUpperCase().contains(text)) {
                    userList.add(item);
                }
            }
        }
        recyclerView.setAdapter(listAdapter);
    }



    public void setupSwipeRefresh() {
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
    }

    public void onItemsLoadComplete() {
        getUsersFromDB();
        mSwipeRefreshLayout.setRefreshing(false);

    }

    public void refreshItems() {
        if (isOnline()) {
            checkVersion();
        }

    }

    private boolean isOnline() {
        if (isNetworkAvailable()) {
            return true;

        } else {
            Toast.makeText(this, getResources().getString(R.string.inetConnection), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public class BackgroundTaskForUserFill extends AsyncTask<Void, Void, Void> {
        RecyclerView recyclerView;
        ProgressBar progressBar;
        StoreDatabase storeDb;
        SQLiteDatabase sqdb;
        DatabaseReference mDatabaseRef, userRef;
        Context context;
        String groupId;

        public BackgroundTaskForUserFill(Context context, String groupId, RecyclerView recyclerView, ProgressBar progressBar) {
            this.recyclerView = recyclerView;
            this.progressBar = progressBar;
            this.context = context;
            this.groupId = groupId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDatabaseRef = FirebaseDatabase.getInstance().getReference();
            progressBar.setVisibility(View.VISIBLE);
            storeDb = new StoreDatabase(context);
            sqdb = storeDb.getWritableDatabase();
            userRef = mDatabaseRef.child("user_list");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Cursor userCursor = sqdb.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE " +
                    COLUMN_GROUP_ID + "=?", new String[]{groupId});

            if (((userCursor != null) && (userCursor.getCount() > 0))) {
                userList.clear();
                while (userCursor.moveToNext()) {
                    userList.add(new User(
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_INFO)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_EMAIL)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_PHONE)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_GROUP_ID)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_GROUP)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_PHOTO)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_ENTER_DATE)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_USER_TYPE)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_IMG_STORAGE_NAME)),
                            userCursor.getInt(userCursor.getColumnIndex(COLUMN_BCOUNT)),
                            userCursor.getInt(userCursor.getColumnIndex(COLUMN_POINT)),
                            userCursor.getInt(userCursor.getColumnIndex(COLUMN_REVIEW_SUM)),
                            userCursor.getInt(userCursor.getColumnIndex(COLUMN_RAINTING_IN_GROUPS))
                    ));

                }

                userListCopy = (ArrayList<User>) userList.clone();

                Collections.sort(userListCopy, User.userPoint);
            }

            return null;
        }
        
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Collections.sort(userList, User.userPoint);
            recyclerView.setAdapter(listAdapter);
            progressBar.setVisibility(View.GONE);

            setTitle("Users " + userList.size());
        }

    }
}