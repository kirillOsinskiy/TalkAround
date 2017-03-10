package com.osk.talkaround;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by KOsinsky on 19.03.2016.
 */
@Path("/talk")
public class TalkAroundServerEntry {
    // The @Context annotation allows us to have certain contextual objects
    // injected into this class.
    // UriInfo object allows us to get URI information (no kidding).
    @Context
    UriInfo uriInfo;

    // Another "injected" object. This allows us to use the information that's
    // part of any incoming request.
    // We could, for example, get header information, or the requestor's address.
    @Context
    Request request;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String respondAsReady() {
        return "Demo service is ready!";
    }

    @GET
    @Path("getTalks")
    @Produces(MediaType.APPLICATION_JSON)
    public InputStream getAvailableTalks() {
        System.out.println("get talks method");
        try {
            return DataAccessService.getInstance().getTalksInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @GET
    @Path("/getTalksWithParams")
    @Produces(MediaType.APPLICATION_JSON)
    public InputStream getAvailableTalks(
            @QueryParam(DataAccessService.TALK_LONGITUDE) Double longitude,
            @QueryParam(DataAccessService.TALK_LATITUDE) Double latitude,
            @QueryParam(DataAccessService.TALK_DISTANCE) Float distance) {
        System.out.println("Getting talks list for lon:" + longitude + "; lat: " + latitude);
        try {
            return DataAccessService.getInstance().getAvailableTalks(longitude, latitude, distance);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @GET
    @Path("/getTalk")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream getTalk(@QueryParam(DataAccessService.TALK_ID) String talkId) {
        System.out.println("Trying to get talk with ID: " + talkId);
        try {
            return DataAccessService.getInstance().getTalkByIdInputStream(talkId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @POST
    @Path("/postTalk")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream postNewTalk(InputStream inputStream) {
        try {
            return DataAccessService.getInstance().createNewTalkInputStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @POST
    @Path("/postAnswer")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream postAnswer(InputStream inputStream) {
        try {
            return DataAccessService.getInstance().addNewAnswerToTalkInputStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}