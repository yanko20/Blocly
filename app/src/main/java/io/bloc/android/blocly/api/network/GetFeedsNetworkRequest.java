package io.bloc.android.blocly.api.network;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by yankomizorov on 5/4/16.
 */
public class GetFeedsNetworkRequest extends NetworkRequest {

    String[] feedUrls;

    public GetFeedsNetworkRequest(String... feedUrls){
        this.feedUrls = feedUrls;
    }

    @Override
    public Object performRequest() {
        for(String feedUrlString : feedUrls){
            InputStream inputStream = openStream(feedUrlString);
            if(inputStream == null){
                return null;
            }
            try{
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = bufferedReader.readLine();
                while(line != null){
                    Log.v(getClass().getSimpleName(), "Line: " + line);
                    line = bufferedReader.readLine();
                }
                bufferedReader.close();
            }catch(IOException e){
                e.printStackTrace();
                setErrorCode(ERROR_IO);
                return null;
            }
        }
        return null;
    }

    public Object performMyRequest() {
        for(String feedUrlString : feedUrls){
            InputStream inputStream = openStream(feedUrlString);
            if(inputStream == null){
                return null;
            }
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(inputStream, "UTF-8");
                int eventType = xpp.getEventType();
                int numberOfTitles = 0;
                while(eventType != XmlPullParser.END_DOCUMENT){
                    if(eventType == XmlPullParser.START_TAG && xpp.getName().equals("item")) {
                        numberOfTitles++;
                    } else if(eventType == XmlPullParser.START_TAG && xpp.getName().equals("title")) {
                        Log.v(getClass().getSimpleName(), "title: " + xpp.nextText());
                    } else if(eventType == XmlPullParser.START_TAG && xpp.getName().equals("link")) {
                        Log.v(getClass().getSimpleName(), "title: " + xpp.nextText());
                    }
                    eventType = xpp.next();
                }
                Log.v(getClass().getSimpleName(), "Number of titles: " + numberOfTitles);

            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
