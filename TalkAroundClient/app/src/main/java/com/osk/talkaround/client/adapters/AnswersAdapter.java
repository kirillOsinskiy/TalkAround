package com.osk.talkaround.client.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.osk.talkaroundclient.R;
import com.makeramen.roundedimageview.RoundedImageView;
import com.osk.talkaround.client.utils.AnswerUtils;
import com.osk.talkaround.model.Answer;
import com.osk.talkaround.model.Talk;

import java.util.List;

import static com.osk.talkaround.client.WebserviceUtils.WebServiceTask.SERVICE_URL;

/**
 * Created by GZaripov1 on 12.03.2017.
 */

public class AnswersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Answer> answerList;



    private class AnswerViewHolder extends RecyclerView.ViewHolder {
        TextView title, message;

        AnswerViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.talk_title);
            message = (TextView) view.findViewById(R.id.talk_message);
        }
    }

    private class ImageViewHolder extends RecyclerView.ViewHolder {
        TextView title, message;
        private RoundedImageView iv;

        ImageViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.talk_title);
            message = (TextView) view.findViewById(R.id.talk_message);
            iv = (RoundedImageView) view.findViewById(R.id.iv);
        }
    }

    public AnswersAdapter(List<Answer> answerList) {
        this.answerList = answerList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == 0) {
            itemView = LayoutInflater.from(parent.getContext().getApplicationContext())
                    .inflate(R.layout.row_answer_layout, parent, false);
            return new AnswerViewHolder(itemView);
        } else {
            itemView = LayoutInflater.from(parent.getContext().getApplicationContext())
                    .inflate(R.layout.row_answer_image_layout, parent, false);
            return new ImageViewHolder(itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return answerList.get(position).getAttachment() == null ? 0 : 2;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Answer answer = answerList.get(position);
        if (holder.getItemViewType() == 0) {
            AnswerViewHolder holder1 = (AnswerViewHolder) holder;
            holder1.title.setText(AnswerUtils.getDateString(answer));
            holder1.message.setText(answer.getMessage());
        } else {
            ImageViewHolder holder1 = (ImageViewHolder) holder;
            holder1.title.setText(AnswerUtils.getDateString(answer));
            holder1.message.setText(answer.getMessage());

            String url = SERVICE_URL + "/getImage/" + answer.getAttachment();

            Glide.with(holder.itemView.getContext()).load(url)
                    .crossFade()
                    .thumbnail(0.35f)
                    //.bitmapTransform(new CircleTransform(this))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder1.iv);
        }
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
