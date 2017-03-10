package com.osk.talkaround.client.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.osk.talkaround.client.ArrayAdapters.TalkListArrayAdapter;
import com.example.osk.talkaroundclient.R;
import com.osk.talkaround.client.WebserviceUtils.ResponseHandler;
import com.osk.talkaround.client.WebserviceUtils.WebServiceTask;
import com.osk.talkaround.model.Talk;
import com.osk.talkaround.client.utils.GPSTracker;

import org.json.JSONException;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TALK_ID_PARAM = "talkIdParam";
    public static final String TALK_MSG_PARAM = "talkMsgParam";
    public static final int DISTANCE_SMALL = 5;
    public static final int DISTANCE_MEDIUM = 50;
    public static final int DISTANCE_BIG = 500;

    public static int curDist = DISTANCE_SMALL;

    ListView talksListView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        talksListView = (ListView) findViewById(R.id.talkList);
        // ListView Item Click Listener
        talksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Intent intent = new Intent(view.getContext(), DisplayTalkActivity.class);
                Talk itemValue = (Talk) talksListView.getItemAtPosition(position);
                intent.putExtra(TALK_ID_PARAM, String.valueOf(itemValue.getId()));
                startActivity(intent);
            }

        });
        getData(curDist);
    }

    public void retrieveSampleData(View vw) {
        getData(curDist);
    }

    public void setLocationSmall(View vw) {
        ImageButton smallLocBtn = (ImageButton)findViewById(R.id.smallLocBtn);
        ImageButton mediumLocBtn = (ImageButton)findViewById(R.id.mediumLocBtn);
        ImageButton bigLocBtn = (ImageButton)findViewById(R.id.bigLocBtn);

        smallLocBtn.setImageResource(R.drawable.radius_100_2);
        mediumLocBtn.setImageResource(R.drawable.radius_500_1);
        bigLocBtn.setImageResource(R.drawable.radius_1000_1);
        curDist = DISTANCE_SMALL;
        getData(DISTANCE_SMALL);
    }

    public void setLocationMedium(View vw) {
        ImageButton smallLocBtn = (ImageButton)findViewById(R.id.smallLocBtn);
        ImageButton mediumLocBtn = (ImageButton)findViewById(R.id.mediumLocBtn);
        ImageButton bigLocBtn = (ImageButton)findViewById(R.id.bigLocBtn);

        smallLocBtn.setImageResource(R.drawable.radius_100_1);
        mediumLocBtn.setImageResource(R.drawable.radius_500_2);
        bigLocBtn.setImageResource(R.drawable.radius_1000_1);
        curDist = DISTANCE_MEDIUM;
        getData(DISTANCE_MEDIUM);
    }

    public void setLocationBig(View vw) {
        ImageButton smallLocBtn = (ImageButton)findViewById(R.id.smallLocBtn);
        ImageButton mediumLocBtn = (ImageButton)findViewById(R.id.mediumLocBtn);
        ImageButton bigLocBtn = (ImageButton)findViewById(R.id.bigLocBtn);

        smallLocBtn.setImageResource(R.drawable.radius_100_1);
        mediumLocBtn.setImageResource(R.drawable.radius_500_1);
        bigLocBtn.setImageResource(R.drawable.radius_1000_2);

        curDist = DISTANCE_BIG;
        getData(DISTANCE_BIG);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData(curDist);
    }

    private void getData(int distance) {
        WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, this, "GETting data...", new ResponseHandler() {
            @Override
            public void handleResponse(Object response) throws JSONException {
                List<Talk> talkList = (List<Talk>) response;
                fillTalksList(talkList.toArray(new Talk[talkList.size()]));
                TextView tvTalksCount = (TextView)  findViewById(R.id.talksCount);
                tvTalksCount.setText(String.format("Found %d talk(s)", talkList.size()));
            }
        });

        wst.addParam("talkLongitude", String.valueOf(GPSTracker.getInstance(
                this.getApplicationContext(), this).getLongitude()));
        wst.addParam("talkLatitude", String.valueOf(GPSTracker.getInstance(
                this.getApplicationContext(), this).getLatitude()));
        wst.addParam("distance", String.valueOf(distance));

        wst.execute(new String[]{WebServiceTask.SERVICE_URL.concat("/getTalksWithParams")});
    }

    public void startNewTalk(View vw) {
        Intent intent = new Intent(this, CreateNewTalkActivity.class);
        startActivity(intent);
    }

    public void fillTalksList(Talk[] talks) {
        if(talksListView == null) {
            talksListView = (ListView) findViewById(R.id.talkList);
        }
        TalkListArrayAdapter adapter = new TalkListArrayAdapter(this, talks);
        talksListView.setAdapter(adapter);
    }
}
