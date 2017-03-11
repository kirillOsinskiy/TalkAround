package com.osk.talkaround.client.activities;

import com.osk.talkaround.model.Talk;

/**
 * Created by GZaripov1 on 11.03.2017.
 */

public interface OnDataUpdate {
    void updateTalks(Talk[] talks);
    void onDistChanged(int metres);
}
