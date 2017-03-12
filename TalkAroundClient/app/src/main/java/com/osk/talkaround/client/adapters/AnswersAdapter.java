package com.osk.talkaround.client.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.osk.talkaroundclient.R;
import com.osk.talkaround.client.utils.AnswerUtils;
import com.osk.talkaround.model.Answer;
import com.osk.talkaround.model.Talk;

import java.util.List;

/**
 * Created by GZaripov1 on 12.03.2017.
 */

public class AnswersAdapter extends RecyclerView.Adapter<AnswersAdapter.AnswerViewHolder> {
    private List<Answer> answerList;



    public class AnswerViewHolder extends RecyclerView.ViewHolder {
        public TextView title, message;

        public AnswerViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.talk_title);
            message = (TextView) view.findViewById(R.id.talk_message);
        }
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public TextView title, message;

        public ImageViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.talk_title);
            message = (TextView) view.findViewById(R.id.talk_message);
        }
    }

    public AnswersAdapter(List<Answer> answerList) {
        this.answerList = answerList;
    }

    @Override
    public AnswerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext().getApplicationContext())
                .inflate(R.layout.row_answer_layout, parent, false);

        return new AnswerViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        return answerList.get(position).getAttachment() == null ? 0 : 2;
    }

    @Override
    public void onBindViewHolder(AnswerViewHolder holder, int position) {
        Answer answer = answerList.get(position);

        holder.title.setText(AnswerUtils.getDateString(answer));
        holder.message.setText(answer.getMessage());
    }

    @Override
    public int getItemCount() {
        return answerList.size();
    }

    public void setAnswerList(List<Answer> answerList) {
        this.answerList = answerList;
    }

    public List<Answer> getAnswerList() {
        return answerList;
    }
}
