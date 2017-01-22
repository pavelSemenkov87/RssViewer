package ru.alfabank.news_viewer.DataEvent;

/**
 * Created by pavel on 15.01.2017.
 */

public class GetSiteEvent {
    public final String message;

    public GetSiteEvent(String message) {
        this.message = message;
    }

    public String getUrl() {
        return message;
    }
}
