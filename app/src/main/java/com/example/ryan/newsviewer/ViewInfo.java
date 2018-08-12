package com.example.ryan.newsviewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Locale;

import me.angrybyte.goose.Article;
import me.angrybyte.goose.Configuration;
import me.angrybyte.goose.ContentExtractor;
import me.angrybyte.goose.network.GooseDownloader;

public class ViewInfo extends AppCompatActivity{
    RssFeedModel item;
    int index;
    Bitmap bitmap;
    SwipeRefreshLayout mSwipeLayout;
    TextView articleTitle, articleAuthor, articleDate, articleContent, articlePolarity, articleSubjectivity, articleLeaning;
    ImageView articleImage;
    Toolbar toolbar;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_info);
        item = getIntent().getParcelableExtra("item");

        //bitmap = getIntent().getParcelableExtra("image");
        boolean hasImage = getIntent().getBooleanExtra("hasImage", false);
        if(hasImage){
            try {
                bitmap = BitmapFactory.decodeStream(this.openFileInput("passImage"));
            }catch(FileNotFoundException e){
                e.printStackTrace();
                bitmap = null;
            }
        }else {
            bitmap = null;
        }
        index = getIntent().getIntExtra("place", 0);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        toolbar = findViewById(R.id.view_tool_bar);
        ((TextView)toolbar.findViewById(R.id.toolbar_title)).setText(item.getDomain());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mSwipeLayout = findViewById(R.id.article_swipe_layout);
        mSwipeLayout.setEnabled(false);
        articleTitle = findViewById(R.id.article_title);
        articleAuthor = findViewById(R.id.article_author);
        articleDate = findViewById(R.id.article_date);
        articleContent = findViewById(R.id.article_content);
        articleImage = findViewById(R.id.article_image);
        articlePolarity = findViewById(R.id.article_polarity);
        articleSubjectivity = findViewById(R.id.article_subjectivity);
        articleLeaning = findViewById(R.id.article_leaning);
        FetchArticleTask mainTask = new FetchArticleTask();

        if(item.getLoaded().equalsIgnoreCase("false")) {
            mainTask.execute((Void) null);
        }else{
            item.setImage(bitmap);
            displayInfo();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void displayInfo() {
        articleTitle.setText(item.getTitle());
        if(item.getAuthor() != null) {
            articleAuthor.setText(item.getAuthor());
        }else{
            articleAuthor.setText("");
        }
        articleDate.setText(item.getDisplayDate());
        if(item.getImage() != null) {
            articleImage.setImageBitmap(item.getImage());
        }
        articleContent.setText(item.getContent());
        findViewById(R.id.article_info).setVisibility(View.VISIBLE);
        if(item.getLeaning() != null) {
            String polarityText = "Positivity: " + String.format(Locale.US, "%.02f%%", item.getPolarity() * 100);
            String subjectivityText = "Subjectivity: " + String.format(Locale.US, "%.02f%%", item.getSubjectivity() * 100);
            articlePolarity.setText(polarityText);
            articleSubjectivity.setText(subjectivityText);

            String leaningText = "Best Guess for Political Bias: " + item.getLeaning();
            articleLeaning.setText(leaningText);
        }else{
            articlePolarity.setVisibility(View.GONE);
            articleSubjectivity.setVisibility(View.GONE);
            articleLeaning.setText(R.string.analyze_failure);
        }

        item.setLoaded("true");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("returnItem", item);
        Bitmap bitmap = item.getImage();
        if(bitmap != null) {
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                FileOutputStream fo = this.openFileOutput("passImage", Context.MODE_PRIVATE);
                fo.write(bytes.toByteArray());
                // remember close file output
                fo.close();
                intent.putExtra("hasImage", true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            intent.putExtra("hasImage", false);
        }

        intent.putExtra("returnPlace", index);
        setResult(RESULT_OK, intent);
    }

    private boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    private class FetchArticleTask extends AsyncTask<Void, Void, Boolean>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSwipeLayout.setRefreshing(true);
        }

        private boolean bypassServer(){
            Configuration config = new Configuration(getCacheDir().getAbsolutePath());
            ContentExtractor extractor = new ContentExtractor(config);
            if(item.getLink() == null) {
                item.setContent(getResources().getString(R.string.no_article_text));
                return false;
            }
            Article article = extractor.extractContent(item.getLink(), true);
            if(article == null){
                item.setContent(getResources().getString(R.string.no_article_text));
                return false;
            }
            String content = article.getCleanedArticleText();
            if(content == null || content.length() < 250) {
                content = getResources().getString(R.string.no_article_text);
                content = content + "\n\n" + article.getMetaDescription();
                articleContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri uri = Uri.parse(item.getLink());
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                });
                Log.d("ViewInfo", "Could not fetch content");
            }
            item.setContent(content);
            if(article.getTopImage() != null) {
                item.setImage(GooseDownloader.getPhoto(article.getTopImage().getImageSrc(), true));
            }else{
                Log.d("ViewInfo", "Could not extract photo");
            }
            Date showDate = article.getPublishDate();
            if(showDate != null){
                item.setDate(showDate);
            }
            extractor.releaseResources();
            return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Boolean doInBackground(Void... voids) {
            FetchArticle fetchArticle = new FetchArticle(item, getApplicationContext());
            if(fetchArticle.execute()){
                item = fetchArticle.getNewItem();
                return true;
            }else{
                return bypassServer();
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mSwipeLayout.setRefreshing(false);
            if(aBoolean){
                displayInfo();
                Log.d("ViewInfo", "FetchArticleTask Success");
            }else{
                Log.d("ViewInfo", "FetchArticleTask Error");
            }
        }
    }
    /*
    private class FetchArticleTask extends AsyncTask<Void, Void, Boolean>{

        Configuration config;
        ContentExtractor extractor;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            config = new Configuration(getCacheDir().getAbsolutePath());
            extractor = new ContentExtractor(config);
            mSwipeLayout.setRefreshing(true);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if(item.getLink() == null) {
                item.setContent(getResources().getString(R.string.no_article_text));
                return false;
            }
            Article article = extractor.extractContent(item.getLink(), true);
            if(article == null){
                item.setContent(getResources().getString(R.string.no_article_text));
                return false;
            }
            String content = article.getCleanedArticleText();
            if(content == null || content.length() < 250) {
                content = getResources().getString(R.string.no_article_text);
                content = content + "\n\n" + article.getMetaDescription();
                articleContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri uri = Uri.parse(item.getLink());
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                });
                Log.d("ViewInfo", "Could not fetch content");
            }
            item.setContent(content);
            if(article.getTopImage() != null) {
                item.setImage(GooseDownloader.getPhoto(article.getTopImage().getImageSrc(), true));
            }else{
                Log.d("ViewInfo", "Could not extract photo");
            }
            Date showDate = article.getPublishDate();
            if(showDate != null){
                item.setDate(showDate);
            }
            extractor.releaseResources();
            return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mSwipeLayout.setRefreshing(false);
            if(aBoolean){
                displayInfo();
                Log.d("ViewInfo", "FetchArticleTask Success");
            }else{
                Log.d("ViewInfo", "FetchArticleTask Error");
            }
        }
    }
    */

}
