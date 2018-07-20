package com.example.ryan.newsviewer;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.net.URI;
import java.net.URISyntaxException;

public class RssFeedDrawerItem {
    public String title;
    public URI link;
    public String URLString;
    public boolean added;

    RssFeedDrawerItem(String title, String link, boolean added) throws URISyntaxException {
        this.title = title;
        this.URLString = link;
        this.link = getLink(link);
        this.added = added;
    }

    private URI getLink(String link) throws URISyntaxException {
        URI result;
        if( !link.startsWith("http://") && !link.startsWith("https://")){
            link = "https://" + link;
        }

        try{
            result = new URI(link);
        }catch (URISyntaxException e) {
            Log.d("RssFeedDrawerItem", "Invalid RSS feed link");
            throw e;
        }
        return result;
    }

    public boolean isAdded(){
        return added;
    }

    public void setAdded(boolean added){
        this.added = added;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setUrl(String url) throws URISyntaxException {
        link = getLink(url);
        this.URLString = link.toString();
    }
}
