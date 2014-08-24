package com.dopamine.api;

/**
 * Created by pradeepbk4u on 6/23/14.
 */
public class URIBuilder {


    String token;

    public URIBuilder(String token) {
        this.token = token;
    }

    public static enum URI {
        INIT,
        TRACK,
        REWARD
    }

    private final String uri = "https://api.usedopamine.com/v2/app/";


    public  String getURI(URI type) {
        switch(type) {
            case  INIT:
                return uri + token + "/init/";

            case TRACK:
                return uri + token + "/track/";

            case REWARD:
                return uri + token + "/reinforce/";

        }
        return "";

    }



}
