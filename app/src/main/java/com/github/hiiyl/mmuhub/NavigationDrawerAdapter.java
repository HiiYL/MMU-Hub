package com.github.hiiyl.mmuhub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Hii on 4/27/15.
 */
public class NavigationDrawerAdapter extends BaseAdapter {
    String[] nav_options;
    int[] nav_icons;
    Context mContext;

    public NavigationDrawerAdapter(Context context,String[] nav_options, int[] nav_icons) {
        this.nav_options = nav_options;
        this.nav_icons = nav_icons;
        mContext = context;
    }

    @Override
    public int getCount() {
        return nav_options.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder {
        TextView tv;
        ImageView img;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;
        Holder holder = null;
        if(convertView == null) {
            holder = new Holder();
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.navigation_list_item, parent, false);
            holder.tv = (TextView) convertView.findViewById(R.id.nav_option_textview);
            holder.img = (ImageView) convertView.findViewById(R.id.nav_option_icon);
            convertView.setTag(holder);
        }else {
            holder =   (Holder) convertView.getTag();
        }
        holder.tv.setText(nav_options[position]);
        holder.img.setImageResource(nav_icons[position]);

        return convertView;
    }
}