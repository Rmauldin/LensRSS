package com.example.ryan.newsviewer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class RssDrawerListAdapter
        extends RecyclerView.Adapter<RssDrawerListAdapter.DrawerItemHolder>{

    private List<RssFeedDrawerItem> rssFeeds;
    private List<RssFeedDrawerItem> shownRssFeeds;
    private boolean drawerChanged;
    private Context context;

    public class DrawerItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View rssFeedView;
        public DrawerItemHolder(View v){
            super(v);
            v.setOnClickListener(this);
            rssFeedView = v;
        }

        @Override
        public void onClick(View view) {

        }
    }

    public RssDrawerListAdapter(List<RssFeedDrawerItem> rssFeeds, Context context){
        this.rssFeeds = rssFeeds;
        this.context = context;
        drawerChanged = false;
        shownRssFeeds = new ArrayList<>();
        for(RssFeedDrawerItem feed : rssFeeds){
            if(feed.isAdded()){
                shownRssFeeds.add(feed);
            }
        }
    }

    @NonNull
    @Override
    public DrawerItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        if(viewType == R.layout.drawer_item) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.drawer_item, parent, false);
        }else{
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.end_of_drawer_item, parent, false);
        }
        return new DrawerItemHolder(v);
    }

    private boolean toggleItem(RssFeedDrawerItem item, DrawerItemHolder holder){
        if (item.isAdded()) {
            holder.rssFeedView.findViewById(R.id.drawer_add_icon).setBackgroundResource(R.drawable.baseline_add_24);
            item.setAdded(false);
            shownRssFeeds.remove(item);
        } else {
            holder.rssFeedView.findViewById(R.id.drawer_add_icon).setBackgroundResource(R.drawable.baseline_check_24);
            item.setAdded(true);
            shownRssFeeds.add(item);
        }
        return item.isAdded();
    }

    @Override
    public void onBindViewHolder(@NonNull final DrawerItemHolder holder, final int position) {
        if (position == rssFeeds.size()) {
            ((TextView)holder.rssFeedView.findViewById(R.id.rssFeed_item_title)).setText(R.string.add_custom_feed);
            holder.rssFeedView.findViewById(R.id.rssFeed_add_icon).setBackgroundResource(R.drawable.baseline_playlist_add_24);
            holder.rssFeedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlterDrawerListDialog alterDrawer = new AlterDrawerListDialog(context, rssFeeds, shownRssFeeds);
                    alterDrawer.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            notifyDataSetChanged();
                        }
                    });
                    alterDrawer.show();
                }
            });
        }else{
            final RssFeedDrawerItem item = rssFeeds.get(position);
            ((TextView) holder.rssFeedView.findViewById(R.id.drawer_item_title)).setText(item.title);
            if (item.isAdded()) {
                holder.rssFeedView.findViewById(R.id.drawer_add_icon).setBackgroundResource(R.drawable.baseline_check_24);
            } else {
                holder.rssFeedView.findViewById(R.id.drawer_add_icon).setBackgroundResource(R.drawable.baseline_add_24);
            }

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setCancelable(true);
                    alertDialogBuilder.setPositiveButton(R.string.edit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            AlterDrawerListDialog alterDrawer = new AlterDrawerListDialog(context, rssFeeds, rssFeeds.indexOf(item));
                            alterDrawer.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    notifyDataSetChanged();
                                }
                            });
                            alterDrawer.show();
                        }
                    });

                    alertDialogBuilder.setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            AlertDialog.Builder deleteConfirmation = new AlertDialog.Builder(context);
                            deleteConfirmation.setTitle("Are you sure?");
                            deleteConfirmation.setMessage("Delete " + item.title + "?");
                            deleteConfirmation.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if(item.isAdded()) shownRssFeeds.remove(item);
                                    rssFeeds.remove(item);
                                    notifyDataSetChanged();
                                }
                            });

                            deleteConfirmation.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });

                            deleteConfirmation.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            deleteConfirmation.show();
                        }
                    });

                    alertDialogBuilder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    alertDialogBuilder.setTitle(R.string.edit_drawer_item_title);
                    alertDialogBuilder.show();
                    //AlertDialog alertDialog = alertDialogBuilder.create();
                    //alertDialog.show();
                    return false;
                }

            });

            holder.itemView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    drawerChanged = true;
                    toggleItem(item, holder);
                    notifyDataSetChanged();
                }
            });

            holder.rssFeedView.findViewById(R.id.drawer_add_icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawerChanged = true;
                    toggleItem(item, holder);
                    notifyDataSetChanged();
                }
            });
        }
    }
    @Override
    public int getItemCount() {
        return rssFeeds.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == getItemCount() - 1 ? R.layout.end_of_drawer_item : R.layout.drawer_item;
    }

    public List<RssFeedDrawerItem> getRssFeeds(){
        return rssFeeds;
    }

    public List<RssFeedDrawerItem> getShownRssFeeds(){
        return shownRssFeeds;
    }

    public boolean drawerChanged(){
        return drawerChanged;
    }
}
