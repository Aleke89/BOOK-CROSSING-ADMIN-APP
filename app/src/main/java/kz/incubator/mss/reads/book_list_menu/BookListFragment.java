package kz.incubator.mss.reads.book_list_menu;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
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
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import kz.incubator.mss.reads.R;
import kz.incubator.mss.reads.book_list_menu.activities.AddBook;
import kz.incubator.mss.reads.book_list_menu.adapters.BookListAdapter;
import kz.incubator.mss.reads.book_list_menu.module.Book;
import kz.incubator.mss.reads.database.StoreDatabase;

import static kz.incubator.mss.reads.MenuActivity.setTitle;
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
import static kz.incubator.mss.reads.database.StoreDatabase.TABLE_BOOKS;


public class BookListFragment extends Fragment implements View.OnClickListener{
    RecyclerView bookListRv;//, bookListRv2;
    BookListAdapter bookListAdapter;
    ArrayList<Book> bookList;
    ArrayList<Book> bookListCopy, bookListCopy2;
    private RecyclerView.LayoutManager linearLayoutManager, gridLayoutManager;
    DatabaseReference mDatabaseRef, booksRef;
    View rootView;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    FloatingActionButton fabBtn;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ProgressBar progressBar;
    Book book;
    SearchView searchView;
    Switch filterSwitch;
    String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_book_list, container, false);
        setupViews();
        setupSwipeRefresh();
        checkVersion();

        fillBooks();
        initializeSearchView();

        //doChange();
        return rootView;
    }

    public void doChange(){
        booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot booksSnapshot : dataSnapshot.getChildren()) {
                        String fKey = booksSnapshot.getKey();
                        booksRef.child(fKey).child("rating").setValue("0,0");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void initializeSearchView(){
        searchView = rootView.findViewById(R.id.searchView);
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
    }

    public void setupViews() {
        bookListRv = rootView.findViewById(R.id.bookListRv);
        bookList = new ArrayList<>();
        bookListCopy = new ArrayList<>();

        linearLayoutManager = new LinearLayoutManager(getActivity());
        gridLayoutManager = new GridLayoutManager(getActivity(), 2);

        bookListAdapter = new BookListAdapter(getActivity(), bookList);

        int resId = R.anim.layout_anim_book;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        bookListRv.setLayoutManager(linearLayoutManager);
        bookListRv.setItemAnimator(new DefaultItemAnimator());
        bookListRv.setLayoutAnimation(animation);
        bookListRv.setAdapter(bookListAdapter);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        booksRef = mDatabaseRef.child("book_list");
        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

        fabBtn = rootView.findViewById(R.id.fabBtn);
        fabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddBook.class));
            }
        });

        progressBar = rootView.findViewById(R.id.ProgressBar);
        addBookChangeListener();
        sortDialog();
    }

    View sortDialogView;
    AlertDialog sortDialog;

    public void sortDialog(){
        LayoutInflater factory = LayoutInflater.from(getActivity());

        sortDialogView  = factory.inflate(R.layout.dialog_filter, null);
        sortDialog = new AlertDialog.Builder(getActivity()).create();

        LinearLayout sort_name   = sortDialogView.findViewById(R.id.sort_name);
        LinearLayout sort_author = sortDialogView.findViewById(R.id.sort_author);
        LinearLayout sort_rating = sortDialogView.findViewById(R.id.sort_rating);
        LinearLayout filter_hand = sortDialogView.findViewById(R.id.filter_hand);
        filterSwitch = sortDialogView.findViewById(R.id.filterSwitch);

        sort_name.setOnClickListener(this);
        sort_author.setOnClickListener(this);
        sort_rating.setOnClickListener(this);
        filter_hand.setOnClickListener(this);

        sortDialog.setView(sortDialogView);

    }

    @Override
    public void onClick(View v) {
        bookList.clear();

        if(filterSwitch.isChecked()) bookList.addAll(bookListCopy);
        else bookList.addAll(bookListCopy2);

        switch (v.getId()){

            case R.id.sort_name:
                Collections.sort(bookList, Book.bookNameComprator);
                sortDialog.dismiss();

                break;
            case R.id.sort_author:

                Collections.sort(bookList, Book.bookAuthorComprator);
                sortDialog.dismiss();

                break;
            case R.id.sort_rating:

                Collections.sort(bookList, Book.ratingComparator);
                sortDialog.dismiss();
                break;
        }

        bookListAdapter.notifyDataSetChanged();
        setTitle("Book list"+bookList.size());
    }

    public void addBookChangeListener() {

        booksRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                book = dataSnapshot.getValue(Book.class);
                storeDb.updateBook(sqdb, book);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                book = dataSnapshot.getValue(Book.class);
                mDatabaseRef.child("user_list").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot bookDataSnapshot : dataSnapshot.getChildren()) {
                            String userId = bookDataSnapshot.getKey();
                            mDatabaseRef.child("user_list").child(userId).child("reading").child(book.getFirebaseKey()).removeValue();
                            mDatabaseRef.child("user_list").child(userId).child("readed").child(book.getFirebaseKey()).removeValue();
                            storeDb.deleteBook(sqdb, book);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void initIncreaseVersion() {
        mDatabaseRef.child("book_list_ver").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String version;

                if (dataSnapshot.exists()) {
                    version = dataSnapshot.getValue().toString();
                    long ver = Long.parseLong(version);
                    ver += 1;
                    mDatabaseRef.child("book_list_ver").setValue(ver);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkVersion() {
        Query myTopPostsQuery = mDatabaseRef.child("book_list_ver");
        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String newVersion = dataSnapshot.getValue().toString();
                    if (!getCurrentBookVersion().equals(newVersion)) {
                        updateVersion(newVersion);
                        refreshBooks();
                    }else{
                        onItemsLoadComplete();
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.can_not_booklist_ver), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void refreshBooks() {
        booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    bookList.clear();
                    storeDb.cleanBooks(sqdb);

                    for (DataSnapshot booksSnapshot : dataSnapshot.getChildren()) {

                        Book book = booksSnapshot.getValue(Book.class);

                        String fKey = book.getFirebaseKey();
                        String name = book.getName();
                        String author = book.getAuthor();
                        String desc = book.getDesc();
                        int page_number = book.getPage_number();
                        String rating = book.getRating();
                        String photo = book.getPhoto();
                        String reserved = book.getReserved();
                        String qr_code = book.getQr_code();
                        String imgStorageName = book.getImgStorageName();

                        ContentValues teacherValue = new ContentValues();
                        teacherValue.put(COLUMN_FKEY, fKey);
                        teacherValue.put(COLUMN_BNAME, name);
                        teacherValue.put(COLUMN_BAUTHOR, author);
                        teacherValue.put(COLUMN_BDESC, desc);
                        teacherValue.put(COLUMN_BPAGE_NUMBER, page_number);
                        teacherValue.put(COLUMN_BRATING, rating);
                        teacherValue.put(COLUMN_PHOTO, photo);
                        teacherValue.put(COLUMN_BRESERVED, reserved);
                        teacherValue.put(COLUMN_QR_CODE, qr_code);
                        teacherValue.put(COLUMN_IMG_STORAGE_NAME, imgStorageName);

                        sqdb.insert(TABLE_BOOKS, null, teacherValue);
                        bookList.add(book);
                    }

                    bookListAdapter.notifyDataSetChanged();
                    setTitle(getString(R.string.menu_books)+bookList.size());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void updateVersion(String version){
        Cursor res = sqdb.rawQuery("SELECT book_list_ver FROM versions ", null);
        res.moveToNext();
        String verStr = res.getString(0);

        ContentValues versionValues = new ContentValues();
        versionValues.put("book_list_ver", version);
        sqdb.update("versions", versionValues, "book_list_ver=" + verStr, null);

    }

    public void fillBooks() {
        Cursor userCursor = getBooks();
        bookList.clear();

        if (((userCursor != null) && (userCursor.getCount() > 0))) {
            while (userCursor.moveToNext()) {
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

            bookListCopy = (ArrayList<Book>) bookList.clone();
            bookListCopy2 = (ArrayList<Book>) bookList.clone();
        }
        mSwipeRefreshLayout.setRefreshing(false);
        bookListAdapter.notifyDataSetChanged();

        Log.i("fillBooks", "fillBooks: ");
        setTitle(getString(R.string.menu_books)+bookList.size());
    }

    public Cursor getBooks() {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_BOOKS + " ORDER BY " + COLUMN_BNAME, null);
        return res;
    }

    public void updateCurrentVersion(String newVersion) {
        ContentValues versionValues = new ContentValues();
        versionValues.put("book_list_ver", newVersion);
        sqdb.update("versions", versionValues, "book_list_ver=" + currentBookVersion, null);
    }

    String currentBookVersion;

    public String getCurrentBookVersion() {
        Cursor res = sqdb.rawQuery("SELECT book_list_ver FROM versions ", null);
        res.moveToNext();
        currentBookVersion = res.getString(0);
        return currentBookVersion;
    }

    public void filter(String text) {

        bookList.clear();

        if (text.isEmpty()) {
            bookList.addAll(bookListCopy);

        } else {
            text = text.toLowerCase();
            for (Book item : bookListCopy) {

                if (item.getName().toLowerCase().contains(text) || item.getAuthor().toLowerCase().contains(text) ||
                        item.getName().toUpperCase().contains(text) || item.getAuthor().toUpperCase().contains(text)) {

                    bookList.add(item);
                }
            }
        }

        setTitle("Books "+bookList.size());
        bookListAdapter.notifyDataSetChanged();

    }

    public void setupSwipeRefresh() {
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
    }

    public void refreshItems() {

        if (!isOnline()) {
            Toast.makeText(getActivity(), "Check internet connection", Toast.LENGTH_LONG).show();
        } else {
            checkVersion();
        }

    }

    public void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.book_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    int c = 0;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.change_view:
                sortDialog.show();
                /*
                sort by name
                sort by author name
                on hand
                sort by rating


                 */
                break;
        }

        /*c++;

        switch (id) {
            case R.id.change_view:
                if(c%2==1) item.setIcon(getResources().getDrawable(R.drawable.ic_view_list));
                else item.setIcon(getResources().getDrawable(R.drawable.ic_grid_view));

                break;
        }
        */
        return super.onOptionsItemSelected(item);
    }

    public boolean isOnline() {
        return true;
    }

}


