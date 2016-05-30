package io.bloc.android.blocly.api;

import android.database.Cursor;
import android.os.Handler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.BuildConfig;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;
import io.bloc.android.blocly.api.model.database.DatabaseOpenHelper;
import io.bloc.android.blocly.api.model.database.table.RssFeedTable;
import io.bloc.android.blocly.api.model.database.table.RssItemTable;
import io.bloc.android.blocly.api.model.database.table.Table;
import io.bloc.android.blocly.api.network.GetFeedsNetworkRequest;
import io.bloc.android.blocly.api.network.NetworkRequest;

/**
 * Created by yankomizorov on 3/20/16.
 */
public class DataSource {

    public static interface Callback<Result>{
        public void onSuccess(Result result);
        public void onError(String errorMessage);
    }

    private DatabaseOpenHelper databaseOpenHelper;
    private RssFeedTable rssFeedTable;
    private RssItemTable rssItemTable;
    private ExecutorService executorService;

    public DataSource() {
        rssFeedTable = new RssFeedTable();
        rssItemTable = new RssItemTable();
        executorService = Executors.newSingleThreadExecutor();
        databaseOpenHelper = new DatabaseOpenHelper(
                BloclyApplication.getSharedInstance(),
                rssFeedTable,
                rssItemTable);
        if (BuildConfig.DEBUG && true) {
            BloclyApplication.getSharedInstance().deleteDatabase("blocly_db");
        }
    }

    private void fetchNewFeed(final String feedURL, final Callback<RssFeed> callback){
        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {

                // query database for feed with specific url
                Cursor existingFeedCursor = RssFeedTable.fetchFeedWithURL(databaseOpenHelper.getReadableDatabase(), feedURL);

                // return fetched feed if database contains it and exit fetchNewFeed
                if (existingFeedCursor.moveToFirst()) {

                    // create and return feed object
                    final RssFeed fetchedFeed = feedFromCursor(existingFeedCursor);
                    existingFeedCursor.close();
                    callbackThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(fetchedFeed);
                        }
                    });

                    // exit fetchNewFeed
                    return;
                }


                // get all feeds (in this case, only one feed)
                GetFeedsNetworkRequest getFeedsNetworkRequest = new GetFeedsNetworkRequest(feedURL);
                List<GetFeedsNetworkRequest.FeedResponse> feedResponses = getFeedsNetworkRequest.performRequest();

                // if error, return error message and exit fetchNewFeed
                if (getFeedsNetworkRequest.getErrorCode() != 0) {
                    final String errorMessage;
                    if (getFeedsNetworkRequest.getErrorCode() == NetworkRequest.ERROR_IO) {
                        errorMessage = "Network error";
                    } else if (getFeedsNetworkRequest.getErrorCode() == NetworkRequest.ERROR_MALFORMED_URL) {
                        errorMessage = "Malformed URL error";
                    } else if (getFeedsNetworkRequest.getErrorCode() == GetFeedsNetworkRequest.ERROR_PARSING) {
                        errorMessage = "Error parsing feed";
                    } else {
                        errorMessage = "Error unknown";
                    }
                    callbackThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(errorMessage);
                        }
                    });
                    return;
                }

                // get first feed response, store it in database,
                GetFeedsNetworkRequest.FeedResponse newFeedResponse = feedResponses.get(0);
                long newFeedId = new RssFeedTable.Builder()
                        .setFeedURL(newFeedResponse.channelFeedUrl)
                        .setSiteURL(newFeedResponse.channelURL)
                        .setTitle(newFeedResponse.channelTitle)
                        .setDescription(newFeedResponse.channelDescription)
                        .insert(databaseOpenHelper.getWritableDatabase());

                // store all item responses (from first feed) in database
                for (GetFeedsNetworkRequest.ItemResponse itemResponse : newFeedResponse.channelItems) {
                    long itemPubDate = System.currentTimeMillis();
                    DateFormat dateFormat =
                            new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss z", Locale.ENGLISH);
                    try {
                        itemPubDate = dateFormat.parse(itemResponse.itemPubDate).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    new RssItemTable.Builder()
                            .setTitle(itemResponse.itemTitle)
                            .setDescription(itemResponse.itemDescription)
                            .setEnclosure(itemResponse.itemEnclosureURL)
                            .setMIMEType(itemResponse.itemEnclosureMIMEType)
                            .setLink(itemResponse.itemURL)
                            .setGUID(itemResponse.itemGUID)
                            .setPubDate(itemPubDate)
                            .setRSSFeed(newFeedId)
                            .insert(databaseOpenHelper.getWritableDatabase());
                }

                // return previously stored feed from database
                Cursor newFeedCursor = rssFeedTable.fetchRow(databaseOpenHelper.getReadableDatabase(), newFeedId);
                newFeedCursor.moveToFirst();
                final RssFeed fetchedFeed = feedFromCursor(newFeedCursor);
                newFeedCursor.close();
                callbackThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(fetchedFeed);
                    }
                });
            }
        });
    }

    private void fetchUpdatedFeed(final String feedURL, final Callback<RssFeed> callback){
        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {

                long feedId;

                // get all feeds (in this case, only one feed)
                GetFeedsNetworkRequest getFeedsNetworkRequest = new GetFeedsNetworkRequest(feedURL);
                List<GetFeedsNetworkRequest.FeedResponse> feedResponses = getFeedsNetworkRequest.performRequest();

                // if error, return error message and exit fetchNewFeed
                if (getFeedsNetworkRequest.getErrorCode() != 0) {
                    final String errorMessage;
                    if (getFeedsNetworkRequest.getErrorCode() == NetworkRequest.ERROR_IO) {
                        errorMessage = "Network error";
                    } else if (getFeedsNetworkRequest.getErrorCode() == NetworkRequest.ERROR_MALFORMED_URL) {
                        errorMessage = "Malformed URL error";
                    } else if (getFeedsNetworkRequest.getErrorCode() == GetFeedsNetworkRequest.ERROR_PARSING) {
                        errorMessage = "Error parsing feed";
                    } else {
                        errorMessage = "Error unknown";
                    }
                    callbackThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(errorMessage);
                        }
                    });
                    return;
                }

                // get first feed response
                GetFeedsNetworkRequest.FeedResponse newFeedResponse = feedResponses.get(0);

                // query database for feed with specified url
                Cursor existingFeedCursor =
                        RssFeedTable.fetchFeedWithURL(databaseOpenHelper.getReadableDatabase(),feedURL);
                // if feed is in database
                if(existingFeedCursor.moveToFirst()){
                    // get feed id
                    feedId = Table.getRowId(existingFeedCursor);
                    existingFeedCursor.close();

                    // check if item for existing feed is in database
                    for (GetFeedsNetworkRequest.ItemResponse itemResponse : newFeedResponse.channelItems) {

                        // query database to see if itemResponse already exists
                        String queryString = "select * from rss_items where rss_feed = " + feedId;
                        Cursor existingItemsCursor =
                                databaseOpenHelper.getReadableDatabase().rawQuery(queryString, null);

                        // if item is not in database, then add it
                        if(!existingItemsCursor.moveToFirst()){
                            existingItemsCursor.close();
                            long itemPubDate = System.currentTimeMillis();
                            DateFormat dateFormat =
                                    new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss z", Locale.ENGLISH);
                            try {
                                itemPubDate = dateFormat.parse(itemResponse.itemPubDate).getTime();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            new RssItemTable.Builder()
                                    .setTitle(itemResponse.itemTitle)
                                    .setDescription(itemResponse.itemDescription)
                                    .setEnclosure(itemResponse.itemEnclosureURL)
                                    .setMIMEType(itemResponse.itemEnclosureMIMEType)
                                    .setLink(itemResponse.itemURL)
                                    .setGUID(itemResponse.itemGUID)
                                    .setPubDate(itemPubDate)
                                    .setRSSFeed(feedId)
                                    .insert(databaseOpenHelper.getWritableDatabase());
                        }
                    }
                }
                // store new feed in database
                else{
                    feedId = new RssFeedTable.Builder()
                            .setFeedURL(newFeedResponse.channelFeedUrl)
                            .setSiteURL(newFeedResponse.channelURL)
                            .setTitle(newFeedResponse.channelTitle)
                            .setDescription(newFeedResponse.channelDescription)
                            .insert(databaseOpenHelper.getWritableDatabase());

                    // store all item responses (from first feed) in database
                    for (GetFeedsNetworkRequest.ItemResponse itemResponse : newFeedResponse.channelItems) {
                        long itemPubDate = System.currentTimeMillis();
                        DateFormat dateFormat =
                                new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss z", Locale.ENGLISH);
                        try {
                            itemPubDate = dateFormat.parse(itemResponse.itemPubDate).getTime();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        new RssItemTable.Builder()
                                .setTitle(itemResponse.itemTitle)
                                .setDescription(itemResponse.itemDescription)
                                .setEnclosure(itemResponse.itemEnclosureURL)
                                .setMIMEType(itemResponse.itemEnclosureMIMEType)
                                .setLink(itemResponse.itemURL)
                                .setGUID(itemResponse.itemGUID)
                                .setPubDate(itemPubDate)
                                .setRSSFeed(feedId)
                                .insert(databaseOpenHelper.getWritableDatabase());
                    }
                }

                // return previously stored feed from database
                Cursor newFeedCursor = rssFeedTable.fetchRow(databaseOpenHelper.getReadableDatabase(), feedId);
                newFeedCursor.moveToFirst();
                final RssFeed fetchedFeed = feedFromCursor(newFeedCursor);
                newFeedCursor.close();
                callbackThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(fetchedFeed);
                    }
                });

            }
        });
    }

    public void fetchFeed(boolean isRefreshed, final String feedURL, final Callback<RssFeed> callback){
        if(!isRefreshed){
            fetchNewFeed(feedURL, callback);
        }else{
            fetchUpdatedFeed(feedURL, callback);
        }
    }

    public void fetchItemsForFeed(final RssFeed rssFeed, final Callback<List<RssItem>> callback){
        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {
                final List<RssItem> resultList = new ArrayList<RssItem>();
                Cursor cursor = RssItemTable.fetchItemsForFeed(
                        databaseOpenHelper.getReadableDatabase(),
                        rssFeed.getRowId());
                if(cursor.moveToFirst()){
                    do{
                        resultList.add(itemFromCursor(cursor));
                    }while(cursor.moveToNext());
                    cursor.close();
                }
                callbackThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(resultList);
                    }
                });
            }
        });
    }

    static RssFeed feedFromCursor(Cursor cursor){
        return new RssFeed(Table.getRowId(cursor), RssFeedTable.getTitle(cursor), RssFeedTable.getDescription(cursor),
                RssFeedTable.getSiteURL(cursor), RssFeedTable.getFeedURL(cursor));
    }

    static RssItem itemFromCursor(Cursor cursor){
        return new RssItem(Table.getRowId(cursor), RssItemTable.getGUID(cursor), RssItemTable.getTitle(cursor),
                RssItemTable.getDescription(cursor), RssItemTable.getLink(cursor),
                RssItemTable.getEnclosure(cursor), RssItemTable.getRssFeedId(cursor),
                RssItemTable.getPubDate(cursor), RssItemTable.getFavorite(cursor),
                RssItemTable.getArchived(cursor));
    }

    void submitTask(Runnable task){
        if(executorService.isShutdown() || executorService.isTerminated()){
            executorService = Executors.newSingleThreadExecutor();
        }
        executorService.submit(task);
    }

}
