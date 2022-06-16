package kz.incubator.mss.reads.settings_menu;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import kz.incubator.mss.reads.R;
import kz.incubator.mss.reads.book_list_menu.interfaces.ItemClickListener;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.MyTViewHolder> {

    private Context context;
    private List<Language> dataList;

    public class MyTViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.lang_photo) ImageView lang_photo;
        @BindView(R.id.lang_name) TextView lang_name;
        ItemClickListener clickListener;

        public MyTViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            this.clickListener.onItemClick(view, getLayoutPosition());
        }

        public void setOnClick(ItemClickListener clickListener) {
            this.clickListener = clickListener;
        }

    }

    public LanguageAdapter(Context context, List<Language> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public MyTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language, parent, false);

        return new MyTViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyTViewHolder holder, int position) {

        final Language language = dataList.get(position);

        holder.lang_photo.setImageResource(language.getImage());
        holder.lang_name.setText(language.getName());

    }
    @Override
    public int getItemCount() {
        return dataList.size();
    }

}