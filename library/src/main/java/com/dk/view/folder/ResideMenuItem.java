package com.dk.view.folder;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ResideMenuItem extends LinearLayout {

    /**
     * menu item  icon
     */
    private ImageView iv_icon;
    /**
     * menu item  title
     */
    private TextView tv_title;

    public ResideMenuItem(Context context) {
        super(context);
        initViews(context);
    }

    public ResideMenuItem(Context context, int title) {
        super(context);
        initViews(context);
        tv_title.setText(title);
    }

    public ResideMenuItem(Context context, int icon, String title) {
        super(context);
        initViews(context);
        iv_icon.setBackgroundResource(icon);
        tv_title.setText(title);
//        tv_title.setBackgroundResource(icon);
    }

    private void initViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.residemenu_item1, this);
        iv_icon = (ImageView) findViewById(R.id.iv_icon1);
        tv_title = (TextView) findViewById(R.id.tv_title);
    }

    public void setIcon(int icon) {
        iv_icon.setImageResource(icon);
    }
    public void setTitle(int title) {
        tv_title.setText(title);
    }
    public void setTitle(String title) {
        tv_title.setText(title);
    }
}
