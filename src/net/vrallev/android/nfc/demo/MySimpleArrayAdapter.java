package net.vrallev.android.nfc.demo;

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
    private ArrayList<Book> values;

    public MySimpleArrayAdapter(Context context, ArrayList<Book> values) {
        super(context, R.layout.row_layout, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_layout, parent, false);
        TextView textView1 = (TextView) rowView.findViewById(R.id.headline);
        TextView textView2 = (TextView) rowView.findViewById(R.id.baseline);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.item_image);
        //textView1.setText(values[position].getName());
        //textView2.setText(values[position].getAuthor());
        textView1.setText(values.get(position).getName());
        textView2.setText(values.get(position).getAuthor());
        // change the icon for Windows and iPhone
        //String s = values[position];
        //if (s.startsWith("iPhone")) {
        //    imageView.setImageResource(R.drawable.no);
        //} else {
        //    imageView.setImageResource(R.drawable.ok);
        //}

        return rowView;
    }

    public void updateEntries(ArrayList<Book> b)
    {
        values=b;
        notifyDataSetChanged();
    }
}
