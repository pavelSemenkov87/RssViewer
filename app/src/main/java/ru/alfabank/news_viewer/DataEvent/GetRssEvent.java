package ru.alfabank.news_viewer.DataEvent;

/**
 * Created by pavel on 15.01.2017.
 */

public class GetRssEvent {
    public final String message;

    public GetRssEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
