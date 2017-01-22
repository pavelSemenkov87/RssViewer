package ru.alfabank.news_viewer.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;
import java.util.List;

import ru.alfabank.news_viewer.DataEvent.GetRssEvent;
import ru.alfabank.news_viewer.DataEvent.RssDataEvent;
import ru.alfabank.news_viewer.DataEvent.SetWebViewFragmentEvent;
import ru.alfabank.news_viewer.R;
import ru.alfabank.news_viewer.RssAdapter;
import ru.alfabank.news_viewer.RssItem;

public class RssFragment extends Fragment implements OnItemClickListener {

    private View view;
    private RssAdapter adapter;
    private ListView listView;
    private List<RssItem> items;
    private SwipeRefreshLayout swipeRefreshLayout;

    public RssFragment() {
    }
    private void setParametrs(List<RssItem> items) {
        this.items = items;
    }

    public static RssFragment getInstance(List<RssItem> items) {
        RssFragment fragment = new RssFragment();
        fragment.setParametrs(items);
        return fragment;
    }
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onStop();
        adapter=null;
        EventBus.getDefault().unregister(this);
    }
    @Override
    public void onResume() {
        super.onResume();
        if (adapter == null){
            adapter = new RssAdapter(getActivity(), items);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_rss, container, false);
        listView = (ListView) view.findViewById(R.id.listPreview);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                EventBus.getDefault().postSticky(new GetRssEvent("getData"));
            }
        });
        listView.setOnItemClickListener(this);
        if(items == null){
            items = new LinkedList<>();
            items.add(new RssItem("Список пуст", null));
        }
        adapter = new RssAdapter(getActivity(), items);
        listView.setAdapter(adapter);
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RssDataEvent event){
        List<RssItem> items = event.getRssItems();
        if (items != null) {
            RssAdapter adapter = new RssAdapter(getActivity(), items);
            listView.setAdapter(adapter);
        } else {
            Toast.makeText(getActivity(), "An error occurred while downloading the rss feed.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RssAdapter adapter = (RssAdapter) parent.getAdapter();
        RssItem item = (RssItem) adapter.getItem(position);
        if(item.getLink() != null){
            EventBus.getDefault().postSticky(new SetWebViewFragmentEvent(item));
        }
    }
}
