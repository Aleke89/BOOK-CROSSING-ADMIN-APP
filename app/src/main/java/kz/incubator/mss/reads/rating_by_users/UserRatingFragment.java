package kz.incubator.mss.reads.rating_by_users;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import kz.incubator.mss.reads.R;
import kz.incubator.mss.reads.book_list_menu.one_book_fragments.RecyclerItemClickListener;
import kz.incubator.mss.reads.database.StoreDatabase;
import kz.incubator.mss.reads.groups_menu.GetUsersAsyncTask;
import kz.incubator.mss.reads.groups_menu.activities.UserProfileActivity;
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

public class UserRatingFragment extends Fragment implements View.OnClickListener {
    DatabaseReference mDatabaseRef, ratingUserRef;
    RecyclerView recyclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView.LayoutManager linearLayoutManager;
    ArrayList<User> userListCopy;
    ArrayList<User> userList;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    SearchView searchView;
    View view;
    ProgressBar progressBar;
    UserRatingdapter userRatingdapter;
    String TABLE_USER = "user_store";
    View filterDialogView;
    AlertDialog filterDialog;
    View progressLoading;
    ArrayList<User> goldUsers, silverUsers, bronzeUsers, otherUsers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_rating_user, container, false);

        initView();
        checkVersion();
        getRatingUsers();
        filterDialog();

        return view;
    }

    public void initView() {

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

        ratingUserRef = mDatabaseRef.child("rating_users");
        userList = new ArrayList<>();
        goldUsers = new ArrayList<>();
        silverUsers = new ArrayList<>();
        bronzeUsers = new ArrayList<>();
        otherUsers = new ArrayList<>();

        progressBar = view.findViewById(R.id.ProgressBar);
        progressLoading = view.findViewById(R.id.llProgressBar);

        recyclerView = view.findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        int resId = R.anim.layout_anim;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutAnimation(animation);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

//        tempFillUsers();
        userRatingdapter = new UserRatingdapter(getActivity(), userList);
        recyclerView.setAdapter(userRatingdapter);

        searchView = view.findViewById(R.id.searchView);
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
                new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int pos) {
                        Intent intent = new Intent(getActivity(), UserProfileActivity.class);
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

    int userTypeRating = 0;

    private User getUserFromDb(String userPhone) {
        Cursor userCursor = storeDb.getUserEntry(userPhone);

        if (((userCursor != null) && (userCursor.getCount() > 0))) {
            userCursor.moveToFirst();
            return new User(
                    userTypeRating--,
                    userCursor.getString(userCursor.getColumnIndex(COLUMN_INFO)),
                    userCursor.getString(userCursor.getColumnIndex(COLUMN_EMAIL)),
                    userCursor.getString(userCursor.getColumnIndex(COLUMN_PHONE)),
                    userCursor.getString(userCursor.getColumnIndex(COLUMN_GROUP_ID)),
                    userCursor.getString(userCursor.getColumnIndex(COLUMN_GROUP)),
                    userCursor.getString(userCursor.getColumnIndex(COLUMN_PHOTO)),
                    userCursor.getString(userCursor.getColumnIndex(COLUMN_ENTER_DATE)),
//                    "none",
                    userCursor.getString(userCursor.getColumnIndex(COLUMN_USER_TYPE)),
                    userCursor.getString(userCursor.getColumnIndex(COLUMN_IMG_STORAGE_NAME)),
                    userCursor.getInt(userCursor.getColumnIndex(COLUMN_BCOUNT)),
                    userCursor.getInt(userCursor.getColumnIndex(COLUMN_POINT)),
                    userCursor.getInt(userCursor.getColumnIndex(COLUMN_REVIEW_SUM)),
                    userCursor.getInt(userCursor.getColumnIndex(COLUMN_RAINTING_IN_GROUPS))
            );
        }

        return null;
    }

    private void getRatingUsers() {
        userList.clear();

        Query goldQuery = ratingUserRef.child("a_gold").orderByChild("point");
        Log.d("M_UserRatingFragment", "a_gold");
        goldQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    goldUsers.clear();
                    userTypeRating = (int) dataSnapshot.getChildrenCount();
                    for (DataSnapshot userData : dataSnapshot.getChildren()) {
                        String userPhone = userData.getKey();
                        User g = getUserFromDb(userPhone);
                        
                        Log.d("M_UserRatingFragment", "type rating: "+g.getTypeRating());
                        goldUsers.add(g);
                    }

                    Collections.reverse(goldUsers);
                    userList.addAll(goldUsers);
                    userRatingdapter.notifyDataSetChanged();
                    getSilverUsers();
                } else {
                    getSilverUsers();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void getSilverUsers() {
        Query silverQuery = ratingUserRef.child("b_silver").orderByChild("point");
        Log.d("M_UserRatingFragment", "b_silver");
        silverQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userTypeRating = (int) dataSnapshot.getChildrenCount();
                    silverUsers.clear();
                    for (DataSnapshot userData : dataSnapshot.getChildren()) {
                        String userPhone = userData.getKey();
                        silverUsers.add(getUserFromDb(userPhone));
                    }
                    Collections.reverse(silverUsers);
                    userList.addAll(silverUsers);
                    userRatingdapter.notifyDataSetChanged();
                    getBronzeUsers();
                } else {
                    getBronzeUsers();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getBronzeUsers() {
        Query bronzeQuery = ratingUserRef.child("c_bronze").orderByChild("point");
        Log.d("M_UserRatingFragment", "bronze");
        bronzeQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userTypeRating = (int) dataSnapshot.getChildrenCount();
                    bronzeUsers.clear();
                    for (DataSnapshot userData : dataSnapshot.getChildren()) {
                        String userPhone = userData.getKey();
                        bronzeUsers.add(getUserFromDb(userPhone));
                    }
                    Collections.reverse(bronzeUsers);
                    userList.addAll(bronzeUsers);
                    userRatingdapter.notifyDataSetChanged();
                    getOtherUsers();
                } else {
                    getOtherUsers();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void getOtherUsers() {
        Query otherQuery = ratingUserRef.child("other").orderByChild("point");
        Log.d("M_UserRatingFragment", "other");
        otherQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userTypeRating = (int) dataSnapshot.getChildrenCount();
                    otherUsers.clear();
                    for (DataSnapshot userData : dataSnapshot.getChildren()) {
                        String userPhone = userData.getKey();
                        otherUsers.add(getUserFromDb(userPhone));
                    }
                    Collections.reverse(otherUsers);
                    userList.addAll(otherUsers);
                    userListCopy = (ArrayList<User>) userList.clone();
                    userRatingdapter.notifyDataSetChanged();
                    progressLoading.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    public void filter(String text) {
        userList.clear();
        if (text.isEmpty()) {
            userList.addAll(userListCopy);
        } else {
            text = text.toLowerCase();
            for (User item : userListCopy) {
                if (item.getInfo().toLowerCase().contains(text) || item.getInfo().toUpperCase().contains(text) ||
                        item.getGroupName().toUpperCase().contains(text) || item.getGroupName().toLowerCase().contains(text)) {
                    userList.add(item);
                }
            }
        }
        recyclerView.setAdapter(userRatingdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        inflater.inflate(R.menu.menu_card_read, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.login_page:

                break;

            case R.id.filter_user:

                filterDialog.show();

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void filterDialog() {
        LayoutInflater factory = LayoutInflater.from(getActivity());

        filterDialogView = factory.inflate(R.layout.dialog_rating_user_filter, null);
        filterDialog = new AlertDialog.Builder(getActivity()).create();

        LinearLayout filter_all = filterDialogView.findViewById(R.id.filter_all);
        LinearLayout filter_gold = filterDialogView.findViewById(R.id.filter_gold);
        LinearLayout filter_silver = filterDialogView.findViewById(R.id.filter_silver);
        LinearLayout filter_bronze = filterDialogView.findViewById(R.id.filter_bronze);
        LinearLayout filter_other = filterDialogView.findViewById(R.id.filter_other);

        filter_all.setOnClickListener(this);
        filter_gold.setOnClickListener(this);
        filter_silver.setOnClickListener(this);
        filter_bronze.setOnClickListener(this);
        filter_other.setOnClickListener(this);

        filterDialog.setView(filterDialogView);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter_all:
                userList.clear();
                userList.addAll(goldUsers);
                userList.addAll(silverUsers);
                userList.addAll(bronzeUsers);
                userList.addAll(otherUsers);
                filterFinalCase();

                break;
            case R.id.filter_gold:

                userList.clear();
                userList.addAll(goldUsers);
                filterFinalCase();

                break;
            case R.id.filter_silver:

                userList.clear();
                userList.addAll(silverUsers);
                filterFinalCase();

                break;
            case R.id.filter_bronze:

                userList.clear();
                userList.addAll(bronzeUsers);
                filterFinalCase();

                break;
            case R.id.filter_other:

                userList.clear();
                userList.addAll(otherUsers);
                filterFinalCase();
                break;
        }
    }

    private void filterFinalCase(){
        userListCopy = (ArrayList<User>) userList.clone();
        userRatingdapter.notifyDataSetChanged();
        filterDialog.dismiss();
    }
    public void setupSwipeRefresh() {
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
    }

    public void refreshItems() {
        if (isOnline()) {
            getRatingUsers();
        }
    }

    private boolean isOnline() {
        if (isNetworkAvailable()) {
            return true;

        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.inetConnection), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
                    }
                } else {
                    Toast.makeText(getActivity(), "Can not find user_ver firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void refreshUsersFromFirebase(String version) {
        new GetUsersAsyncTask(getActivity(), version, mSwipeRefreshLayout, progressLoading).execute();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public String getDayCurrentVersion() {
        Cursor res = sqdb.rawQuery("SELECT user_ver FROM versions ", null);
        res.moveToNext();
        return res.getString(0);
    }

    public class BackgroundTaskForUserFill extends AsyncTask<Void, Void, Void> {
        RecyclerView recyclerView;
        ProgressBar progressBar;
        StoreDatabase storeDb;
        SQLiteDatabase sqdb;
        Context context;

        public BackgroundTaskForUserFill(Context context, RecyclerView recyclerView, ProgressBar progressBar) {
            this.recyclerView = recyclerView;
            this.progressBar = progressBar;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDatabaseRef = FirebaseDatabase.getInstance().getReference();
            progressBar.setVisibility(View.VISIBLE);
            storeDb = new StoreDatabase(context);
            sqdb = storeDb.getWritableDatabase();
        }

        @Override
        protected Void doInBackground(Void... voids) {


            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Collections.reverse(userList);
            recyclerView.setAdapter(userRatingdapter);
            progressBar.setVisibility(View.GONE);
        }
    }
}