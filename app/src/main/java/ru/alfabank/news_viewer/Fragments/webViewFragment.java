package ru.alfabank.news_viewer.Fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.alfabank.news_viewer.DataBase.DbHelper;
import ru.alfabank.news_viewer.DataBase.DbTable;
import ru.alfabank.news_viewer.DataEvent.SitePageEvent;
import ru.alfabank.news_viewer.R;
import ru.alfabank.news_viewer.RssItem;
import ru.alfabank.news_viewer.Util.NetworkUtils;

public class webViewFragment extends Fragment {

    private View view;
    private WebView webView;
    private String pathFilename;
    private RssItem item;
    private DbHelper dbHelper;

    public webViewFragment() {
    }

    private void setParametrs(RssItem item) {
        this.item = item;
    }

    public static webViewFragment getInstance(RssItem item) {
        webViewFragment fragment = new webViewFragment();
        fragment.setParametrs(item);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_web_view, container, false);
            FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
            webView = (WebView) view.findViewById(R.id.webView);
            Pattern p = Pattern.compile("/\\d+\\.");
            Matcher m = p.matcher(item.getLink());
            String name = m.find() ? m.group() : "/"+item.getTitle()+".";
            pathFilename = getActivity().getFilesDir().getPath() + name + "mht";
            webView.setWebViewClient(new WebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);
            if (NetworkUtils.isConnectingToInternet(getActivity())) {
                webView.loadUrl(item.getLink());
            } else {
                webView.loadUrl("file://" + pathFilename);
            }
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveView();
                }
            });
        } else {
            ViewGroup parent = (ViewGroup) view.getParent();
            parent.removeView(view);
        }

        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SitePageEvent sitePageEvent) {
        webView.loadData(sitePageEvent.getHtml(), "text/html", "utf-8");
    }

    private void saveView() {
        dbHelper = new DbHelper(getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(DbTable.TABLE_ITEMS_NAME, null, null, null, null, null, null);
        ContentValues cv = new ContentValues();
        if (cursor.moveToFirst()) {
            int titleColIndex = cursor.getColumnIndex(DbTable.COLUMN_ITEM_TITLE);
            boolean noItem = true;
            do {
                String title = cursor.getString(titleColIndex);
                if (title.equals(item.getTitle())){
                    noItem = false;
                    continue;
                }
            } while (cursor.moveToNext());
            if(noItem){
                cv.put(DbTable.COLUMN_ITEM_TITLE, item.getTitle());
                cv.put(DbTable.COLUMN_ITEM_LINK, item.getLink());
                db.insert(DbTable.TABLE_ITEMS_NAME, null, cv);
                webView.saveWebArchive(pathFilename);
            }
        }else {
            cv.put(DbTable.COLUMN_ITEM_TITLE, item.getTitle());
            cv.put(DbTable.COLUMN_ITEM_LINK, item.getLink());
            db.insert(DbTable.TABLE_ITEMS_NAME, null, cv);
            webView.saveWebArchive(pathFilename);
        }
        dbHelper.close();
    }
}
