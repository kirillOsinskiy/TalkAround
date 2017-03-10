package com.osk.talkaround.client.utils;

import com.osk.talkaround.model.Answer;

import java.text.SimpleDateFormat;

/**
 * Created by Kirill on 02.04.2016.
 */
public class AnswerUtils {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss  dd.MM.yyyy");

    public static String getDateString(Answer answer) {
        return dateFormat.format(answer.getAnswerDate());
    }
}
