package io.bloc.android.blocly.ui.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import io.bloc.android.blocly.R;
import io.bloc.android.blocly.ui.adapter.ItemAdapter;

/**
 * Created by yankomizorov on 3/15/16.
 */
public class BloclyActivity extends Activity implements ImageLoadingListener{

    private ItemAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocly);
        adapter = new ItemAdapter();
        recyclerView = (RecyclerView) findViewById(R.id.rv_activity_blocly);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        String imageURL =
                "https://wallpaperscraft.com/image/spots_background_light_blue_white_85546_540x960.jpg";
        ImageLoader.getInstance().loadImage(imageURL, this);
    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {

    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

    }

    @Override @SuppressWarnings("deprecation")
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        Drawable drawable = new BitmapDrawable(getResources(), loadedImage);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            recyclerView.setBackgroundDrawable(drawable);
        } else {
            recyclerView.setBackground(drawable);
        }
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {

    }
}
