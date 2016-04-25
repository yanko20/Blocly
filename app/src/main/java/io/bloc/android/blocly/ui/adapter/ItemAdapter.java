package io.bloc.android.blocly.ui.adapter;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.lang.ref.WeakReference;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.DataSource;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;

/**
 * Created by yankomizorov on 3/22/16.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemAdapterViewHolder> {

    private static String TAG = ItemAdapter.class.getSimpleName();
    WeakReference<ItemAdapterDelegate> delegate;

    public static interface ItemAdapterDelegate{
        public void didExpandItem(RssItem item);
        public void didContractItem(RssItem item);
        public void didWishToVisitSite();
        public void didFavoriteItem(RssItem item);
        public void didUnfavoriteItem(RssItem item);
        public void didArchiveItem(RssItem item);
        public void didUnarchiveItem(RssItem item);
    }

    public ItemAdapterDelegate getDelegate(){
        return delegate == null ? null : delegate.get();
    }

    public void setDelegate(ItemAdapterDelegate delegate){
        this.delegate = new WeakReference<ItemAdapterDelegate>(delegate);
    }

    @Override
    public ItemAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rss_item, parent, false);
        return new ItemAdapterViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(ItemAdapterViewHolder holder, int position) {
        DataSource sharedDataSource = BloclyApplication.getSharedDataSource();
        holder.update(sharedDataSource.getFeeds().get(0), sharedDataSource.getItems().get(position));
    }

    @Override
    public int getItemCount() {
        return BloclyApplication.getSharedDataSource().getItems().size();
    }

    class ItemAdapterViewHolder extends RecyclerView.ViewHolder implements ImageLoadingListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        boolean contentExpanded;
        TextView title;
        TextView feed;
        TextView content;
        View headerWrapper;
        ImageView headerImage;
        CheckBox archiveCheckbox;
        CheckBox favoriteCheckbox;
        View expandedContentWrapper;
        TextView expandedContent;
        TextView visitSite;
        RssItem rssItem;

        public ItemAdapterViewHolder(View itemView){
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tv_rss_item_title);
            feed = (TextView) itemView.findViewById(R.id.tv_rss_item_feed_title);
            content = (TextView) itemView.findViewById(R.id.tv_rss_item_content);
            headerWrapper = itemView.findViewById(R.id.fl_rss_item_image_header);
            headerImage = (ImageView)headerWrapper.findViewById(R.id.iv_rss_item_image);
            archiveCheckbox = (CheckBox)itemView.findViewById(R.id.cb_rss_item_check_mark);
            favoriteCheckbox = (CheckBox) itemView.findViewById(R.id.cb_rss_item_favorite_star);
            expandedContentWrapper = itemView.findViewById(R.id.ll_rss_item_expanded_content_wrapper);
            expandedContent = (TextView) expandedContentWrapper.findViewById(R.id.tv_rss_item_content_full);
            visitSite = (TextView) expandedContentWrapper.findViewById(R.id.tv_rss_item_visit_site);
            itemView.setOnClickListener(this);
            visitSite.setOnClickListener(this);
            archiveCheckbox.setOnCheckedChangeListener(this);
            favoriteCheckbox.setOnCheckedChangeListener(this);
        }

        void update(RssFeed rssFeed, RssItem rssItem){
            this.rssItem = rssItem;
            feed.setText(rssFeed.getTitle());
            title.setText(rssItem.getTitle());
            content.setText(rssItem.getDescription());
            expandedContent.setText(rssItem.getDescription());
            if(rssItem.getImageUrl() != null){
                headerWrapper.setVisibility(View.VISIBLE);
                headerImage.setVisibility(View.INVISIBLE);
                ImageLoader.getInstance().loadImage(rssItem.getImageUrl(), this);
            }else{
                headerWrapper.setVisibility(View.GONE);
            }
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
                animateContent(!contentExpanded);
            } else {
                getDelegate().didWishToVisitSite();
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.v(TAG, "Checked changed to: " + isChecked);
            if(buttonView.getId() == favoriteCheckbox.getId()){
                if(isChecked){
                    getDelegate().didFavoriteItem(rssItem);
                }else{
                    getDelegate().didUnfavoriteItem(rssItem);
                }
            } else if(buttonView.getId() == archiveCheckbox.getId()){
                if(isChecked){
                    getDelegate().didArchiveItem(rssItem);
                }else{
                    getDelegate().didUnarchiveItem(rssItem);
                }
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
                getDelegate().didExpandItem(rssItem);
                // #3
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
                getDelegate().didContractItem(rssItem);
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
