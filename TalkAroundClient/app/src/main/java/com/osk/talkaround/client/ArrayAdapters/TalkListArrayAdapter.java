package com.osk.talkaround.client.ArrayAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.osk.talkaroundclient.R;
import com.osk.talkaround.model.Talk;

/**
 * Created by KOsinsky on 20.03.2016.
 */
public class TalkListArrayAdapter extends ArrayAdapter<Talk> {

    private final Context context;
    private final Talk[] values;

    static class ViewHolder {
        public TextView titleTextView;
//        public TextView distTextView;
        public TextView messageTextView;
    }

    public TalkListArrayAdapter(Context context, Talk[] values) {
        super(context, R.layout.row_talk_layout, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            rowView = inflater.inflate(R.layout.row_talk_layout, null, true);
            holder = new ViewHolder();
            holder.titleTextView = (TextView) rowView.findViewById(R.id.talk_title);
            holder.messageTextView = (TextView) rowView.findViewById(R.id.talk_message);
//            holder.distTextView = (TextView) rowView.findViewById(R.id.distanceTxt);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        holder.titleTextView.setText(values[position].getTitle());
//        holder.distTextView.setText(String.format("(Distance: %s m)",String.valueOf(values[position].getDistance())));
        holder.messageTextView.setText(values[position].getText());

        return rowView;
    }

}
