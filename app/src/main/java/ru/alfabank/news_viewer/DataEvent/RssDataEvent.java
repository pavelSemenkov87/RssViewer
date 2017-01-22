package ru.alfabank.news_viewer.DataEvent;

import java.util.List;

import ru.alfabank.news_viewer.RssItem;

/**
 * Created by pavel on 15.01.2017.
 */

public class RssDataEvent {
    private List<RssItem> rssItems;

    public RssDataEvent(List<RssItem> rssItems) {
        super();
        this.rssItems = rssItems;
    }
    public List<RssItem> getRssItems() {
        return rssItems;
    }
}
