package ru.alfabank.news_viewer.DataEvent;

import ru.alfabank.news_viewer.RssItem;

public class SetWebViewFragmentEvent {
    private RssItem item;

    public SetWebViewFragmentEvent(RssItem item) {
        this.item = item;
    }

    public RssItem getItem() {
        return item;
    }
}
