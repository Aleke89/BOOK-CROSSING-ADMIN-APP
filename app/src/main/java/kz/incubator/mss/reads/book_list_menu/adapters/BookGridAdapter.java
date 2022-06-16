package kz.incubator.mss.reads.book_list_menu.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import kz.incubator.mss.reads.R;
import kz.incubator.mss.reads.book_list_menu.activities.OneBookAcvitiy;
import kz.incubator.mss.reads.book_list_menu.interfaces.ItemClickListener;
import kz.incubator.mss.reads.book_list_menu.module.Book;

public class BookGridAdapter extends RecyclerView.Adapter<BookGridAdapter.MyTViewHolder>{
    private Context context;
    private List<Book> bookList;
    DateFormat dateF;
    String date;
    String number;
    onMeClick clickListener;
    private List<Book> exampleList;

    public interface onMeClick{
        void onClickThisUser(View view, int position);
    }

    public class MyTViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView book_photo;
        public TextView info, author;
        ItemClickListener clickListener;

        public MyTViewHolder(View view) {
            super(view);
            book_photo = view.findViewById(R.id.book_photo);
            info = view.findViewById(R.id.info);
            author = view.findViewById(R.id.author);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            this.clickListener.onItemClick(view,getLayoutPosition());
        }

        public void setOnClick(ItemClickListener clickListener){
            this.clickListener = clickListener;
        }
    }

    public BookGridAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
        this.exampleList = bookList;

    }

    @Override
    public MyTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        manageDate();

        return new MyTViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyTViewHolder holder, int position) {
        Book item = bookList.get(position);
        Glide.with(context)
                .load(item.getPhoto())
                .placeholder(R.drawable.item_book)
                .into(holder.book_photo);

        holder.info.setText(item.getName());
        holder.author.setText(item.getAuthor());

        holder.setOnClick(new ItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onItemClick(View v, int pos) {

                Intent intent = new Intent(v.getContext(), OneBookAcvitiy.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("book", bookList.get(pos));
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);

            }
        });

    }

    public void manageDate() {
        dateF = new SimpleDateFormat("dd.MM");//2001.07.04
        date = dateF.format(Calendar.getInstance().getTime());
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

}