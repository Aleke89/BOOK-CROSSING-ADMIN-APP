package kz.incubator.mss.reads.about_us_menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import kz.incubator.mss.reads.R;

public class ModeratorsAdapter extends BaseAdapter {
    Context context;
    ArrayList<Moderator> Moderators = new ArrayList<>();
    public ModeratorsAdapter(Context context, ArrayList<Moderator> Moderators){
        this.context = context;
        this.Moderators = Moderators;
    }
    @Override
    public int getCount() {
        return Moderators.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("ResourceType")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.moderator_desing,null);
            TextView name = convertView.findViewById(R.id.name);
            TextView desc = convertView.findViewById(R.id.desc);
            CircleImageView imageView = convertView.findViewById(R.id.imageView);
            RelativeLayout relativeLayout = convertView.findViewById(R.id.backgroundColor);

            desc.setText(Moderators.get(position).getName());
            name.setText(Moderators.get(position).getDesc());
            imageView.setImageResource(Moderators.get(position).getImage());


            relativeLayout.setBackgroundColor(context.getResources().getColor(Moderators.get(position).getColor()));

        }
        return convertView;
    }
}
