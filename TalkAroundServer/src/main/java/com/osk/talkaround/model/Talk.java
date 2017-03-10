package com.osk.talkaround.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.TreeSet;

/**
 * Created by KOsinsky on 19.03.2016.
 */
@XmlRootElement
public class Talk implements Serializable, Comparable {

    protected BigInteger id;
    protected Date creationDate;
    protected String title;
    protected String text;
    protected CustomLocation location;
    protected transient Float distance;
    private TreeSet<Answer> answerList = new TreeSet<Answer>();

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CustomLocation getLocation() {
        return location;
    }

    public void setLocation(CustomLocation location) {
        this.location = location;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public TreeSet<Answer> getAnswerList() {
        return answerList;
    }

    public void setAnswerList(TreeSet<Answer> answerList) {
        this.answerList = answerList;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "Talk{" +
                "id=" + id +
                ", creationDate=" + creationDate +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", longitude=" + location.getLongitude() +
                ", latitude=" + location.getLatitude() +
                '}';
    }

    public int compareTo(Object o) {
        Talk other = (Talk)o;
        if (answerList.isEmpty() && other.getAnswerList().isEmpty()) {
            return creationDate.compareTo(other.creationDate);
        } else if(answerList.isEmpty() && !other.getAnswerList().isEmpty()) {
            return creationDate.compareTo(other.getAnswerList().last().getAnswerDate());
        } else if(!answerList.isEmpty() && other.getAnswerList().isEmpty()) {
            return answerList.last().compareTo(other.creationDate);
        } else {
            return answerList.last().compareTo(((Talk) o).getAnswerList().last());
        }
    }
}
