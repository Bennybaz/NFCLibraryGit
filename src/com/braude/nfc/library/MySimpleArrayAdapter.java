package com.braude.nfc.library;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Lidor on 25/03/14.
 */
public class MySimpleArrayAdapter extends ArrayAdapter<Book> {
    private Context context;
    public ArrayList<Book> values;

    public MySimpleArrayAdapter(Context context, ArrayList<Book> values)
    {
        super(context, R.layout.row_layout, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_layout, parent, false);
        TextView textView1 = (TextView) rowView.findViewById(R.id.headline);
        TextView textView2 = (TextView) rowView.findViewById(R.id.baseline);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.item_image_right);
        textView1.setText(values.get(position).getName());
        textView2.setText(values.get(position).getAuthor());
        imageView.setImageResource(R.drawable.remove_icon);
        imageView.setFocusable(false);
        imageView.setFocusableInTouchMode(false);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                values.remove(pos);
                notifyDataSetChanged();
            }
        });
        return rowView;
    }

}
