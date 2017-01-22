package ru.alfabank.news_viewer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class BootServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentService = new Intent(context, RssService.class);
        context.startService(intentService);
    }
}