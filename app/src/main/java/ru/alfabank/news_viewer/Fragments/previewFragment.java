package ru.alfabank.news_viewer.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import ru.alfabank.news_viewer.DataEvent.GetRssEvent;
import ru.alfabank.news_viewer.DataEvent.SetWebViewFragmentEvent;
import ru.alfabank.news_viewer.MainActivity;
import ru.alfabank.news_viewer.R;

public class previewFragment extends Fragment {

    private View view;
    private SharedPreferences sPref;

    public previewFragment() {
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SetWebViewFragmentEvent event) {
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
        view = inflater.inflate(R.layout.fragment_preview, container, false);
        Button button = (Button) view.findViewById(R.id.button);
        TextView textView = (TextView) view.findViewById(R.id.prevText);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(getString(R.string.previewText) ,Html.FROM_HTML_MODE_LEGACY));
        } else {
            textView.setText(Html.fromHtml(getString(R.string.previewText)));
        }
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sPref = getActivity().getPreferences(MainActivity.MODE_PRIVATE);
                        SharedPreferences.Editor ed = sPref.edit();
                        ed.putString(MainActivity.FIRST_LOAD, "false");
                        ed.commit();
                        EventBus.getDefault().postSticky(new GetRssEvent("getData"));
                    }
                });
        return view;
    }

    public void onClickS() {

    }
}
