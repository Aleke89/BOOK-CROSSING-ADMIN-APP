package kz.incubator.mss.reads.book_list_menu;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import kz.incubator.mss.reads.book_list_menu.adapters.BookListAdapter;
import kz.incubator.mss.reads.book_list_menu.module.Book;
import kz.incubator.mss.reads.database.StoreDatabase;

import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_BAUTHOR;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_BDESC;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_BNAME;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_BPAGE_NUMBER;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_BRATING;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_BRESERVED;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_FKEY;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_PHOTO;
import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_QR_CODE;
import static kz.incubator.mss.reads.database.StoreDatabase.TABLE_BOOKS;

public class GetBooksAsyncTask extends AsyncTask<Void, Book, Void> {

    ArrayList<Book> bookList = new ArrayList<>();
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    DatabaseReference mDatabaseRef, booksRef;
    BookListAdapter bookListAdapter;
    Context context;
    String version = "";

    public GetBooksAsyncTask(Context context, RecyclerView recyclerView, SwipeRefreshLayout refreshLayout, String version) {
        this.recyclerView = recyclerView;
        this.swipeRefreshLayout = refreshLayout;
        this.context = context;
        this.version = version;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        swipeRefreshLayout.setRefreshing(true);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        storeDb = new StoreDatabase(context);
        sqdb = storeDb.getWritableDatabase();
        booksRef = mDatabaseRef.child("book_list");
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Cursor res = sqdb.rawQuery("SELECT book_list_ver FROM versions ", null);
        res.moveToNext();
        String getDay = res.getString(0);

        ContentValues versionValues = new ContentValues();
        versionValues.put("book_list_ver", version);
        sqdb.update("versions", versionValues, "book_list_ver=" + getDay, null);

        booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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

                    sqdb.insert(TABLE_BOOKS, null, teacherValue);

                    bookList.add(book);
                    Log.i("info", "qr_code: "+qr_code);
                }

//                Collections.reverse(userList);
                bookListAdapter = new BookListAdapter(context, bookList);
                recyclerView.setAdapter(bookListAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return null;
    }

    @Override
    protected void onProgressUpdate(Book... values) {
        super.onProgressUpdate(values);
        bookList.add(values[0]);
        bookListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        swipeRefreshLayout.setRefreshing(false);
    }
}
