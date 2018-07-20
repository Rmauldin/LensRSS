package com.example.ryan.newsviewer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DialogTitle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.URISyntaxException;
import java.util.List;

class AlterDrawerListDialog extends Dialog{

    Context context;
    public Button confirm, cancel;
    public EditText editTitle, editUrl;
    private boolean addingItem;
    public TextView invalid_url;
    private List<RssFeedDrawerItem> rssFeeds;
    private List<RssFeedDrawerItem> shownRssFeeds;
    private RssFeedDrawerItem itemToAlter;

    public AlterDrawerListDialog(Context context, List<RssFeedDrawerItem> rssFeeds, int itemIndex) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.edit_drawer_item);
        addingItem = false;
        this.context = context;
        this.rssFeeds = rssFeeds;
        this.itemToAlter = rssFeeds.get(itemIndex);
        this.invalid_url = findViewById(R.id.invalid_url);
        this.editTitle = findViewById(R.id.title_input);
        this.editUrl = findViewById(R.id.url_input);
        this.confirm = findViewById(R.id.confirmation);
        this.cancel = findViewById(R.id.cancel);
        editTitle.setText(itemToAlter.title);
        editUrl.setText(itemToAlter.URLString);
    }

    public AlterDrawerListDialog(Context context, List<RssFeedDrawerItem> rssFeeds, List<RssFeedDrawerItem> shownRssFeeds){
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.edit_drawer_item);
        addingItem = true;
        this.context = context;
        this.rssFeeds = rssFeeds;
        this.shownRssFeeds = shownRssFeeds;
        this.invalid_url = findViewById(R.id.invalid_url);
        this.editTitle = findViewById(R.id.title_input);
        this.editUrl = findViewById(R.id.url_input);
        this.confirm = findViewById(R.id.confirmation);
        this.cancel = findViewById(R.id.cancel);
        editTitle.setHint(R.string.edit_title_hint);
        editUrl.setHint(R.string.edit_url_hint);
        ((DialogTitle)findViewById(R.id.dialog_title)).setText(R.string.add_custom_feed);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        confirm.setText(R.string.save);
        cancel.setText(R.string.cancel);
        editTitle.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if((keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (i == EditorInfo.IME_ACTION_DONE)){
                    editUrl.requestFocus();
                    return true;
                }
                return false;
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(addingItem){
                        itemToAlter = new RssFeedDrawerItem(editTitle.getText().toString(), editUrl.getText().toString(), true);
                        rssFeeds.add(itemToAlter);
                        shownRssFeeds.add(itemToAlter);
                        dismiss();
                    }else {
                        itemToAlter.setUrl(editUrl.getText().toString());
                        itemToAlter.setTitle(editTitle.getText().toString());
                        dismiss();
                    }
                }catch (URISyntaxException e){
                    invalid_url.setVisibility(View.VISIBLE);
                    Log.d("AlterDrawerList", "URISyntaxException");
                }
            }
        });

    }

    public List<RssFeedDrawerItem> getRssFeeds(){
        return rssFeeds;
    }


}
