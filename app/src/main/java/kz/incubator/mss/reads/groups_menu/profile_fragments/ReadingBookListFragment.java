package kz.incubator.mss.reads.groups_menu.profile_fragments;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import kz.incubator.mss.reads.R;
import kz.incubator.mss.reads.book_list_menu.module.Book;
import kz.incubator.mss.reads.database.StoreDatabase;
import kz.incubator.mss.reads.groups_menu.adapters.UserBookListAdapter;
import kz.incubator.mss.reads.groups_menu.module.User;

import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_BAUTHOR;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_BDESC;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_BNAME;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_BPAGE_NUMBER;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_BRATING;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_BRESERVED;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_FKEY;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_IMG_STORAGE_NAME;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_PHOTO;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_QR_CODE;

public class ReadingBookListFragment extends Fragment{
    ArrayList<Book> bookList;
    UserBookListAdapter bookAdapter;
    RecyclerView recyclerView;
    View view;
    DatabaseReference mDatabase;
    ProgressBar progressBar;
    TextView checkIsEmpty;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    String TABLE_BOOKS = "book_store";
    static Activity activity;
    String userId;
    FirebaseUser currentUser;
    User user;
    String classType;

    public ReadingBookListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_reading_book_list, container, false);
        initUserId();
        initialize();

        downloadReadBooks();
        return view;
    }

    public void initialize() {
        recyclerView = view.findViewById(R.id.recyclerForBook);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        checkIsEmpty = view.findViewById(R.id.checkIsEmpty);
        activity = getActivity();

        bookList = new ArrayList<>();
        bookAdapter = new UserBookListAdapter(getActivity(), bookList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(bookAdapter);
        progressBar = view.findViewById(R.id.ProgressBar);

        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

    }

    public void initUserId() {
        Bundle bundle = this.getArguments();
        userId = "";

        if (bundle != null) {
            user = (User) bundle.getSerializable("user");
            userId = user.getPhoneNumber();
        }
    }

    public void downloadReadBooks() {
        mDatabase.child("user_list").child(userId).child("reading").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                bookList.clear();
                if (dataSnapshot.exists()) {

                    checkIsEmpty.setVisibility(View.GONE);
                    for (DataSnapshot last : dataSnapshot.getChildren()) {
                        String bookKey = last.getKey();

                        Cursor userCursor = getBookByFKey(bookKey);
                        if (userCursor != null && userCursor.getCount() > 0) {
                            userCursor.moveToNext();
                            bookList.add(new Book(
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_FKEY)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_BNAME)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_BAUTHOR)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_BDESC)),
                                    userCursor.getInt(userCursor.getColumnIndex(COLUMN_BPAGE_NUMBER)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_BRATING)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_PHOTO)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_BRESERVED)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_QR_CODE)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_IMG_STORAGE_NAME))
                            ));

                        }
                    }

                } else {
                    checkIsEmpty.setVisibility(View.VISIBLE);
                }

                bookAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public Cursor getBookByFKey(String fkey) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_BOOKS + " WHERE " +
                COLUMN_FKEY + "=?", new String[]{fkey});
        return res;

    }

}