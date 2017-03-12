package com.osk.talkaround.client.WebserviceUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.roger.catloadinglibrary.CatLoadingView;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.osk.talkaround.client.WebserviceUtils.WebServiceTask.SERVICE_URL;

/**
 * Created by GZaripov1 on 12.03.2017.
 */

public class UploadImageTask extends AsyncTask<String, String, String> {
    private static final String TAG = "Catty";

    private AppCompatActivity activity;
    private ProgressDialog progressDialog;
    private String path;
    private WebServiceTask wst;
    private CatLoadingView catLoadingView;

    public UploadImageTask(AppCompatActivity activity, String path, WebServiceTask wst) {
        this.activity = activity;
        this.path = path;
        this.wst = wst;
    }

    /**
     * Before starting background thread
     * Show Progress Bar Dialog
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        catLoadingView = new CatLoadingView();
       /* progressDialog = new ProgressDialog(activity);
        progressDialog.show();*/
       catLoadingView.show(activity.getSupportFragmentManager(), TAG);
    }

    /**
     * Downloading file in background thread
     */
    @Override
    protected String doInBackground(String... f_url) {
        String iFileName = "ovicam_temp_vid.mp4";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        String Tag = "fSnd";

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String filename = path.substring(path.lastIndexOf("/") + 1);

        try {
            FileInputStream fileInputStream = new FileInputStream(path);

            URL connectURL = new URL(SERVICE_URL + "/saveImage");

            Log.e(Tag, "Starting Http File Sending to URL");

            // Open a HTTP connection to the URL
            HttpURLConnection conn = (HttpURLConnection) connectURL.openConnection();

            // Allow Inputs
            conn.setDoInput(true);

            // Allow Outputs
            conn.setDoOutput(true);

            // Don't use a cached copy.
            conn.setUseCaches(false);

            // Use a post method.
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Connection", "Keep-Alive");

            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"title\"" + lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);//dos.writeBytes(lineEnd);
            dos.writeBytes(filename);
            //dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + iFileName + "\"" + lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);//dos.writeBytes(lineEnd);

            Log.e(Tag, "Headers are written");

            // create a buffer of maximum size
            int bytesAvailable = fileInputStream.available();

            int maxBufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];

            // read file and write it into form...
            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // close streams
            fileInputStream.close();

            dos.flush();

            Log.e(Tag, "File Sent, Response: " + String.valueOf(conn.getResponseCode()));

            InputStream is = conn.getInputStream();

            // retrieve the response from server
            int ch;

            StringBuilder b = new StringBuilder();
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }
            String s = b.toString();
            Log.i("Response", s);
            dos.close();
            if (!s.isEmpty()) {
                wst.addParam("imageUrl", s);
            }
        } catch (MalformedURLException ex) {
            Log.e(Tag, "URL error: " + ex.getMessage(), ex);
        } catch (IOException ioe) {
            Log.e(Tag, "IO error: " + ioe.getMessage(), ioe);
        }

        return null;
    }

    /**
     * Updating progress bar
     * */

    /**
     * After completing background task
     * Dismiss the progress dialog
     **/
    @Override
    protected void onPostExecute(String file_url) {
        wst.execute(new String[]{WebServiceTask.SERVICE_URL.concat("/postAnswer")});
        catLoadingView.dismiss();
        activity = null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        activity = null;
    }
}