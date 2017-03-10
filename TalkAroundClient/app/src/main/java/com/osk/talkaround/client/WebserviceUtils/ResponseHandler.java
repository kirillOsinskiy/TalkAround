package com.osk.talkaround.client.WebserviceUtils;

import org.json.JSONException;

/**
 * Created by KOsinsky on 19.03.2016.
 */
public interface ResponseHandler {
    void handleResponse(Object response) throws JSONException;
}
