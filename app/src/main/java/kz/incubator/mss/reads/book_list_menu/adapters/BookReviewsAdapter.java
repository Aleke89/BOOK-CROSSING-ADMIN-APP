package kz.incubator.mss.reads.book_list_menu.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

import kz.incubator.mss.reads.R;
import kz.incubator.mss.reads.book_list_menu.module.ReviewInUser;
import kz.incubator.mss.reads.database.StoreDatabase;

import static kz.incubator.mss.reads.database.StoreDatabase.COLUMN_BNAME;

public class BookReviewsAdapter extends RecyclerView.Adapter<BookReviewsAdapter.MyTViewHolder> {
    private Context context;
    private List<ReviewInUser> bookList;
    StoreDatabase storeDb;

    public class MyTViewHolder extends RecyclerView.ViewHolder{
        public TextView info;
        TextView review_text, adminRate;
        RatingBar bookRating;
        ImageView notebook;

        public MyTViewHolder(View view) {
            super(view);
            info = view.findViewById(R.id.titleOfBook);
            review_text = view.findViewById(R.id.review_text);
            bookRating = view.findViewById(R.id.bookRating);
            adminRate = view.findViewById(R.id.adminRate);
//            notebook = view.findViewById(R.id.notebook);
//
//            notebook.setImageResource(R.drawable.done);
        }
    }

    public BookReviewsAdapter(Context context, List<ReviewInUser> bookList) {
        this.context = context;
        this.bookList = bookList;

        storeDb = new StoreDatabase(context);
    }

    @NonNull
    @Override
    public MyTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);

        return new MyTViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyTViewHolder holder, int position) {
        ReviewInUser item = bookList.get(position);
        String bookKey = item.getBook_id();
        String bookName = "";
        int bookRating = item.getUser_rate();
        int adminRating = item.getAdmin_rate();

        Cursor userCursor = storeDb.getBookByFKey(bookKey);
        if (userCursor != null && userCursor.getCount() > 0) {
            userCursor.moveToNext();
            bookName = userCursor.getString(userCursor.getColumnIndex(COLUMN_BNAME));
        }

        holder.info.setText(bookName);
        holder.review_text.setText("\""+item.getReview_text()+"\"");
        holder.bookRating.setRating(bookRating);

        if(adminRating==0){
            holder.adminRate.setTextColor(context.getResources().getColor(R.color.orange));

        }else if(adminRating < 0){

            holder.adminRate.setTextColor(context.getResources().getColor(R.color.red));
            holder.adminRate.setText("" + adminRating);

        }else{
            holder.adminRate.setTextColor(context.getResources().getColor(R.color.green_dark));
            holder.adminRate.setText("" + adminRating);
        }
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

}