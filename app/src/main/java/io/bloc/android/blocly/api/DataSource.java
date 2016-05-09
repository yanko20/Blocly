package io.bloc.android.blocly.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;
import io.bloc.android.blocly.api.network.GetFeedsNetworkRequest;

/**
 * Created by yankomizorov on 3/20/16.
 */
public class DataSource {

    private List<RssFeed> feeds;
    private List<RssItem> items;

    public DataSource(){
        feeds = new ArrayList<RssFeed>();
        items = new ArrayList<RssItem>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<GetFeedsNetworkRequest.FeedResponse> feedResponses =
                        new GetFeedsNetworkRequest("http://feeds.feedburner.com/androidcentral?format=xml")
                                .performRequest();
                for(GetFeedsNetworkRequest.FeedResponse feedResponse : feedResponses){
                    feeds.add(new RssFeed(
                            feedResponse.channelTitle,
                            feedResponse.channelDescription,
                            feedResponse.channelURL, feedResponse.channelFeedUrl));
                    for(GetFeedsNetworkRequest.ItemResponse itemResponse : feedResponse.channelItems){
                        items.add(new RssItem(
                                itemResponse.itemGUID,
                                itemResponse.itemTitle,
                                itemResponse.itemDescription,
                                itemResponse.itemURL,
                                itemResponse.itemEnclosureURL,
                                0,
                                getDate(itemResponse.itemPubDate),
                                false,
                                false
                        ));
                    }
                }
            }
        }).start();
    }

    private long getDate(String dateString){
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy H:mm:ss z");
        try {
            Date date = sdf.parse(dateString);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<RssFeed> getFeeds() {
        return feeds;
    }

    public List<RssItem> getItems() {
        return items;
    }

}
