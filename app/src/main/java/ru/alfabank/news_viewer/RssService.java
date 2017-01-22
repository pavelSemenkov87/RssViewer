package ru.alfabank.news_viewer;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import ru.alfabank.news_viewer.DataEvent.GetRssEvent;
import ru.alfabank.news_viewer.DataEvent.RssDataEvent;
import ru.alfabank.news_viewer.Util.NetworkUtils;
import ru.alfabank.news_viewer.Util.PcWorldRssParser;

public class RssService extends Service {

    public static final String TAG = "RssApp";
    private static final String RSS_LINK = "http://alfabank.ru/_/rss/_rss.html";
    public static final int RSS = 1, APPDATE = 2, MINUTA = 60000, DEFAULT_NOTIFICATION_ID = 101;
    LooperThread looperThread;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            Message message = looperThread.mHandler.obtainMessage(APPDATE);
            looperThread.mHandler.sendMessage(message);
            timerHandler.postDelayed(this, MINUTA*5);
        }
    };

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "RssService onCreate");
        EventBus.getDefault().register(this);
        looperThread = new LooperThread();
        looperThread.start();
        timerHandler.postDelayed(timerRunnable, 0);
        strtForegraundService();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "RssService onDestroy");
        EventBus.getDefault().unregister(this);
        looperThread.mHandler.getLooper().quit();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    class LooperThread extends Thread {

        public Handler mHandler;
        @Override
        public void run() {
            Looper.prepare();
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    int codeData = msg.what;
                    if (codeData == RSS) {
                        if(NetworkUtils.isConnectingToInternet(getApplicationContext())){
                            EventBus.getDefault().postSticky(new RssDataEvent(getItem()));
                        }
                    } else if (codeData == APPDATE){
                        if(NetworkUtils.isConnectingToInternet(getApplicationContext())){
                            EventBus.getDefault().postSticky(new RssDataEvent(getItem()));
                        }
                    }
                }
            };
            Object getRssEvent = EventBus.getDefault().getStickyEvent(GetRssEvent.class);
            if(getRssEvent != null){
                Message message = mHandler.obtainMessage(RSS);
                mHandler.sendMessage(message);
            }
            Looper.loop();
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GetRssEvent getRssEvent) {
        Message message = looperThread.mHandler.obtainMessage(RSS);
        looperThread.mHandler.sendMessage(message);
    }
    private List<RssItem> getItem(){
        List<RssItem> rssItems = null;
        try {
            PcWorldRssParser parser = new PcWorldRssParser();
            rssItems = parser.parse(getInputStream(RSS_LINK));
        } catch (XmlPullParserException | IOException e) {
            Log.w(e.getMessage(), e);
        }
        return rssItems;
    }
    public InputStream getInputStream(String link) {
        try {
            URL url = new URL(link);
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            Log.w(TAG, "Exception while retrieving the input stream", e);
            return null;
        }
    }

    private void strtForegraundService(){
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Новости Альфа-Банка")
                .setTicker("Новости Альфа-Банка")
                .setContentText("Новости Альфа-Банка")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(
                        Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setOngoing(true).build();

        startForeground(DEFAULT_NOTIFICATION_ID,
                notification);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
