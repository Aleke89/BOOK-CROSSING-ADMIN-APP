package kz.incubator.mss.reads.groups_menu.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import kz.incubator.mss.reads.R;
import kz.incubator.mss.reads.book_list_menu.interfaces.ItemClickListener;
import kz.incubator.mss.reads.groups_menu.module.Groups;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.MyTViewHolder> {

    private Context context;
    private List<Groups> groupList;
    TypedArray gradientStore, subjectStore;

    public class MyTViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView groupName, personCount, groupPoint;
        RelativeLayout gradientLayout;
        ImageView groupPhoto;
        ItemClickListener clickListener;

        public MyTViewHolder(View view) {
            super(view);
            groupName = view.findViewById(R.id.groupName);
            personCount = view.findViewById(R.id.personCount);
            groupPoint = view.findViewById(R.id.groupPoint);
            groupPhoto = view.findViewById(R.id.groupPhoto);
            gradientLayout = view.findViewById(R.id.gradientLayout);

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

    public GroupListAdapter(Context context, List<Groups> groupList) {
        this.context = context;
        this.groupList = groupList;
        gradientStore = context.getResources().obtainTypedArray(R.array.gradientStore);
        subjectStore = context.getResources().obtainTypedArray(R.array.subjectStore);
    }

    @Override
    public MyTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_groups2, parent, false);

        return new MyTViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyTViewHolder holder, int position) {

        final Groups item = groupList.get(position);

        holder.groupName.setText(item.getGroup_name());
        holder.personCount.setText("" + item.getPerson_count());
        holder.groupPoint.setText("" + (item.getSum_point() / (item.getPerson_count() == 0?1:item.getPerson_count())));
//        holder.groupPlace.setText(""+(position+1));

        int gradientBack = gradientStore.getResourceId(position, 0);
        int subjectImage = subjectStore.getResourceId(position, 0);

        holder.gradientLayout.setBackground(context.getResources().getDrawable(gradientBack));
        holder.groupPhoto.setBackground(context.getResources().getDrawable(subjectImage));

//        holder.setOnClick(new ItemClickListener() {
//            @Override
//            public void onItemClick(View v, int pos) {
//
//                Intent intent = new Intent(context, GroupUsersActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("groupName", item.getGroup_name());
//                bundle.putSerializable("groupId", item.getGroup_id());
//                intent.putExtras(bundle);
//                context.startActivity(intent);
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

}