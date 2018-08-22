package com.example.ryan.newsviewer;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.RelativeDateTimeFormatter;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    protected static final int VIEW_INFO_REQUEST_CODE = 0;
    private static final String TAG = "MyActivity";
    private RecyclerView mRecyclerViewer;
    private SwipeRefreshLayout mSwipeLayout;
    private SwipeRefreshLayout mDrawerSwipeLayout;
    private List<RssFeedModel> mFeedModelList;
    private Sort sortMethod;
    private DrawerLayout mDrawerLayout;
    private ArrayList<RssFeedDrawerItem> rssFeeds;
    private ArrayList<RssFeedDrawerItem> shownRssFeeds;
    private ActionBarDrawerToggle mDrawerToggle;
    private Comparator<RssFeedDrawerItem> drawerComparator;
    FetchFeedTask mainFeed;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == VIEW_INFO_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                RssFeedModel returnItem = data.getParcelableExtra("returnItem");
                boolean hasImage = data.getBooleanExtra("hasImage", false);
                if(hasImage){
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(this.openFileInput("passImage"));
                        returnItem.setImage(bitmap);
                    }catch(FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
                int index = data.getIntExtra("returnPlace", 0 );
                mFeedModelList.set(index, new RssFeedModel(returnItem));
                mRecyclerViewer.getAdapter().notifyDataSetChanged();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadRssFeeds(){
        // get or create list of rss feeds
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());
        Gson gson = new Gson();

        /*
        // ----------------- debugging ----------------
        appSharedPrefs.edit().remove("FeedList").apply();
        // --------------------------------------------
        */

        drawerComparator = new Comparator<RssFeedDrawerItem>() {
            @Override
            public int compare(RssFeedDrawerItem t1, RssFeedDrawerItem t2) {
                if(t1.isAdded() && !t2.isAdded()) return -1;
                if(!t1.isAdded() && t2.isAdded()) return 1;
                String t1Org = t1.title.toLowerCase();
                String t2Org = t2.title.toLowerCase();
                if(t1Org.startsWith("the ")) t1Org = t1Org.substring(4);
                if(t2Org.startsWith("the ")) t2Org = t2Org.substring(4);
                return t1Org.compareTo(t2Org);
            }
        };

        String json = appSharedPrefs.getString("FeedList", null);
        shownRssFeeds = new ArrayList<RssFeedDrawerItem>();
        if(json != null){
            Type type = new TypeToken<List<RssFeedDrawerItem>>(){}.getType();
            rssFeeds = gson.fromJson(json, type);
            for (RssFeedDrawerItem feed : rssFeeds){
                if(feed.isAdded()) shownRssFeeds.add(feed);
            }
        }else{
            rssFeeds = new ArrayList<RssFeedDrawerItem>();
            try {
                rssFeeds.add(new RssFeedDrawerItem("The Guardian", "https://www.theguardian.com/us-news/rss", true));
                rssFeeds.add(new RssFeedDrawerItem("BBC World News", "http://feeds.bbci.co.uk/news/world/rss.xml", true));
                rssFeeds.add(new RssFeedDrawerItem("CNN Top Stories", "http://rss.cnn.com/rss/cnn_topstories.rss", true));
                rssFeeds.add(new RssFeedDrawerItem("ABC News: Top Stories", "https://abcnews.go.com/abcnews/topstories", true));
                rssFeeds.add(new RssFeedDrawerItem("CNBC: Top News", "https://www.cnbc.com/id/100003114/device/rss/rss.html", true));
                // https://www.cnbc.com/id/100003114/device/rss/rss.html
                rssFeeds.add(new RssFeedDrawerItem("Fox News: US", "http://feeds.foxnews.com/foxnews/national", true));
                rssFeeds.add(new RssFeedDrawerItem("Washington Post: Fact Checker",
                        "http://feeds.washingtonpost.com/rss/rss_fact-checker", true));
                rssFeeds.add(new RssFeedDrawerItem("Reuter's: US News", "http://feeds.reuters.com/Reuters/domesticNews", true));
                rssFeeds.add(new RssFeedDrawerItem("NY Times: US", "http://rss.nytimes.com/services/xml/rss/nyt/US.xml", true));
                for(RssFeedDrawerItem feed : rssFeeds){
                    if(feed.isAdded()) shownRssFeeds.add(feed);
                }
            }catch (URISyntaxException e) {
                Log.d("MainActivity", "Cannot add invalid RSS feed");
            }
            //rssFeeds.sort(drawerComparator);
            Collections.sort(rssFeeds, drawerComparator);
            saveRssFeeds();
        }
    }

    private void saveRssFeeds(){
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());
        Gson gson = new Gson();

        String json = gson.toJson(rssFeeds);
        SharedPreferences.Editor editor = appSharedPrefs.edit();
        editor.putString("FeedList", json);
        editor.apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateRssFeeds(List<RssFeedDrawerItem> newRssFeeds){
        rssFeeds = new ArrayList<>(newRssFeeds);
        shownRssFeeds = new ArrayList<>();
        for(RssFeedDrawerItem feed : rssFeeds){
            if(feed.isAdded()) {
                shownRssFeeds.add(feed);
            }
        }

        rssFeeds.sort(drawerComparator);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateRssFeeds(List<RssFeedDrawerItem> newRssFeeds, List<RssFeedDrawerItem> newShownRssFeeds){
        rssFeeds = new ArrayList<>(newRssFeeds);
        shownRssFeeds = new ArrayList<>(newShownRssFeeds);

        //rssFeeds.sort(drawerComparator);
        Collections.sort(rssFeeds, drawerComparator);
        // TODO outsource saveRssFeeds to when the app stops
        saveRssFeeds();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initializeContent() {
        mainFeed = new FetchFeedTask();
        mRecyclerViewer = findViewById(R.id.recyclerView);
        mRecyclerViewer.setLayoutManager(new LinearLayoutManager(this));

        sortMethod = Sort.RECENT_DATE;
        mSwipeLayout = findViewById(R.id.swipeRefreshLayout);

        mainFeed.execute((Void) null);

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mainFeed.cancel(true);
                mainFeed = new FetchFeedTask();
                mainFeed.execute((Void) null);
            }
        });
    }

    private void setupActionBar(){
        Toolbar toolbar = findViewById(R.id.tool_bar);
        toolbar.setNavigationIcon(R.drawable.baseline_menu_24);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setupTabSort(){
        TabLayout mSortTabs = findViewById(R.id.sort_tabs);
        mSortTabs.addTab(mSortTabs.newTab().setText("Recent"));
        mSortTabs.addTab(mSortTabs.newTab().setText("Alphabetical"));
        mSortTabs.addTab(mSortTabs.newTab().setText("Shuffle"));
        mSortTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d("MainActivity", "Selected " + tab.getText());
                if(tab.getText().equals("Recent")){
                    sortMethod = Sort.RECENT_DATE;
                }else if(tab.getText().equals("Alphabetical")){
                    sortMethod = Sort.ORG_ALPHABETICAL;
                }else if(tab.getText().equals("Shuffle")){
                    sortMethod = Sort.SHUFFLE;
                }
                mainFeed.cancel(true);
                mainFeed = new FetchFeedTask();
                mainFeed.execute((Void) null);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void initializeDrawer(){
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        RecyclerView mDrawerRecyclerViewer = findViewById(R.id.list_of_feeds);
        mDrawerRecyclerViewer.setLayoutManager(new LinearLayoutManager(this));
        final RssDrawerListAdapter drawerAdapter = new RssDrawerListAdapter(rssFeeds, this);
        mDrawerRecyclerViewer.setAdapter(drawerAdapter);
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if(drawerAdapter.drawerChanged()){
                    updateRssFeeds(drawerAdapter.getRssFeeds(), drawerAdapter.getShownRssFeeds());
                    //drawerAdapter.getRssFeeds().sort(drawerComparator);
                    Collections.sort(drawerAdapter.getRssFeeds(), drawerComparator);
                    drawerAdapter.notifyDataSetChanged();
                    mainFeed.cancel(true);
                    mainFeed = new FetchFeedTask();
                    mainFeed.execute((Void) null);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActionBar();
        loadRssFeeds();
        initializeDrawer();
        initializeContent();
        setupTabSort();
    }

    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            mSwipeLayout.setRefreshing(true);
            //urlLink = "http://feeds.bbci.co.uk/news/world/rss.xml";
            //urlLink = "https://www.theguardian.com/us-news/rss";
            mFeedModelList = new ArrayList<RssFeedModel>();
            //((TextView)findViewById(R.id.list_end)).setText("No Parseable RSS Feeds.");
            findViewById(R.id.list_end).setVisibility(View.GONE);
        }

        @TargetApi(Build.VERSION_CODES.O)
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean successful = false;
            //if(shownRssFeeds.isEmpty()) return true;
            for (RssFeedDrawerItem i : shownRssFeeds) {
                String urlLink = i.URLString;
                if (TextUtils.isEmpty(urlLink)) continue;
                try {
                    if (!(urlLink.startsWith("http://") || urlLink.startsWith("https://")))
                        urlLink = "http://" + urlLink;

                    URL url = new URL(urlLink);
                    InputStream inputStream = url.openConnection().getInputStream();
                    mFeedModelList.addAll(parseFeed(inputStream));
                    inputStream.close();
                    successful = true;
                } catch (IOException e) {
                    Log.e(TAG, "Error IOException");
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    Log.e(TAG, "Error XmlPullParserException");
                    e.printStackTrace();
                }
            }

            if(successful){
                sortList(mFeedModelList, sortMethod);
            }

            return successful;
        }

        @TargetApi(Build.VERSION_CODES.O)
        @RequiresApi(api = Build.VERSION_CODES.N)
        private void sortList(List<RssFeedModel> mFeedModelList, Sort method) {
            switch(method){
                case RECENT_DATE:
                    Collections.sort(mFeedModelList, new Comparator<RssFeedModel>() {
                        SimpleDateFormat dtf = new SimpleDateFormat("HH:mm:ss");
                        @Override
                        public int compare(RssFeedModel t1, RssFeedModel t2) {
                            if(t1.getDisplayDate().equalsIgnoreCase(t2.getDisplayDate())) {
                                //return dtf.parse(t1.getTime()) - dtf.parse(t2.getTime());
                                try {
                                    Date d1 = dtf.parse(t1.getTime());
                                    Date d2 = dtf.parse(t2.getTime());
                                    if(d1.equals(d2)) return 0;
                                    if(d1.after(d2)) return -1;
                                    return 1;
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    return t2.getDisplayDate().compareTo(t1.getDisplayDate());
                                }
                            }
                            return t2.getDisplayDate().compareTo(t1.getDisplayDate());
                        }
                    });
                    /*
                    mFeedModelList.sort(new Comparator<RssFeedModel>() {
                        @Override
                        public int compare(RssFeedModel t1, RssFeedModel t2) {
                            return t2.getDisplayDate().compareTo(t1.getDisplayDate());
                        }
                    });
                    */
                    break;
                case ORG_ALPHABETICAL:
                    Collections.sort(mFeedModelList, new Comparator<RssFeedModel>() {
                                @Override
                                public int compare(RssFeedModel t1, RssFeedModel t2) {
                                    String t1Org = t1.getDomain().toLowerCase();
                                    String t2Org = t2.getDomain().toLowerCase();
                                    if(t1Org.startsWith("the ")) t1Org = t1Org.substring(4);
                                    if(t2Org.startsWith("the ")) t2Org = t2Org.substring(4);
                                    return t1Org.compareTo(t2Org);
                                }
                            });
                    /*
                    mFeedModelList.sort(new Comparator<RssFeedModel>() {
                        @Override
                        public int compare(RssFeedModel t1, RssFeedModel t2) {
                            String t1Org = t1.getDomain().toLowerCase();
                            String t2Org = t2.getDomain().toLowerCase();
                            if(t1Org.startsWith("the ")) t1Org = t1Org.substring(4);
                            if(t2Org.startsWith("the ")) t2Org = t2Org.substring(4);
                            return t1Org.compareTo(t2Org);
                        }
                    });
                    */
                    break;
                case SHUFFLE:
                    Collections.sort(mFeedModelList, new Comparator<RssFeedModel>() {
                        @Override
                        public int compare(RssFeedModel t1, RssFeedModel t2) {
                            return t1.hashCode() - t2.hashCode();
                        }
                    });
                    /*
                    mFeedModelList.sort(new Comparator<RssFeedModel>() {
                        @Override
                        public int compare(RssFeedModel rssFeedModel, RssFeedModel t1) {
                            // comparator needs to have a guarantee of reproducibility
                            return ThreadLocalRandom.current().nextInt(-1, 1 + 1);
                        }
                    });
                    */
                    break;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mSwipeLayout.setRefreshing(false);
            RssFeedListAdapter adapter = new RssFeedListAdapter(mFeedModelList, MainActivity.this);
            mRecyclerViewer.setAdapter(adapter);
            if(!success){
                ((TextView)findViewById(R.id.list_end)).setText(R.string.no_feeds);
                findViewById(R.id.list_end).setVisibility(View.VISIBLE);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException, IOException{
        String title = null;
        String link = null;
        String details = null;
        String date = null;
        String img = null;
        boolean isItem = false;
        List<RssFeedModel> items = new ArrayList<>();
        try{
            XmlPullParser puller = Xml.newPullParser();
            puller.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            puller.setInput(inputStream, null);

            puller.nextTag();
            while(puller.next() != XmlPullParser.END_DOCUMENT){
                int eventType = puller.getEventType();
                String name = puller.getName();
                if(name == null) continue;

                if(eventType == XmlPullParser.END_TAG){
                    if(name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if(eventType == XmlPullParser.START_TAG){
                    if(name.equalsIgnoreCase("item")){
                        isItem = true;
                        title = null;
                        link = null;
                        details = null;
                        date = null;
                        img = null;
                        continue;
                    }
                }

                Log.d("newsviewer", "Parsing name ==> " + name);
                String result = "";
                if(puller.next() == XmlPullParser.TEXT){
                    result = puller.getText();
                    puller.nextTag();
                }
                //if(isItem) {
                    if (name.equalsIgnoreCase("title")) {
                        title = result;
                    } else if (name.equalsIgnoreCase("link")) {
                        link = result;
                    } else if (name.equalsIgnoreCase("description")) {
                        details = result;
                    } else if (name.equalsIgnoreCase("pubdate")) {
                        date = result;
                    } else if (name.equalsIgnoreCase("media:thumbnail")) {
                        img = result;
                    }
                //}
                if(title != null && link != null && details != null && date != null){
                    if(isItem){
                        RssFeedModel item = new RssFeedModel(title, link, details, date);
                        if(img != null) item.setThumbnail(img);
                        items.add(item);
                    }
                    title = null;
                    link = null;
                    details = null;
                    date = null;
                    img = null;
                    isItem = false;
                }

            }
            return items;
        }finally{
            inputStream.close();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.mRefresh:
                mainFeed.cancel(true);
                mainFeed = new FetchFeedTask();
                mainFeed.execute((Void) null);
                return true;
                //break;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        //return super.onOptionsItemSelected(item);
    }

}
