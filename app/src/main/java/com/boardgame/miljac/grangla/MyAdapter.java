package com.boardgame.miljac.grangla;

/**
 * Created by miljac on 7.9.2017..
 */



        import android.app.Activity;
        import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.ImageView;
        import android.widget.TextView;

        import java.util.ArrayList;

public class MyAdapter extends ArrayAdapter<ListItem> {

    LayoutInflater inflater;
    ArrayList<ListItem> objects;
    ViewHolder holder = null;

    public MyAdapter(Context context, int textViewResourceId, ArrayList<ListItem> objects) {
        super(context, textViewResourceId, objects);
        inflater = ((Activity) context).getLayoutInflater();
        this.objects = objects;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {

        ListItem listItem = objects.get(position);
        View row = convertView;

        if (null == row) {
            holder = new ViewHolder();
            row = inflater.inflate(R.layout.row, parent, false);
            //holder.name = (TextView) row.findViewById(R.id.name);
            //holder.place = (TextView) row.findViewById(R.id.place);
            holder.imgThumb = (ImageView) row.findViewById(R.id.imgThumb);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        //holder.name.setText(listItem.name);
        //holder.place.setText(listItem.place);
        holder.imgThumb.setBackgroundResource(listItem.logo);

        return row;
    }

    static class ViewHolder {
        TextView name, place;
        ImageView imgThumb;
    }
}