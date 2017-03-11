package com.osk.talkaround.client.WebserviceUtils;

import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.client.methods.HttpRequestBaseHC4;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntityHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kirill on 23.03.2016.
 */
public class WebServiceTask extends AsyncTask<String, Integer, Object> {

    public static final String SERVICE_URL = "http://91.225.131.148:8191/TalkAroundServer/rest/talk";

    public static final int POST_TASK = 1;
    public static final int GET_TASK = 2;

    private ResponseHandler responseHandler;

    private static final String TAG = "WebServiceTask";

    // connection timeout, in milliseconds (waiting to connect)
    private static final int CONN_TIMEOUT = 3000;

    // socket timeout, in milliseconds (waiting for data)
    private static final int SOCKET_TIMEOUT = 5000;

    private int taskType = GET_TASK;
    private Context mContext = null;
    private String processMessage = "Processing...";

    private HashMap<String, String> paramsNew = new HashMap<>();

    private ProgressDialog pDlg = null;

    public WebServiceTask(int taskType, Context mContext, String processMessage, ResponseHandler responseHandler) {
        this.taskType = taskType;
        this.mContext = mContext;
        this.processMessage = processMessage;
        this.responseHandler = responseHandler;
    }

    public void addParam(String name, String value) {
        paramsNew.put(name, value);
    }

    private void showProgressDialog() {
        pDlg = new ProgressDialog(mContext);
        pDlg.setMessage(processMessage);
        pDlg.setProgressDrawable(WallpaperManager.getInstance(mContext).getDrawable());
        pDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDlg.setCancelable(false);
        pDlg.show();
    }

    @Override
    protected void onPreExecute() {
        showProgressDialog();
    }

    protected Object doInBackground(String... urls) {
        String url = urls[0];
        Object result = null;
        try {
            result = doRequestForUrl(url);
        } catch (URISyntaxException | IOException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
        return result;
    }

    private Object processResponse(CloseableHttpResponse response) {
        Object result = null;
        if (response == null) {
            return result;
        } else {
            try {
                result = inputStreamToObject(response.getEntity().getContent());
            } catch (IOException | ClassNotFoundException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Object response) {
        try {
            responseHandler.handleResponse(response);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
        pDlg.dismiss();
        mContext = null;
    }

    private Object doRequestForUrl(String url) throws IOException, URISyntaxException {
        HttpRequestBaseHC4 httpRequest = null;
        URIBuilder uriBuilder = new URIBuilder(url);
        try {
            switch (taskType) {
                case POST_TASK:
                    httpRequest = new HttpPostHC4(uriBuilder.build());
                    InputStreamEntityHC4 streamEntity = new InputStreamEntityHC4(getInputStreamParams(paramsNew));
                    ((HttpPostHC4) httpRequest).setEntity(streamEntity);
                    break;
                case GET_TASK:
                    for(Map.Entry<String, String> entry : paramsNew.entrySet()) {
                        uriBuilder.addParameter(entry.getKey(), entry.getValue());
                    }
                    httpRequest = new HttpGetHC4(uriBuilder.build());
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
        Object result = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            CloseableHttpResponse response = httpclient.execute(httpRequest);
            try {
                if (response == null) {
                    return result;
                }
                result = processResponse(response);
            } finally {
                if(response!=null) {
                    response.close();
                }
            }
        } finally {
            httpclient.close();
        }
        return result;
    }

    private InputStream getInputStreamParams(HashMap<String, String> paramsNew) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(paramsNew);
        oos.flush();
        oos.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private Object inputStreamToObject(InputStream inputStream) throws IOException, ClassNotFoundException {
        byte[] bytes = IOUtils.toByteArray(inputStream);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return objectInputStream.readObject();
    }

    private String inputStreamToString(InputStream is) {
        String line = "";
        StringBuilder total = new StringBuilder();
        // Wrap a BufferedReader around the InputStream
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        try {
            // Read response until the end
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
        // Return full string
        return total.toString();
    }
}
