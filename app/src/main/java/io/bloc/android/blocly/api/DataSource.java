package io.bloc.android.blocly.api;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.BuildConfig;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;
import io.bloc.android.blocly.api.model.database.DatabaseOpenHelper;
import io.bloc.android.blocly.api.model.database.table.RssFeedTable;
import io.bloc.android.blocly.api.model.database.table.RssItemTable;
import io.bloc.android.blocly.api.network.GetFeedsNetworkRequest;

/**
 * Created by yankomizorov on 3/20/16.
 */
public class DataSource {

    private DatabaseOpenHelper databaseOpenHelper;
    private RssFeedTable rssFeedTable;
    private RssItemTable rssItemTable;
    private List<RssFeed> feeds;
    private List<RssItem> items;

    public DataSource(){
        rssFeedTable = new RssFeedTable();
        rssItemTable = new RssItemTable();
        databaseOpenHelper = new DatabaseOpenHelper(
                BloclyApplication.getSharedInstance(),
                rssFeedTable,
                rssItemTable);
        feeds = new ArrayList<RssFeed>();
        items = new ArrayList<RssItem>();
        createFakeData();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(BuildConfig.DEBUG && true){
                    BloclyApplication.getSharedInstance().deleteDatabase("blocly_db");
                }
                SQLiteDatabase writableDatabase = databaseOpenHelper.getWritableDatabase();
                List<GetFeedsNetworkRequest.FeedResponse> feedList = new GetFeedsNetworkRequest(
                        "http://feeds.feedburner.com/androidcentral?format=xml").
                        performRequest();
                saveDataToDatabase(feedList, writableDatabase);
            }
        }).start();
    }

    public List<RssFeed> getFeeds() {
        return feeds;
    }

    public List<RssItem> getItems() {
        return items;
    }

    void createFakeData() {
        feeds.add(new RssFeed("My Favorite Feed",
                "This feed is just incredible, I can't even begin to tell you…",
                "http://favoritefeed.net", "http://feeds.feedburner.com/favorite_feed?format=xml"));
        for (int i = 0; i < 10; i++) {
            items.add(new RssItem(String.valueOf(i),
                    BloclyApplication.getSharedInstance().getString(R.string.placeholder_headline) + " " + i,
                    BloclyApplication.getSharedInstance().getString(R.string.placeholder_content),
                    "http://favoritefeed.net?story_id=an-incredible-news-story",
                    "http://g-ecx.images-amazon.com/images/G/01/img15/pet-products/small-tiles/23695_pets_vertical_store_dogs_small_tile_8._CB312176604_.jpg",
                    0, System.currentTimeMillis(), false, false));
        }
    }

    private void saveDataToDatabase(List<GetFeedsNetworkRequest.FeedResponse> feedList, SQLiteDatabase db){
        for(GetFeedsNetworkRequest.FeedResponse feed : feedList){

            ContentValues dbFeedValues = new ContentValues();
            dbFeedValues.put(RssFeedTable.COLUMN_LINK, feed.channelURL);
            dbFeedValues.put(RssFeedTable.COLUMN_TITLE, feed.channelTitle);
            dbFeedValues.put(RssFeedTable.COLUMN_DESCRIPTION, feed.channelDescription);
            dbFeedValues.put(RssFeedTable.COLUMN_FEED_URL, feed.channelFeedUrl);
            long feedPrimaryKey = db.insert(rssFeedTable.getName(), null, dbFeedValues);

            for(GetFeedsNetworkRequest.ItemResponse item : feed.channelItems){
                ContentValues dbItemValues = new ContentValues();
                dbItemValues.put(RssItemTable.COLUMN_LINK, item.itemURL);
                dbItemValues.put(RssItemTable.COLUMN_TITLE, item.itemTitle);
                dbItemValues.put(RssItemTable.COLUMN_DESCRIPTION, item.itemDescription);
                dbItemValues.put(RssItemTable.COLUMN_GUID, item.itemGUID);
                dbItemValues.put(RssItemTable.COLUMN_PUB_DATE, item.itemPubDate);
                dbItemValues.put(RssItemTable.COLUMN_ENCLOSURE, item.itemEnclosureURL);
                dbItemValues.put(RssItemTable.COLUMN_MIME_TYPE, item.itemEnclosureMIMEType);
                dbItemValues.put(RssItemTable.COLUMN_RSS_FEED, feedPrimaryKey);
                dbItemValues.put(RssItemTable.COLUMN_FAVORITE, 0);
                dbItemValues.put(RssItemTable.COLUMN_ARCHIVED, 0);
                db.insert(rssItemTable.getName(), null, dbItemValues);
            }
        }
        // check data
        Cursor c = db.rawQuery("select * from rss_feeds", null);
        while(c.moveToNext())
        {
            String title = c.getString(c.getColumnIndex(RssFeedTable.COLUMN_TITLE));
            Log.v("bloclydb", "rss_feeds title: " + title);
        }
        c = db.rawQuery("select * from rss_items", null);
        while(c.moveToNext()){
            String title = c.getString(c.getColumnIndex(RssItemTable.COLUMN_TITLE));
            Log.v("bloclydb", "rss_items title: " + title);
        }
    }

}
