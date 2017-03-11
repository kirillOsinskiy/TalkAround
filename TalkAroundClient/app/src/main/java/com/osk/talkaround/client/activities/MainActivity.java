package com.osk.talkaround.client.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.osk.talkaroundclient.R;
import com.google.android.gms.maps.GoogleMap;
import com.osk.talkaround.client.WebserviceUtils.ResponseHandler;
import com.osk.talkaround.client.WebserviceUtils.WebServiceTask;
import com.osk.talkaround.client.adapters.ViewPagerAdapter;
import com.osk.talkaround.client.utils.GPSTracker;
import com.osk.talkaround.model.Talk;

import org.json.JSONException;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TALK_ID_PARAM = "talkIdParam";
    public static final String TALK_MSG_PARAM = "talkMsgParam";

    public static final int DISTANCE_SMALL = 5;
    public static final int DISTANCE_MEDIUM = 50;
    public static final int DISTANCE_BIG = 500;

    public static int curDist = DISTANCE_SMALL;
    private ViewPagerAdapter adapter;
    private Toolbar toolbar;
    private TextView tvTalksCount;
    private ImageView smallLocBtn;
    private ImageView mediumLocBtn;
    private ImageView bigLocBtn;

    private GoogleMap mMap;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0);
        TabLayout tabLayout = ((TabLayout) findViewById(R.id.tabs));
        ViewPager viewPager = ((ViewPager) findViewById(R.id.viewpager));
        String[] titles = getResources().getStringArray(R.array.tab_titles);
        tvTalksCount = (TextView) findViewById(R.id.talksCount);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new MessagesFragment(), titles[0]);
        adapter.addFragment(new MapFragment(), titles[1]);

        smallLocBtn = (ImageView) findViewById(R.id.smallLocBtn);
        mediumLocBtn = (ImageView) findViewById(R.id.mediumLocBtn);
        bigLocBtn = (ImageView) findViewById(R.id.bigLocBtn);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabLayout.setupWithViewPager(viewPager);
        setLocationSmall(null);
    }


    @Override
    protected void onResume() {
        super.onResume();
        getData(curDist);
    }


    public void setLocationSmall(View view) {
        smallLocBtn.setImageResource(R.drawable.round_selected);
        mediumLocBtn.setImageResource(R.drawable.round_unselected);
        bigLocBtn.setImageResource(R.drawable.round_unselected);

        curDist = DISTANCE_SMALL;
        getData(DISTANCE_SMALL);
    }

    public void setLocationMedium(View view) {
        smallLocBtn.setImageResource(R.drawable.round_unselected);
        mediumLocBtn.setImageResource(R.drawable.round_selected);
        bigLocBtn.setImageResource(R.drawable.round_unselected);

        curDist = DISTANCE_MEDIUM;
        getData(DISTANCE_MEDIUM);
    }

    public void setLocationBig(View view) {
        smallLocBtn.setImageResource(R.drawable.round_unselected);
        mediumLocBtn.setImageResource(R.drawable.round_unselected);
        bigLocBtn.setImageResource(R.drawable.round_selected);

        curDist = DISTANCE_BIG;
        getData(DISTANCE_BIG);
    }

    private void getData(int distance) {
        WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, this, "GETting data...", new ResponseHandler() {
            @Override
            public void handleResponse(Object response) throws JSONException {
                if (response instanceof List) {
                    List<Talk> talkList = (List<Talk>) response;
                    Talk[] talks = talkList.toArray(new Talk[talkList.size()]);
                    adapter.updateTalks(talks);
                    tvTalksCount.setText(String.format("Found %d talk(s)", talkList.size()));
                }

            }
        });

        wst.addParam("talkLongitude", String.valueOf(GPSTracker.getInstance(
                this, this).getLongitude()));
        wst.addParam("talkLatitude", String.valueOf(GPSTracker.getInstance(
                this, this).getLatitude()));
        wst.addParam("distance", String.valueOf(distance));

        wst.execute(WebServiceTask.SERVICE_URL.concat("/getTalksWithParams"));
    }



}
