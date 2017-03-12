package com.osk.talkaround.client.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.osk.talkaroundclient.R;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.indicator.progresspie.ProgressPieIndicator;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.github.piasy.biv.view.BigImageView;

import static com.osk.talkaround.client.WebserviceUtils.WebServiceTask.SERVICE_URL;
import static com.osk.talkaround.client.activities.DisplayTalkActivity.TAG_ATTACHMENT;

public class ImageViewerActivity extends AppCompatActivity {
    private BigImageView bigImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BigImageViewer.initialize(GlideImageLoader.with(getApplicationContext()));
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        bigImageView = (BigImageView) findViewById(R.id.mBigImage);
        bigImageView.setProgressIndicator(new ProgressPieIndicator());

        Intent intent = getIntent();
        String file = intent.getStringExtra(TAG_ATTACHMENT);
        String url = SERVICE_URL + "/getImage/" + file;

        bigImageView.showImage(Uri.parse(url));
    }
}
