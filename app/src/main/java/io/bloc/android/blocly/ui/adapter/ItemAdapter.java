package io.bloc.android.blocly.ui.adapter;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.ui.UIUtils;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;

/**
 * Created by yankomizorov on 3/22/16.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemAdapterViewHolder> {

    public static interface DataSource{
        public RssItem getRssItem(ItemAdapter itemAdapter, int position);
        public RssFeed getRssFeed(ItemAdapter itemAdapter, int position);
        public int getItemCount(ItemAdapter itemAdapter);
    }

    public static interface Delegate{
        public void onItemClicked(ItemAdapter itemAdapter, RssItem rssItem);
        public void onVisitClicked(ItemAdapter itemAdapter, RssItem rssItem);
    }

    private static String TAG = "yanstag";
    private Map<Long, Integer> rssFeedToColor = new HashMap<>();
    private RssItem expandedItem = null;

    private WeakReference<Delegate> delegate;
    private WeakReference<DataSource> dataSource;
    private int collapsedItemHeight;
    private int expandedItemHeight;

    public DataSource getDataSource(){
        return dataSource == null ? null : dataSource.get();
    }

    public void setDataSource(DataSource dataSource){
        this.dataSource = new WeakReference<DataSource>(dataSource);
    }

    public Delegate getDelegate(){
        return delegate == null ? null : delegate.get();
    }

    public void setDelegate(Delegate delegate){
        this.delegate = new WeakReference<Delegate>(delegate);
    }

    public RssItem getExpandedItem(){
        return expandedItem;
    }

    public void setExpandedItem(RssItem expandedItem){
        this.expandedItem = expandedItem;
    }

    public int getCollapsedItemHeight() {
        return collapsedItemHeight;
    }

    public void setCollapsedItemHeight(int collapsedItemHeight) {
        this.collapsedItemHeight = collapsedItemHeight;
    }

    public int getExpandedItemHeight() {
        return expandedItemHeight;
    }

    public void setExpandedItemHeight(int expandedItemHeight) {
        this.expandedItemHeight = expandedItemHeight;
    }

    @Override
    public ItemAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rss_item, parent, false);
        return new ItemAdapterViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(ItemAdapterViewHolder holder, int position) {
        if(getDataSource() == null){
            return;
        }
        RssItem rssItem = getDataSource().getRssItem(this, position);
        RssFeed rssFeed = getDataSource().getRssFeed(this, position);
        holder.update(rssFeed, rssItem);
    }

    @Override
    public int getItemCount() {
        if(getDataSource() == null){
            return 0;
        }
        return getDataSource().getItemCount(this);
    }

    class ItemAdapterViewHolder extends RecyclerView.ViewHolder implements ImageLoadingListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        boolean onTablet;
        boolean contentExpanded;
        TextView title;
        TextView content;

        // Phone only
        TextView feed;
        View headerWrapper;
        ImageView headerImage;
        CheckBox archiveCheckbox;
        CheckBox favoriteCheckbox;
        View expandedContentWrapper;
        TextView expandedContent;
        TextView visitSite;

        // Tablet only
        TextView callout;

        RssItem rssItem;

        public ItemAdapterViewHolder(View itemView){
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tv_rss_item_title);
            content = (TextView) itemView.findViewById(R.id.tv_rss_item_content);
            if (itemView.findViewById(R.id.tv_rss_item_feed_title) != null) {
                feed = (TextView) itemView.findViewById(R.id.tv_rss_item_feed_title);
                headerWrapper = itemView.findViewById(R.id.fl_rss_item_image_header);
                headerImage = (ImageView) headerWrapper.findViewById(R.id.iv_rss_item_image);
                archiveCheckbox = (CheckBox) itemView.findViewById(R.id.cb_rss_item_check_mark);
                favoriteCheckbox = (CheckBox) itemView.findViewById(R.id.cb_rss_item_favorite_star);
                expandedContentWrapper = itemView.findViewById(R.id.ll_rss_item_expanded_content_wrapper);
                expandedContent = (TextView) expandedContentWrapper.findViewById(R.id.tv_rss_item_content_full);
                visitSite = (TextView) expandedContentWrapper.findViewById(R.id.tv_rss_item_visit_site);
                visitSite.setOnClickListener(this);
                archiveCheckbox.setOnCheckedChangeListener(this);
                favoriteCheckbox.setOnCheckedChangeListener(this);
            } else {
                // Recover Tablet Views
                onTablet = true;
                callout = (TextView) itemView.findViewById(R.id.tv_rss_item_callout);
                // #3
                if (Build.VERSION.SDK_INT >= 21) {
                    callout.setOutlineProvider(new ViewOutlineProvider() {
                        @SuppressLint("NewApi")
                        @Override
                        public void getOutline(View view, Outline outline) {
                            outline.setOval(0, 0, view.getWidth(), view.getHeight());
                        }
                    });
                    callout.setClipToOutline(true);
                }
            }
            itemView.setOnClickListener(this);
        }

        void update(RssFeed rssFeed, RssItem rssItem){
            this.rssItem = rssItem;
            title.setText(rssItem.getTitle());
            content.setText(rssItem.getDescription());
            if (onTablet) {
                // #4
                callout.setText("" + Character.toUpperCase(rssFeed.getTitle().charAt(0)));
                Integer color = rssFeedToColor.get(rssFeed.getRowId());
                if (color == null) {
                    color = UIUtils.generateRandomColor(ContextCompat.getColor(callout.getContext(), android.R.color.white));
                    rssFeedToColor.put(rssFeed.getRowId(), color);
                }
                callout.setBackgroundColor(color);
                return;
            }
            feed.setText(rssFeed.getTitle());
            expandedContent.setText(rssItem.getDescription());
            if(rssItem.getImageUrl() != null){
                headerWrapper.setVisibility(View.VISIBLE);
                headerImage.setVisibility(View.INVISIBLE);
                ImageLoader.getInstance().loadImage(rssItem.getImageUrl(), this);
            }else{
                headerWrapper.setVisibility(View.GONE);
            }
            animateContent(getExpandedItem() == rssItem);
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {

        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            Log.e(TAG, "onLoadingFailed: " + failReason.toString() + "for URL: " + imageUri);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if(imageUri.equals(rssItem.getImageUrl())){
                headerImage.setImageBitmap(loadedImage);
                headerImage.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            ImageLoader.getInstance().loadImage(imageUri, this);
        }

        @Override
        public void onClick(View view) {
            if(view == itemView){
                if(getDelegate() != null){
                    getDelegate().onItemClicked(ItemAdapter.this, rssItem);
                }
            } else {
                Toast.makeText(view.getContext(), "Visit " + rssItem.getUrl(), Toast.LENGTH_SHORT).show();
                getDelegate().onVisitClicked(ItemAdapter.this, rssItem);
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(buttonView.getId() == R.id.cb_rss_item_favorite_star){
                Log.v(TAG, "Clicked on favorite..");
                Log.v(TAG, "RSS Item guid: " + rssItem.getGuid());
                int checked = isChecked ? 1 : 0;
                BloclyApplication.getSharedDataSource().updateFavorite(checked, rssItem.getGuid());
            }

        }

        private void animateContent(final boolean expand) {
            // #1
            if ((expand && contentExpanded) || (!expand && !contentExpanded)) {
                return;
            }
            // #2
            int startingHeight = expandedContentWrapper.getMeasuredHeight();
            int finalHeight = content.getMeasuredHeight();
            if (expand) {
                setCollapsedItemHeight(itemView.getHeight());
                startingHeight = finalHeight;
                expandedContentWrapper.setAlpha(0f);
                expandedContentWrapper.setVisibility(View.VISIBLE);
                // #4
                expandedContentWrapper.measure(
                        View.MeasureSpec.makeMeasureSpec(content.getWidth(), View.MeasureSpec.EXACTLY),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                finalHeight = expandedContentWrapper.getMeasuredHeight();
            } else {
                content.setVisibility(View.VISIBLE);
            }
            startAnimator(startingHeight, finalHeight, new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    // #5
                    float animatedFraction = valueAnimator.getAnimatedFraction();
                    float wrapperAlpha = expand ? animatedFraction : 1f - animatedFraction;
                    float contentAlpha = 1f - wrapperAlpha;

                    expandedContentWrapper.setAlpha(wrapperAlpha);
                    content.setAlpha(contentAlpha);
                    // #6
                    expandedContentWrapper.getLayoutParams().height = animatedFraction == 1f ?
                            ViewGroup.LayoutParams.WRAP_CONTENT :
                            (Integer) valueAnimator.getAnimatedValue();
                    // #7
                    expandedContentWrapper.requestLayout();
                    if (animatedFraction == 1f) {
                        if (expand) {
                            content.setVisibility(View.GONE);
                            setExpandedItemHeight(itemView.getHeight());
                        } else {
                            expandedContentWrapper.setVisibility(View.GONE);
                        }
                    }
                }
            });
            contentExpanded = expand;
        }

        private void startAnimator(int start, int end, ValueAnimator.AnimatorUpdateListener animatorUpdateListener){
            ValueAnimator valueAnimator = ValueAnimator.ofInt(start, end);
            valueAnimator.addUpdateListener(animatorUpdateListener);
            valueAnimator.setDuration(itemView.getResources().getInteger(android.R.integer.config_mediumAnimTime));
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.start();
        }
    }

}
