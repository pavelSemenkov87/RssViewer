package ru.alfabank.news_viewer;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import ru.alfabank.news_viewer.DataBase.DbHelper;
import ru.alfabank.news_viewer.DataBase.DbTable;
import ru.alfabank.news_viewer.DataEvent.RssDataEvent;
import ru.alfabank.news_viewer.DataEvent.SetWebViewFragmentEvent;
import ru.alfabank.news_viewer.Fragments.RssFragment;
import ru.alfabank.news_viewer.Fragments.developerFragment;
import ru.alfabank.news_viewer.Fragments.previewFragment;
import ru.alfabank.news_viewer.Fragments.webViewFragment;
import ru.alfabank.news_viewer.Util.NetworkUtils;

public class MainActivity extends AppCompatActivity {

    private FragmentTransaction fTrans;
    private DbHelper dbHelper;
    private boolean replace = true, add = false;
    private SharedPreferences sPref;
    public static final int MODE_PRIVATE = 0x0000;
    public static final String FIRST_LOAD = "first";
    private Toolbar toolbar;
    private List<RssItem> items;
    private int countBack = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RssDataEvent rssDataEvent = EventBus.getDefault().getStickyEvent(RssDataEvent.class);
        sPref = getPreferences(MODE_PRIVATE);
        if (Boolean.valueOf(sPref.getString(FIRST_LOAD, "true"))) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.fragment_frame, new previewFragment());
            transaction.commit();
            initToolbar(getString(R.string.title_preview));
        } else if (rssDataEvent != null && NetworkUtils.isConnectingToInternet(getApplicationContext())) {
            items = (List<RssItem>) rssDataEvent.getRssItems();
            initToolbar(getString(R.string.title_rss));
        } else {
            setFragment(getDbRss(), add);
            initToolbar(getString(R.string.title_liked));
        }
        startService(new Intent(this, RssService.class)
        );
    }

    private void initToolbar(String title) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
    }

    private List<RssItem> getDbRss() {
        List<RssItem> items = null;
        dbHelper = new DbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(DbTable.TABLE_ITEMS_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int titleColIndex = cursor.getColumnIndex(DbTable.COLUMN_ITEM_TITLE);
            int linkColIndex = cursor.getColumnIndex(DbTable.COLUMN_ITEM_LINK);
            items = new ArrayList<RssItem>();
            do {
                String title = cursor.getString(titleColIndex);
                String link = cursor.getString(linkColIndex);
                RssItem item = new RssItem(title, link);
                items.add(item);
            } while (cursor.moveToNext());
        }
        return items;
    }

    private void setFragment(List<RssItem> items, boolean replace) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (NetworkUtils.isConnectingToInternet(getApplicationContext())) {
            if (replace) {
                transaction.replace(R.id.fragment_frame, RssFragment.getInstance(items));
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                getSupportActionBar().setTitle(getString(R.string.title_rss));
            } else {
                transaction.add(R.id.fragment_frame, RssFragment.getInstance(items));
            }
        } else {
            Toast toast2 = Toast.makeText(getApplicationContext(), getString(R.string.action_no_internet), Toast.LENGTH_LONG);
            toast2.setGravity(Gravity.TOP, 0, 150);
            toast2.show();
            setFragment(getDbRss(), add);
            getSupportActionBar().setTitle(getString(R.string.title_liked));
        }
        transaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SetWebViewFragmentEvent event) {
        fTrans = getSupportFragmentManager().beginTransaction();
        fTrans.replace(R.id.fragment_frame, webViewFragment.getInstance(event.getItem()));
        fTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fTrans.addToBackStack(null);
        fTrans.commit();
        getSupportActionBar().setTitle(getString(R.string.title_webview));
        countBack = 0;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RssDataEvent event) {
        items =  event.getRssItems();
        if (!Boolean.valueOf(sPref.getString(FIRST_LOAD, "true")))
            setFragment(event.getRssItems(), replace);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.getItem(1).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.devInfo:
                fTrans = getSupportFragmentManager().beginTransaction();
                fTrans.replace(R.id.fragment_frame, new developerFragment());
                fTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                fTrans.commit();
                getSupportActionBar().setTitle(getString(R.string.title_develop));
                countBack = 0;
                break;
            case R.id.Liked:
                setFragment(getDbRss(), replace);
                getSupportActionBar().setTitle(getString(R.string.title_liked));
                countBack = 0;
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        if (countBack<1){
            setFragment(items, replace);
            countBack++;
        }else openQuitDialog();
    }
    private void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(this);
        quitDialog.setTitle("Выход: Вы уверены?");

        quitDialog.setPositiveButton("Таки да!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                finish();
            }
        });

        quitDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });

        quitDialog.show();
    }
}
