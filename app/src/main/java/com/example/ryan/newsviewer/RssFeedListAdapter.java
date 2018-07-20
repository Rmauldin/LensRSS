package com.example.ryan.newsviewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class RssFeedListAdapter
        extends RecyclerView.Adapter<RssFeedListAdapter.FeedModelViewHolder> {

    private static final String TAG = "RssFeedListAdapter";
    private List<RssFeedModel> mRssFeedModels;
    private Context context;
    private int mExpandedPosition = -1;
    public class FeedModelViewHolder extends RecyclerView.ViewHolder {
        private View rssFeedView;

        public FeedModelViewHolder(View v) {
            super(v);
            rssFeedView = v;
        }
    }

    public RssFeedListAdapter(List<RssFeedModel> rssFeedModels, Context context) {
        this.mRssFeedModels = rssFeedModels;
        this.context = context;
    }

    @NonNull
    @Override
    public FeedModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View v;
        if(type == R.layout.item_rss_feed) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_rss_feed, parent, false);
        }else{
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.end_of_item_rss_feed, parent,false);
        }
        return new FeedModelViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final FeedModelViewHolder holder, final int position) {
        if (position == mRssFeedModels.size()) {
            if(mRssFeedModels.size() == 0)
                ((TextView)holder.rssFeedView.findViewById(R.id.end_of_rss_items)).setText(R.string.no_feeds);
            else
                ((TextView)holder.rssFeedView.findViewById(R.id.end_of_rss_items)).setText(R.string.end_of_feed);
        } else {
            final RssFeedModel rssFeedModel = mRssFeedModels.get(position);

            ((TextView) holder.rssFeedView.findViewById(R.id.titleText)).setText(rssFeedModel.getTitle());
            ((TextView) holder.rssFeedView.findViewById(R.id.sourceText)).setText(rssFeedModel.getDomain());
            ((TextView) holder.rssFeedView.findViewById(R.id.dateText)).setText(rssFeedModel.getDisplayDate());
            ((TextView) holder.rssFeedView.findViewById(R.id.descriptionText)).setText(rssFeedModel.getDetails());

            final boolean isExpanded = position == mExpandedPosition;
            holder.rssFeedView.findViewById(R.id.descriptionText).setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            holder.itemView.setActivated(isExpanded);
            holder.rssFeedView.findViewById(R.id.main_info).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View v) {
                    mExpandedPosition = isExpanded ? -1 : position;
                    TransitionManager.beginDelayedTransition((ViewGroup) holder.rssFeedView);
                    notifyDataSetChanged();
                }
            });

            holder.rssFeedView.findViewById(R.id.go_to_link).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri = Uri.parse(rssFeedModel.getLink());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    context.startActivity(intent);
                }
            });

            holder.rssFeedView.findViewById(R.id.go_to_info).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent viewInfoIntent = new Intent(context, ViewInfo.class);
                    viewInfoIntent.putExtra("item", rssFeedModel);
                    Bitmap bitmap = rssFeedModel.getImage();
                    if(bitmap != null) {
                        // has image
                        try {
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                            FileOutputStream fo = context.openFileOutput("passImage", Context.MODE_PRIVATE);
                            fo.write(bytes.toByteArray());
                            // remember close file output
                            fo.close();
                            viewInfoIntent.putExtra("hasImage", true);
                        } catch (Exception e) {
                            e.printStackTrace();
                            viewInfoIntent.putExtra("hasImage", false);
                        }
                    }else{
                        viewInfoIntent.putExtra("hasImage", false);
                    }

                    //viewInfoIntent.putExtra("image", bitmap);
                    viewInfoIntent.putExtra("place", mRssFeedModels.indexOf(rssFeedModel));
                    ((Activity)context).startActivityForResult(viewInfoIntent, MainActivity.VIEW_INFO_REQUEST_CODE);
                }
            });


        }
    }

    @Override
    public int getItemCount() {
        return mRssFeedModels.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == getItemCount() - 1 ? R.layout.end_of_item_rss_feed : R.layout.item_rss_feed;
    }
}
