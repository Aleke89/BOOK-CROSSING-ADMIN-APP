package kz.incubator.mss.reads.groups_menu.profile_fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import kz.incubator.mss.reads.R;
import kz.incubator.mss.reads.book_list_menu.adapters.RecommendationBookListAdapter;
import kz.incubator.mss.reads.book_list_menu.module.Book;
import kz.incubator.mss.reads.groups_menu.module.User;


public class RecommendationBookListFragment extends Fragment {
    View view;
    RecyclerView recyclerView;
    RecommendationBookListAdapter adapter;
    ArrayList<Book> bookList = new ArrayList<>();
    ProgressBar progressBar;
    DatabaseReference mDatabase;
    ArrayList<String> keys = new ArrayList<>();
    String userId;

    public RecommendationBookListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recommendation_book_list, container, false);

        initUserId();
        getRecommendedBooks();
        return view;
    }

    User user;

    public void initUserId() {
        Bundle bundle = this.getArguments();
        userId = "";

        if (bundle != null) {
            user = (User) bundle.getSerializable("user");
            userId = user.getPhoneNumber();
        }
    }

    public void initializeRecyclerView() {
        recyclerView = view.findViewById(R.id.recyclerView);
        Collections.reverse(bookList);
        adapter = new RecommendationBookListAdapter(getActivity(), bookList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }

    public void getRecommendedBooks() {
        progressBar = view.findViewById(R.id.ProgressBar);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("user_list").child(userId).child("recommendations").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                keys.clear();
                bookList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Book book = data.getValue(Book.class);
                    bookList.add(book);
                }
                initializeRecyclerView();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
