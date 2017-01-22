package ru.alfabank.news_viewer.DataEvent;

/**
 * Created by pavel on 15.01.2017.
 */

public class SitePageEvent {
    String html;
    public SitePageEvent(String html){
        this.html = html;
    }
    public String getHtml(){
        return html;
    }
}
