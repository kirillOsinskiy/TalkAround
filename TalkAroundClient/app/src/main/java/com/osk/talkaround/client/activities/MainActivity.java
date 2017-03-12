package com.osk.talkaround.client.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.osk.talkaroundclient.R;
import com.osk.talkaround.client.WebserviceUtils.ResponseHandler;
import com.osk.talkaround.client.WebserviceUtils.WebServiceTask;
import com.osk.talkaround.client.adapters.ViewPagerAdapter;
import com.osk.talkaround.client.utils.GPSTracker;
import com.osk.talkaround.model.Talk;

import org.json.JSONException;

import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    public static final String TALK_ID_PARAM = "talkIdParam";
    public static final String TALK_MSG_PARAM = "talkMsgParam";
    private static final int REQUEST_LOCATION_PERMISSIONS = 25123;
    public static final int DISTANCE_SMALL = 100;
    public static final int DISTANCE_MEDIUM = 500;
    public static final int DISTANCE_BIG = 1000;

    public static int curDist = DISTANCE_SMALL;
    private ViewPagerAdapter adapter;
    private Toolbar toolbar;
    //private TextView tvTalksCount;
    private ImageView smallLocBtn;
    private ImageView mediumLocBtn;
    private ImageView bigLocBtn;

    /**
     * Called when the activity is first created.
     */
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

        // tvTalksCount = (TextView) findViewById(R.id.talksCount);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSIONS: {
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    getData(curDist);
                }
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //getData(curDist);
    }


    public void setLocationSmall(View view) {
        smallLocBtn.setImageResource(R.drawable.round_selected);
        mediumLocBtn.setImageResource(R.drawable.round_unselected);
        bigLocBtn.setImageResource(R.drawable.round_unselected);

        curDist = DISTANCE_SMALL;
        getData(DISTANCE_SMALL);
        adapter.onDistChanged(DISTANCE_SMALL);
    }

/*    public void startNewTalk(View vw) {

    }*/

    public void setLocationMedium(View view) {
        smallLocBtn.setImageResource(R.drawable.round_unselected);
        mediumLocBtn.setImageResource(R.drawable.round_selected);
        bigLocBtn.setImageResource(R.drawable.round_unselected);

        curDist = DISTANCE_MEDIUM;
        getData(DISTANCE_MEDIUM);
        adapter.onDistChanged(DISTANCE_MEDIUM);

    }

    public void setLocationBig(View view) {
        smallLocBtn.setImageResource(R.drawable.round_unselected);
        mediumLocBtn.setImageResource(R.drawable.round_unselected);
        bigLocBtn.setImageResource(R.drawable.round_selected);

        curDist = DISTANCE_BIG;
        getData(DISTANCE_BIG);
        adapter.onDistChanged(DISTANCE_BIG);
    }

    public void refreshData() {
        getData(curDist);
    }

    private void getData(int distance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSIONS);
                return;
            }
        }
        WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, this, "GETting data...", new ResponseHandler() {
            @Override
            public void handleResponse(Object response) throws JSONException {
                if (response instanceof List) {
                    List<Talk> talkList = (List<Talk>) response;
                    Talk[] talks = talkList.toArray(new Talk[talkList.size()]);
                    adapter.updateTalks(talks);
                    //tvTalksCount.setText(String.format("Found %d talk(s)", talkList.size()));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.add) {
            Intent intent = new Intent(this, CreateNewTalkActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
