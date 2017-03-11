package com.osk.talkaround.client.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.osk.talkaroundclient.R;
import com.osk.talkaround.client.activities.DisplayTalkActivity;
import com.osk.talkaround.model.Talk;

import java.util.List;

import static com.osk.talkaround.client.activities.MainActivity.TALK_ID_PARAM;

/**
 * Created by GZaripov1 on 11.03.2017.
 */

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private List<Talk> talksList;



    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView title, message;

        public MessageViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.talk_title);
            message = (TextView) view.findViewById(R.id.talk_message);
        }
    }

    public MessagesAdapter(List<Talk> talksList) {
        this.talksList = talksList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_talk_layout, parent, false);

        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Talk talk = talksList.get(position);
        holder.title.setText(talk.getTitle());
        holder.message.setText(talk.getText());
    }

    @Override
    public int getItemCount() {
        return talksList.size();
    }

    public void setTalksList(List<Talk> talksList) {
        this.talksList = talksList;
    }

    public List<Talk> getTalksList() {
        return talksList;
    }
}
