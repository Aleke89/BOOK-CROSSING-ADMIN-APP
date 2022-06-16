package kz.incubator.mss.reads.book_list_menu.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
import kz.incubator.mss.reads.R;
import kz.incubator.mss.reads.book_list_menu.module.ReviewInBook;
import kz.incubator.mss.reads.groups_menu.activities.UserProfileActivity;

public class UserReviewsAdapter extends RecyclerView.Adapter<UserReviewsAdapter.MyTViewHolder> {
    private Context context;
    private List<ReviewInBook> userList;
    DateFormat dateF;
    String date;
    DatabaseReference userRef;

    public class MyTViewHolder extends RecyclerView.ViewHolder {
        CircleImageView person_photo;
        TextView userName;
        TextView review_text;
        RatingBar bookRating;
        LinearLayout userLinear;

        public MyTViewHolder(View view) {
            super(view);
            person_photo = view.findViewById(R.id.person_photo);
            userName = view.findViewById(R.id.userName);
            review_text = view.findViewById(R.id.review_text);
            bookRating = view.findViewById(R.id.bookRating);
            userLinear = view.findViewById(R.id.userLinear);
        }
    }

    public UserReviewsAdapter(Context context, List<ReviewInBook> userList) {
        this.context = context;
        this.userList = userList;
    }

    @Override
    public MyTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review2, parent, false);
        manageDate();

        return new MyTViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyTViewHolder holder, int position) {
        final ReviewInBook review = userList.get(position);

        holder.userName.setText(review.getUser().getInfo());
        Glide.with(context.getApplicationContext())
                .load(review.getUser().getPhoto())
                .placeholder(R.drawable.user_def)
                .into(holder.person_photo);

        holder.review_text.setText("\"" + review.getReview_text() + "\"");
        holder.bookRating.setRating(review.getUser_rate());

        holder.userLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, UserProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", review.getUser());
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });


    }

    public String checkAbone(String ticketDay) {
        String abone = "norm";

        DateFormat day = new SimpleDateFormat("dd");
        DateFormat month = new SimpleDateFormat("MM");
        DateFormat year = new SimpleDateFormat("yyyy");
        int first_slash = ticketDay.indexOf('/');
        int last_slash = ticketDay.lastIndexOf('/');

        String period_day = ticketDay.substring(0, first_slash);
        String period_month = ticketDay.substring(first_slash + 1, last_slash);
        String period_year = ticketDay.substring(last_slash + 1, ticketDay.length());

        Calendar cal = Calendar.getInstance();

        if (period_year.equals(year.format(cal.getTime()))) {
            if (period_month.equals(month.format(cal.getTime()))) {
                if (period_day.equals(day.format(cal.getTime()))) {
                    abone = "last day";
                } else if (Integer.parseInt(period_day) > Integer.parseInt(day.format(cal.getTime()))) {
                    int counter = Integer.parseInt(period_day) - Integer.parseInt(day.format(cal.getTime()));
                    if (counter <= 7) {
                        abone = counter + " days left";
                    }
                } else {
                    abone = "Your subscription is up";

                }
            } else if (Integer.parseInt(period_month) < Integer.parseInt(month.format(cal.getTime()))) {
                abone = "Your subscription is up";

            }
        } else if (Integer.parseInt(period_year) < Integer.parseInt(year.format(cal.getTime()))) {
            abone = "Your subscription is up";

        }

        return abone;
    }

    public void manageDate() {
        dateF = new SimpleDateFormat("dd.MM");//2001.07.04
        date = dateF.format(Calendar.getInstance().getTime());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}