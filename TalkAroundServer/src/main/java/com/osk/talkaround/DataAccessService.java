package com.osk.talkaround;

import com.osk.talkaround.model.Answer;
import com.osk.talkaround.model.CustomLocation;
import com.osk.talkaround.model.Talk;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by KOsinsky on 19.03.2016.
 */
public class DataAccessService {

    public final static String TALK_ID = "talkId";
    public final static String TALK_TITLE = "talkTitle";
    public final static String TALK_TEXT = "talkText";
    public final static String TALK_LATITUDE = "talkLatitude";
    public final static String TALK_LONGITUDE = "talkLongitude";
    public final static String TALK_DISTANCE = "distance";
    public final static String ANSWER_TEXT = "answerText";
    public final static String ANSWER_ATTACHMENT = "imageUrl";

    public final static String IMAGE_QUERY_PARAM = "imageName";

    private static final String SELECT_AVAILABLE_TALKS =
            "SELECT id, creationdate, title, text, longitude, latitude from (" +
                    "SELECT id, creationdate, title, text, longitude, latitude, " +
                    "st_distance_sphere(st_makepoint(?, ?), st_makepoint(talk.longitude, talk.latitude)) AS distance " +
                    "FROM talk) as dist_table where distance < ?";
    private static final String INSERT_NEW_TALK_SQL =
            "INSERT INTO Talk(creationdate, title, text, longitude, latitude) VALUES(?,?,?,?,?)";
    private static final String INSERT_NEW_ANSWER_FOR_TALK_SQL =
            "INSERT INTO answer(talkid, ordernumber, answerdate, message, attachment) VALUES(?,?,?,?,?)";
    private static final String SELECT_TALK_BY_ID = "SELECT * FROM talk WHERE id = ?";
    private static final String SELECT_ANSWERS_BY_TALK_ID = "SELECT * FROM answer WHERE talkid = ?";

    public final static String DB_TALK_ID = "id";
    public final static String DB_TALK_CREATION_DATE = "creationdate";
    public final static String DB_TALK_TITLE = "title";
    public final static String DB_TALK_TEXT = "text";
    public final static String DB_TALK_LATITUDE = "latitude";
    public final static String DB_TALK_LONGITUDE = "longitude";

    private static final String DB_ANSWER_ID = "id";
    private static final String DB_ANSWER_ORDER_NUM = "ordernumber";
    private static final String DB_ANSWER_TALK_ID = "talkid";
    private static final String DB_ANSWER_DATE = "answerdate";
    private static final String DB_ANSWER_MSG = "message";
    private static final String DB_ANSWER_ATTACHMENT = "attachment";
    public static final String UPLOAD_FILE_NAME = "uploadFileName";

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
    private static Connection connection;

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

    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = openNewConnection();
        }
        return connection;
    }

    private DataAccessService() {
        try {
            Connection conn = openNewConnection();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Connection openNewConnection() {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Opened database successfully");
        return connection;
    }

    public InputStream createNewTalkInputStream(InputStream inputStream)
            throws IOException, ClassNotFoundException, SQLException {
        return getInputStreamFromObject(createNewTalk(inputStream));
    }

    public InputStream getTalksInputStream() throws IOException {
        return getInputStreamFromObject(talkList);
    }

    public InputStream getAvailableTalks(Double longitude, Double latitude, Float distance)
            throws IOException, SQLException {
        return getInputStreamFromObject(getTalksForLocation(longitude, latitude, distance));
    }

    private ArrayList<Talk> getTalksForLocation(Double longitude, Double latitude, Float distance) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement(SELECT_AVAILABLE_TALKS);
        statement.setDouble(1, longitude);
        statement.setDouble(2, latitude);
        statement.setFloat(3, distance);
        System.out.println(statement.toString());
        ResultSet result = statement.executeQuery();
        ArrayList<Talk> talks = new ArrayList<>();
        while (result.next()) {
            talks.add(createTalkFromResultSet(result));
        }
        return talks;
    }

    public InputStream getTalkByIdInputStream(String talkId) throws IOException, SQLException {
        return getInputStreamFromObject(getTalkById(talkId));
    }

    public synchronized InputStream addNewAnswerToTalkInputStream(InputStream inputStream)
            throws IOException, SQLException, ClassNotFoundException {
        return getInputStreamFromObject(addNewAnswerToTalk(inputStream));
    }

    private Talk createNewTalk(InputStream inputStream) throws IOException, SQLException {
        Map<String, Object> talkParams = getParamsFromInputStream(inputStream);
        // parse params
        String title = (String) talkParams.get(TALK_TITLE);
        String text = (String) talkParams.get(TALK_TEXT);
        Double longitude = Double.valueOf((String) talkParams.get(TALK_LONGITUDE));
        Double latitude = Double.valueOf((String) talkParams.get(TALK_LATITUDE));
        // create new talk
        Talk res = createTalk(title, text, longitude, latitude);
        storeTalkInDB(res);
        return res;
    }

    private boolean storeTalkInDB(Talk res) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement(INSERT_NEW_TALK_SQL);
        statement.setTimestamp(1, new Timestamp(res.getCreationDate().getTime()));
        statement.setString(2, res.getTitle());
        statement.setString(3, res.getText());
        statement.setDouble(4, res.getLocation().getLongitude());
        statement.setDouble(5, res.getLocation().getLatitude());
        System.out.println(statement.toString());
        return statement.execute();
    }

    private Talk getTalkById(String talkId) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement(SELECT_TALK_BY_ID);
        statement.setInt(1, Integer.valueOf(talkId));
        System.out.println(statement.toString());
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            Talk res = createTalkFromResultSet(result);
            res.setAnswerList(getAnswersForTalk(talkId));
            return res;
        }
        throw new RuntimeException("Talk with id = " + talkId + " not found.");
    }

    private TreeSet<Answer> getAnswersForTalk(String talkId) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement(SELECT_ANSWERS_BY_TALK_ID);
        statement.setInt(1, Integer.valueOf(talkId));
        System.out.println(statement.toString());
        ResultSet result = statement.executeQuery();
        TreeSet<Answer> answers = new TreeSet<>();
        while (result.next()) {
            answers.add(createAnswer(result.getLong(DB_ANSWER_ID),
                    result.getLong(DB_ANSWER_ORDER_NUM),
                    result.getLong(DB_ANSWER_TALK_ID),
                    result.getTimestamp(DB_ANSWER_DATE),
                    result.getString(DB_ANSWER_MSG),
                    result.getString(DB_ANSWER_ATTACHMENT)));
        }
        return answers;
    }

    private Answer createAnswer(long answerId, long orderNumber, long talkId, Date answerDate, String msg,
                                String attachment) {
        Answer answer = new Answer();
        answer.setId(BigInteger.valueOf(answerId));
        answer.setOrderNumber(orderNumber);
        answer.setTalkId(BigInteger.valueOf(talkId));
        answer.setAnswerDate(answerDate);
        answer.setMessage(msg);
        answer.setAttachment(attachment);
        return answer;
    }

    private synchronized Talk addNewAnswerToTalk(InputStream inputStream)
            throws IOException, SQLException, ClassNotFoundException {
        Map<String, Object> talkParams = getParamsFromInputStream(inputStream);
        // parse request params
        String talkId = (String) talkParams.get(TALK_ID);
        String answerText = (String) talkParams.get(ANSWER_TEXT);
        String answerAttachmentData = (String) talkParams.get(ANSWER_ATTACHMENT);
        // add answer to talk
        return addNewAnswerToTalk(talkId, answerText, answerAttachmentData);
    }

    private Talk addNewAnswer(Talk talk, @NotNull String answerText, @Nullable String attachment)
            throws SQLException, ClassNotFoundException {
        Answer answer = new Answer();
        answer.setId(generateAnswerId());
        answer.setTalkId(talk.getId());
        answer.setAnswerDate(Calendar.getInstance().getTime());
        answer.setMessage(answerText);
        answer.setOrderNumber(getLastOrderNumber(talk));
        answer.setAttachment(attachment);

        storeAnswerInDB(answer);
        talk.getAnswerList().add(answer);
        return talk;
    }

    private boolean storeAnswerInDB(Answer answer) throws SQLException, ClassNotFoundException {
        PreparedStatement statement = getConnection().prepareStatement(INSERT_NEW_ANSWER_FOR_TALK_SQL);
        statement.setLong(1, answer.getTalkId().longValue());
        statement.setLong(2, answer.getOrderNumber());
        statement.setTimestamp(3, new Timestamp(answer.getAnswerDate().getTime()));
        statement.setString(4, answer.getMessage());
        statement.setString(5, answer.getAttachment());
        System.out.println(statement.toString());
        return statement.execute();
    }

    private Talk createTalk(BigInteger id, Date creationDate, String title, String text, Double longitude,
                            Double latitude) {
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

    private Talk createTalkFromResultSet(ResultSet result) throws SQLException {
        return createTalk(BigInteger.valueOf(result.getInt(DB_TALK_ID)),
                result.getTimestamp(DB_TALK_CREATION_DATE),
                result.getString(DB_TALK_TITLE),
                result.getString(DB_TALK_TEXT),
                result.getDouble(DB_TALK_LONGITUDE),
                result.getDouble(DB_TALK_LATITUDE));
    }

    private synchronized BigInteger generateTalkId() {
        if (talkIdSeq == null) {
            talkIdSeq = new BigInteger("0");
        } else {
            talkIdSeq = talkIdSeq.add(new BigInteger("1"));
        }
        return talkIdSeq;
    }

    private synchronized BigInteger generateAnswerId() {
        if (answerIdSeq == null) {
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

    private synchronized Talk addNewAnswerToTalk(String talkId, @NotNull String answerText,
                                                 @Nullable String answerAttachment)
            throws SQLException, ClassNotFoundException {
        Talk talk = this.getTalkById(talkId);
        talk = addNewAnswer(talk, answerText, answerAttachment);
        // DEBUG info
        System.out.println("Adding new answer to talk with ID: " + talkId);
        System.out.println("Talk has answers:");
        for (Answer answer : talk.getAnswerList()) {
            System.out.println(String.format("%d: %s", answer.getOrderNumber(), answer.getMessage()));
        }
        return talk;
    }

    private static Map<String, Object> getParamsFromInputStream(InputStream inputStream) throws IOException {
        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            try {
                return (Map<String, Object>) objectInputStream.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return new HashMap<>();
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
