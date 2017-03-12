package com.osk.talkaround.client.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
public class CreateNewTalkActivity extends AppCompatActivity {

    private GPSTracker mLocationListener;
    private Toolbar toolbar;
    private AppBarLayout appBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_talk);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        appBar = (AppBarLayout) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mLocationListener = GPSTracker.getInstance(this.getApplicationContext(), this);
        if(mLocationListener.isCanGetLocation() ){
            mLocationListener.getLocation();
        } else {
            Toast.makeText(this, "Unabletofind", Toast.LENGTH_SHORT).show();
        }

        final float toolbarElevation =  4 * getResources().getDisplayMetrics().density + 0.5f;

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    appBar.setElevation(toolbarElevation);
                }
            }
        }, 800);
    }

    public void createNewTalk() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.send) {
            createNewTalk();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
