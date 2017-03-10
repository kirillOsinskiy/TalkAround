package com.osk.talkaround;

import com.osk.talkaround.model.Answer;
import com.osk.talkaround.model.CustomLocation;
import com.osk.talkaround.model.Talk;
import com.osk.talkaround.utils.CustomLocationUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by KOsinsky on 19.03.2016.
 */
public class DataAccessService {

    public final static String TALK_ID = "talkId";
    public final static String TALK_CREATIONDATE = "creationdate";
    public final static String TALK_TITLE = "talkTitle";
    public final static String TALK_TEXT = "talkText";
    public final static String TALK_LATITUDE = "talkLatitude";
    public final static String TALK_LONGITUDE = "talkLongitude";
    public final static String TALK_DISTANCE = "distance";
    public final static String ANSWER_TEXT = "answerText";

    private final static String TALK_TABLE_NAME = "talk";

    private static volatile List<Talk> talkList = new ArrayList<Talk>();

    private static BigInteger talkIdSeq = null;
    private static BigInteger answerIdSeq = null;

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.postgresql.Driver";
    static final String DB_URL = "jdbc:postgresql://localhost:5432/talkaroundbase";

    //  Database credentials
    static final String USER = "talkaround";
    static final String PASS = "talkaround";

    private static volatile DataAccessService instance;

    public static DataAccessService getInstance() {
        DataAccessService localInstance = instance;
        if (localInstance == null) {
            synchronized (DataAccessService.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new DataAccessService();
                }
            }
        }
        return localInstance;
    }

    private DataAccessService() {
        try {
            Connection conn = openNewConnection();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Connection openNewConnection() throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_DRIVER);
        Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
        System.out.println("Opened database successfully");
        return  connection;
    }

    public InputStream createNewTalkInputStream(InputStream inputStream) throws IOException, ClassNotFoundException {
        return getInputStreamFromObject(createNewTalk(inputStream));
    }

    public InputStream getTalksInputStream() throws IOException {
        return getInputStreamFromObject(talkList);
    }

    public InputStream getAvailableTalks(Double longitude, Double latitude, Float distance) throws IOException {
        ArrayList<Talk> resultList = new ArrayList<Talk>();
//        try {
//            resultList = getTalksForLocation(longitude, latitude, distance);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
        CustomLocation location = new CustomLocation(longitude, latitude);
        for (Talk talk : talkList) {
            Float distanceBetween = CustomLocationUtils.distanceTo(location, talk.getLocation());
            System.out.println("Distance between user and talk " + talk.getTitle() + " is " + distanceBetween);
            if(distanceBetween <= distance) {
                talk.setDistance(distanceBetween);
                resultList.add(talk);
            }
        }
        return getInputStreamFromObject(resultList);
    }

    private ArrayList<Talk> getTalksForLocation(Double longitude, Double latitude, Float distance) throws SQLException, ClassNotFoundException {
        Connection conn = openNewConnection();
        Statement statement = null;
        statement = conn.createStatement();
//        String query = String.format("SELECT talk.*, " +
//                "(point("+longitude+","+latitude+") <@> point(talk.longitude, talk.latitude)) as distance FROM %s talk " +
//                "WHERE distance<" + distance, TALK_TABLE_NAME);
        String query = String.format("SELECT talk.* FROM %s talk",TALK_TABLE_NAME);
        System.out.println(query);
        ResultSet result = statement.executeQuery(query);
        ArrayList<Talk> talks = new ArrayList<Talk>();
        while (result.next()) {
            talks.add(createTalk(BigInteger.valueOf(result.getInt(TALK_ID)), result.getDate(TALK_CREATIONDATE),
                                 result.getString(TALK_TITLE),result.getString(TALK_TEXT),
                                 result.getDouble(TALK_LONGITUDE), result.getDouble(TALK_LATITUDE)));
        }
        return talks;
    }

    public InputStream getTalkByIdInputStream(String talkId) throws IOException {
        return getInputStreamFromObject(getTalkById(talkId));
    }

    public synchronized InputStream addNewAnswerToTalkInputStream(InputStream inputStream) throws IOException, ClassNotFoundException {
        return getInputStreamFromObject(addNewAnswerToTalk(inputStream));
    }

    private Talk createNewTalk(InputStream inputStream) throws IOException, ClassNotFoundException {
        Map<String, String> talkParams = getParamsFromInputStream(inputStream);
        // parse params
        String title = talkParams.get(TALK_TITLE);
        String text = talkParams.get(TALK_TEXT);
        Double longitude = Double.valueOf(talkParams.get(TALK_LONGITUDE));
        Double latitude = Double.valueOf(talkParams.get(TALK_LATITUDE));
        // create new talk
        Talk res = createTalk(title, text, longitude, latitude);
        talkList.add(res);
        return res;
    }

    private Talk getTalkById(String talkId) {
        BigInteger id = new BigInteger(talkId);
        for(Talk t : talkList) {
            if(id.equals(t.getId())) {
                return t;
            }
        }
        throw new RuntimeException(String.format("Talk with ID %s no found!", id));
    }

    private synchronized Talk addNewAnswerToTalk(InputStream inputStream) throws IOException, ClassNotFoundException {
        Map<String, String> talkParams = getParamsFromInputStream(inputStream);
        // parse request params
        String talkId = talkParams.get(TALK_ID);
        String answerText = talkParams.get(ANSWER_TEXT);
        // add answer to talk
        return addNewAnswerToTalk(talkId, answerText);
    }

    private Talk addNewAnswer(Talk talk, String answerText) {
        synchronized (talk) {
            Answer answer = new Answer();
            answer.setId(generateAnswerId());
            answer.setAnswerDate(Calendar.getInstance().getTime());
            answer.setOrderNumber(getLastOrderNumber(talk));
            answer.setMessage(answerText);
            talk.getAnswerList().add(answer);
            return talk;
        }
    }

    private Talk createTalk(BigInteger id, Date creationDate, String title, String text, Double longitude, Double latitude) {
        Talk res = new Talk();
        res.setId(id);
        res.setCreationDate(creationDate);
        res.setTitle(title);
        res.setText(text);
        res.setLocation(new CustomLocation(longitude, latitude));
        return res;
    }

    private Talk createTalk(String title, String text, Double longitude, Double latitude) {
        Talk res = new Talk();
        res.setId(generateTalkId());
        res.setCreationDate(Calendar.getInstance().getTime());
        res.setTitle(title);
        res.setText(text);
        res.setLocation(new CustomLocation(longitude, latitude));
        return res;
    }

    private synchronized BigInteger generateTalkId() {
        if(talkIdSeq==null) {
            talkIdSeq = new BigInteger("0");
        } else {
            talkIdSeq = talkIdSeq.add(new BigInteger("1"));
        }
        return talkIdSeq;
    }

    private synchronized BigInteger generateAnswerId() {
        if(answerIdSeq==null) {
            answerIdSeq = new BigInteger("0");
        } else {
            answerIdSeq = answerIdSeq.add(new BigInteger("1"));
        }
        return answerIdSeq;
    }

    private long getLastOrderNumber(Talk talk) {
        synchronized (talk) {
            return talk.getAnswerList().isEmpty() ? 1 : talk.getAnswerList().last().getOrderNumber() + 1;
        }
    }

    private synchronized Talk addNewAnswerToTalk(String talkId, String answerText) {
        Talk talk = this.getTalkById(talkId);
        talk = addNewAnswer(talk, answerText);
        // DEBUG info
        System.out.println("Adding new answer to talk with ID: " + talkId);
        System.out.println("Talk has answers:");
        for(Answer answer : talk.getAnswerList()) {
            System.out.println(String.format("%d: %s", answer.getOrderNumber(), answer.getMessage()));
        }
        return talk;
    }

    private static Map<String, String> getParamsFromInputStream(InputStream inputStream) throws IOException, ClassNotFoundException {
        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (Map<String, String>)objectInputStream.readObject();
        } finally {
            inputStream.close();
        }
    }

    private static InputStream getInputStreamFromObject(Object object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();
        objectOutputStream.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
}
