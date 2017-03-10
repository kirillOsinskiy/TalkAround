package com.osk.talkaround.client.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.osk.talkaroundclient.R;
import com.osk.talkaround.client.WebserviceUtils.ResponseHandler;
import com.osk.talkaround.client.WebserviceUtils.WebServiceTask;
import com.osk.talkaround.client.utils.GPSTracker;

/**
 * Created by KOsinsky on 19.03.2016.
 */
public class CreateNewTalkActivity extends Activity {

    private GPSTracker mLocationListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_talk);
        mLocationListener = GPSTracker.getInstance(this.getApplicationContext(), this);
        if(mLocationListener.isCanGetLocation() ){
            mLocationListener.getLocation();
        } else {
            Toast.makeText(this, "Unabletofind", Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelTalk(View view) {
        finish();
    }

    public void createNewTalk(View view) {
        EditText edTalkTitle = (EditText) findViewById(R.id.talk_title);
        EditText edTalkText = (EditText) findViewById(R.id.talk_text);

        String talkTitle = edTalkTitle.getText().toString();
        String talkText = edTalkText.getText().toString();

        if (talkTitle.trim().equals("") || talkText.trim().equals("")) {
            Toast.makeText(this, "Please enter in all required fields.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        WebServiceTask wst = new WebServiceTask(WebServiceTask.POST_TASK, this, "Posting data...", new ResponseHandler() {
            @Override
            public void handleResponse(Object response) {
                finish();
            }
        });

        wst.addParam("talkTitle", talkTitle);
        wst.addParam("talkText", talkText);
        wst.addParam("talkLatitude", String.valueOf(mLocationListener.getLatitude()));
        wst.addParam("talkLongitude", String.valueOf(mLocationListener.getLongitude()));

        // the passed String is the URL we will POST to
        wst.execute(new String[]{WebServiceTask.SERVICE_URL.concat("/postTalk")});
    }
}
