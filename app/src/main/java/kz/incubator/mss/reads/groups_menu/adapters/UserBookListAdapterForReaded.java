package kz.incubator.mss.reads.groups_menu.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import kz.incubator.mss.reads.R;
import kz.incubator.mss.reads.book_list_menu.interfaces.ItemClickListener;
import kz.incubator.mss.reads.book_list_menu.module.Book;

public class UserBookListAdapterForReaded extends RecyclerView.Adapter<UserBookListAdapterForReaded.MyTViewHolder> {
    private Context context;
    private List<Book> bookList;
    DateFormat dateF;
    String date;
    String number;
    ItemClickListener itemClickListener;

    public void setOnItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

    public class MyTViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView info;
        TextView author;
        ImageView notebook;

        public MyTViewHolder(View view) {
            super(view);
            info = view.findViewById(R.id.titleOfBook);
            author = view.findViewById(R.id.author);
            notebook = view.findViewById(R.id.notebook);
            notebook.setImageResource(R.drawable.done);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onItemClick(view,getLayoutPosition());
        }
    }

    public UserBookListAdapterForReaded(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    @Override
    public MyTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.design_of_book, parent, false);
        manageDate();
        return new MyTViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyTViewHolder holder, int position) {
        Book item = bookList.get(position);
        holder.info.setText(item.getName());
        holder.author.setText(item.getAuthor());
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