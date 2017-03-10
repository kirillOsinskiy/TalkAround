package com.osk.talkaround.client.ArrayAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.osk.talkaroundclient.R;
import com.osk.talkaround.client.utils.AnswerUtils;
import com.osk.talkaround.model.Answer;

/**
 * Created by KOsinsky on 20.03.2016.
 */
public class AnswerListArrayAdapter extends ArrayAdapter<Answer> {

    private final Context context;
    private final Answer[] values;

    static class ViewHolder {
        public TextView titleTextView;
        public TextView messageTextView;
        public TextView answerOrdNumber;
    }

    public AnswerListArrayAdapter(Context context, Answer[] values) {
        super(context, R.layout.row_answer_layout, values);
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
            rowView = inflater.inflate(R.layout.row_answer_layout, null, true);
            holder = new ViewHolder();
//            holder.answerOrdNumber = (TextView) rowView.findViewById(R.id.answer_ord_number);
            holder.titleTextView = (TextView) rowView.findViewById(R.id.answer_date);
            holder.messageTextView = (TextView) rowView.findViewById(R.id.answer_message);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
//        holder.answerOrdNumber.setText(String.valueOf(values[position].getOrderNumber()));
        holder.titleTextView.setText(AnswerUtils.getDateString(values[position]));
        holder.messageTextView.setText(values[position].getMessage());

        return rowView;
    }
}
