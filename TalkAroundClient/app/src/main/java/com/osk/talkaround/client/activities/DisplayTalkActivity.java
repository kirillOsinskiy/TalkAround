package com.osk.talkaround.client.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.osk.talkaround.client.adapters.AnswerListArrayAdapter;
import com.example.osk.talkaroundclient.R;
import com.osk.talkaround.model.Answer;
import com.osk.talkaround.model.Talk;
import com.osk.talkaround.client.WebserviceUtils.ResponseHandler;
import com.osk.talkaround.client.WebserviceUtils.WebServiceTask;

import org.json.JSONException;

import java.util.Iterator;

public class DisplayTalkActivity extends AppCompatActivity {

    private ListView answersListView;
    private String talkId;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_talk);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        answersListView = (ListView) findViewById(R.id.answer_list);

        Intent intent = getIntent();
        talkId = intent.getStringExtra(MainActivity.TALK_ID_PARAM);
        getData(talkId);
    }

    private void getData(String id) {
        WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, this, "GETting data...", new ResponseHandler() {
            @Override
            public void handleResponse(Object response) throws JSONException {
                Talk talk = (Talk) response;
                fillAnswerList(talk);
            }
        });
        wst.addParam("talkId", id);
        wst.execute(new String[]{WebServiceTask.SERVICE_URL.concat("/getTalk")});
    }

    public void fillAnswerList(Talk talk) {
        TextView talkTitle = (TextView)findViewById(R.id.talk_title);
        talkTitle.setText(talk.getTitle());

        if(answersListView == null) {
            answersListView = (ListView) findViewById(R.id.answer_list);
        }
        Answer[] answers = new Answer[talk.getAnswerList().size()+1];
        Iterator<Answer> iter = talk.getAnswerList().iterator();
        Answer topMsg = new Answer();
        topMsg.setMessage(talk.getText());
        topMsg.setAnswerDate(talk.getCreationDate());
        topMsg.setOrderNumber(0);
        answers[0] = topMsg;
        for(int i=1;i<talk.getAnswerList().size()+1;i++) {
            answers[i] = iter.next();
        }
        AnswerListArrayAdapter adapter = new AnswerListArrayAdapter(this, answers);
        answersListView.setAdapter(adapter);
    }

    public void addNewAnswer(View view) {
        EditText edAnswerText = (EditText) findViewById(R.id.answer_text);
        String answerText = edAnswerText.getText().toString();
        edAnswerText.getText().clear();
        if (answerText.trim().equals("")) {
            return;
        }
        WebServiceTask wst = new WebServiceTask(WebServiceTask.POST_TASK, this, "Posting data...", new ResponseHandler() {
            @Override
            public void handleResponse(Object response) {
                try {
                    Talk talk = (Talk) response;
                    fillAnswerList(talk);
                } catch (Exception e) {
                    Log.e("WebServiceTask", e.getLocalizedMessage(), e);
                }
            }
        });
        wst.addParam("talkId", talkId);
        wst.addParam("answerText", answerText);

        wst.execute(new String[]{WebServiceTask.SERVICE_URL.concat("/postAnswer")});
    }
}
