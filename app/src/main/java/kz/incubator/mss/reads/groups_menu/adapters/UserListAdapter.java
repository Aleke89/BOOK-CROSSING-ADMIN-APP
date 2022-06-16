package kz.incubator.mss.reads.groups_menu.adapters;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import kz.incubator.mss.reads.R;
import kz.incubator.mss.reads.groups_menu.module.User;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.MyTViewHolder>{
    private Context context;
    public ArrayList<User> userList;
    String [] monthStore;

    public static class MyTViewHolder extends RecyclerView.ViewHolder{
        public TextView userPoint, info, bookCount, userRating, tv_enter_date, userReview;
        CircleImageView person_photo;
        ImageView userTypeIcon;

        public MyTViewHolder(View view) {
            super(view);
            person_photo = view.findViewById(R.id.person_photo);
            userPoint = view.findViewById(R.id.userPoint);
            info = view.findViewById(R.id.info);
            bookCount = view.findViewById(R.id.bookCount);
            userRating = view.findViewById(R.id.userRating);
            tv_enter_date = view.findViewById(R.id.user_group);
            userReview = view.findViewById(R.id.userReview);
            userTypeIcon = view.findViewById(R.id.userTypeIcon);
        }

    }
    public UserListAdapter(Context context, ArrayList<User> userList) {
        this.context = context;
        this.userList = userList;
        monthStore = context.getResources().getStringArray(R.array.monthStore);
    }

    @Override
    public MyTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rating_user, parent, false);
        return new MyTViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyTViewHolder holder, int position){
        User item = userList.get(position);
        Glide.with(context.getApplicationContext())
                .load(item.getPhoto())
                .placeholder(R.drawable.user_def)
                .dontAnimate()
                .into(holder.person_photo);


        holder.userPoint.setText(""+item.getPoint());
        holder.info.setText(item.getInfo());
        holder.bookCount.setText(""+item.getBookCount());
        holder.userRating.setText(""+item.getRatingInGroups());
        holder.userReview.setText("" + item.getReview_sum());

        if(item.getEnterDate().equals("not")){
            holder.tv_enter_date.setTextColor(context.getResources().getColor(R.color.gradientLightOrange2));
            holder.tv_enter_date.setText(context.getString(R.string.not_logged_in_yet));
        }else{
            String [] dateStr = item.getEnterDate().split("\\.");
            String monthStr = monthStore[Integer.parseInt(dateStr[1])-1];

            holder.tv_enter_date.setText(String.format("%s %s, %s", dateStr[0], monthStr,  dateStr[2]));
            holder.tv_enter_date.setTextColor(context.getResources().getColor(R.color.bronze2));
        }


        int uTypeIcon = R.color.transparent;

        switch (item.getUserType()) {
            case "gold":
                uTypeIcon = R.drawable.ic_gold;
                break;
            case "silver":
                uTypeIcon = R.drawable.ic_silver;
                break;
            case "bronze":
                uTypeIcon = R.drawable.ic_bronze;
                break;
        }

        holder.userTypeIcon.setImageResource(uTypeIcon);
    }

    private boolean isOnline() {
        if (isNetworkAvailable()) {
            return true;

        } else {
            Toast.makeText(context, context.getResources().getString(R.string.inetConnection), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

}