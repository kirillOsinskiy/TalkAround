package com.osk.talkaround.client.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.osk.talkaroundclient.R;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.osk.talkaround.client.WebserviceUtils.ResponseHandler;
import com.osk.talkaround.client.WebserviceUtils.UploadImageTask;
import com.osk.talkaround.client.WebserviceUtils.WebServiceTask;
import com.osk.talkaround.client.adapters.AnswersAdapter;
import com.osk.talkaround.model.Answer;
import com.osk.talkaround.model.Talk;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static com.osk.talkaround.client.activities.MainActivity.TALK_ID_PARAM;

public class DisplayTalkActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_EXTERNAL_STORAGE = 22;
    public static final String TAG_ATTACHMENT = "att";
    private RecyclerView recyclerView;
    private String talkId;
    private AnswersAdapter adapter;
    private ImagePicker imagePicker;
    private Toolbar toolbar;
    private ChosenImage image;
    private AppBarLayout appBar;
    private TextView tvAttach;
    private LinearLayout ll_writeBox;

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
        setupRecyclerView();
        tvAttach = (TextView) findViewById(R.id.txtAttach);
        Intent intent = getIntent();
        talkId = intent.getStringExtra(MainActivity.TALK_ID_PARAM);
        appBar = (AppBarLayout) findViewById(R.id.app_bar) ;
        getData(talkId);
        ll_writeBox = (LinearLayout) findViewById(R.id.ll_writeBox);
        final float toolbarElevation =  4 * getResources().getDisplayMetrics().density + 0.5f;

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    appBar.setElevation(toolbarElevation);
                    ll_writeBox.setElevation(2*toolbarElevation);
                }
            }
        }, 800);
    }

    private void setupRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AnswersAdapter(new ArrayList<Answer>());
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (adapter.getItemViewType(position) == 0) return;
                Intent intent = new Intent(DisplayTalkActivity.this, ImageViewerActivity.class);
                Answer itemValue = adapter.getAnswerList().get(position);
                intent.putExtra(TAG_ATTACHMENT, itemValue.getAttachment());
                startActivity(intent);
            }

            @Override
            public void onLongItemClick(View view, int position) {
                // do whatever
            }
        }));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
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
        TextView talkTitle = (TextView) findViewById(R.id.talk_title);
        talkTitle.setText(talk.getTitle());

        Answer[] answers = new Answer[talk.getAnswerList().size() + 1];
        Iterator<Answer> iter = talk.getAnswerList().iterator();
        Answer topMsg = new Answer();
        topMsg.setMessage(talk.getText());
        topMsg.setAnswerDate(talk.getCreationDate());
        topMsg.setOrderNumber(0);
        answers[0] = topMsg;
        for (int i = 1; i < talk.getAnswerList().size() + 1; i++) {
            answers[i] = iter.next();
        }
        adapter.setAnswerList(Arrays.asList(answers));
        adapter.notifyDataSetChanged();
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
        if (image != null) {
            UploadImageTask uploadImageTask =
                    new UploadImageTask(this, image.getOriginalPath(), wst);
            uploadImageTask.execute();
            tvAttach.setVisibility(View.GONE);
            image = null;
            return;
        }
        wst.execute(new String[]{WebServiceTask.SERVICE_URL.concat("/postAnswer")});

}

    public void attachImage(View view) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_EXTERNAL_STORAGE);
                return;
            }
        }


        imagePicker = new ImagePicker(DisplayTalkActivity.this);
        imagePicker.setImagePickerCallback(new ImagePickerCallback() {
            @Override
            public void onImagesChosen(List<ChosenImage> images) {
                image = images.get(0);
                tvAttach.setVisibility(View.VISIBLE);
                tvAttach.setText("Attached: " + image.getDisplayName() + " (" + image.getHumanReadableSize(true) + ")");
            }
            @Override
            public void onError(String message) {
                // Do error handling
            }
        });
        //imagePicker.allowMultiple();
        imagePicker.pickImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == Picker.PICK_IMAGE_DEVICE) {
                imagePicker.submit(data);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    attachImage(null);
                }
            }

        }
    }

}
