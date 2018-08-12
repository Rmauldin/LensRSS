package com.example.ryan.newsviewer;


import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import me.angrybyte.goose.network.GooseDownloader;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FetchArticle {
    //final String SERVER_URL = "http://newsviewer-env.yhrfxqcrf4.us-west-1.elasticbeanstalk.com/request-url-info";
    String server_url;
    RssFeedModel item;

    public FetchArticle(RssFeedModel item, Context context){
        server_url = context.getResources().getString(R.string.server_url);
        this.item = item;
    }

    private void processJson(String responseData){
        try{
            JSONObject reader = new JSONObject(responseData);
            item.setContent(reader.getString("text"));
            item.setPolarity(reader.getDouble("polarity"));
            item.setSubjectivity(reader.getDouble("subjectivity"));
            item.setLeaning(reader.getString("leaning"));
            String imageUrl = reader.getString("image");
            if(!imageUrl.equalsIgnoreCase("")) {
                item.setImage(GooseDownloader.getPhoto(imageUrl, true));
            }
            String author = reader.getJSONArray("authors").getString(0);
            item.setAuthor(author);
        }catch(JSONException e){
            Log.d("processJson", e.getLocalizedMessage());
        }
        item.setLoaded("true");
    }

    public boolean execute(){
        RequestBody formBody = new FormBody.Builder().add("url", item.getLink()).build();
        Request request = new Request.Builder()
                .url(server_url)
                .post(formBody)
                .build();
        Log.d("FetchArticleTask", "request is: " + request.toString());
        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if(response.code() == 200){
                String responseData = response.body().string();
                processJson(responseData);
                response.close();
            }else{
                Log.d("FetchArticleTask", "Server issue");
                response.close();
                return false;
                //Server problem
            }
        }catch(IOException e){
            Log.d("FetchArticle", e.getLocalizedMessage());
            if(response != null) response.close();
            return false;
        }
        return true;
    }

    public RssFeedModel getNewItem(){
        return this.item;
    }

}
