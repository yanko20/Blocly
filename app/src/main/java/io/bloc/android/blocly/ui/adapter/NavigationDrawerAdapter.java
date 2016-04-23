package io.bloc.android.blocly.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.model.RssFeed;

/**
 * Created by yankomizorov on 4/21/16.
 */
public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder>{

    public enum NavigationOption{
        NAVIGATION_OPTION_INBOX,
        NAVIGATION_OPTION_FAVORITES,
        NAVIGATION_OPTION_ARCHIVED
    }

    public static interface NavigationDraawerAdapterDelegate{
        public void didSelectNavigationOption(NavigationDrawerAdapter adapter, NavigationOption navigationOption);
        public void didSelectFeed(NavigationDrawerAdapter adapter, RssFeed rssFeed);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.navigation_item, parent, false);
        return new ViewHolder(inflate);
    }

    WeakReference<NavigationDraawerAdapterDelegate> delegate;

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        RssFeed rssFeed = null;

        if(position >= NavigationOption.values().length){
            int feedPosition = position - NavigationOption.values().length;
            rssFeed = BloclyApplication.getSharedDataSource().getFeeds().get(feedPosition);
        }
        holder.update(position, rssFeed);

    }

    @Override
    public int getItemCount() {
        return NavigationOption.values().length + BloclyApplication.getSharedDataSource().getFeeds().size();
    }

    public NavigationDraawerAdapterDelegate getDelegate(){
        return delegate == null ? null : delegate.get();
    }

    public void setDelegate(NavigationDraawerAdapterDelegate delegate){
        this.delegate = new WeakReference<NavigationDraawerAdapterDelegate>(delegate);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener{

        View topPadding;
        TextView title;
        View bottomPadding;
        View divider;
        int position;
        RssFeed rssFeed;

        public ViewHolder(View itemView) {
            super(itemView);
            topPadding = itemView.findViewById(R.id.v_nav_item_top_padding);
            title = (TextView)itemView.findViewById(R.id.tv_nav_item_title);
            bottomPadding = itemView.findViewById(R.id.v_nav_item_bottom_padding);
            divider = itemView.findViewById(R.id.v_nav_item_divider);
            itemView.setOnClickListener(this);
        }

        void update(int position, RssFeed rssFeed){

            this.position = position;
            this.rssFeed = rssFeed;

            boolean shouldShowTopPadding = false;
            boolean shouldShowBottomPadding = false;

            if(position == NavigationOption.NAVIGATION_OPTION_INBOX.ordinal() ||
                    position == NavigationOption.values().length){
                shouldShowTopPadding = true;
            }
            topPadding.setVisibility(shouldShowTopPadding ? View.VISIBLE : View.GONE);


            if(position == NavigationOption.NAVIGATION_OPTION_ARCHIVED.ordinal() || position == getItemCount() - 1){
                shouldShowBottomPadding = true;
            }
            bottomPadding.setVisibility(shouldShowBottomPadding ? View.VISIBLE : View.GONE);

            if(position == NavigationOption.NAVIGATION_OPTION_ARCHIVED.ordinal()){
                divider.setVisibility(View.VISIBLE);
            }else{
                divider.setVisibility(View.GONE);
            }

            if(position < NavigationOption.values().length){
                int[] titleTexts = new int[] {
                        R.string.navigation_option_inbox,
                        R.string.navigation_option_favorites,
                        R.string.navigation_option_archived
                };
                title.setText(titleTexts[position]);
            }else{
                title.setText(rssFeed.getTitle());
            }

        }

        @Override
        public void onClick(View v) {
            if (getDelegate() == null) return;
            if (position < NavigationOption.values().length){
                getDelegate().didSelectNavigationOption(
                        NavigationDrawerAdapter.this,
                        NavigationOption.values()[position]);
            } else {
                getDelegate().didSelectFeed(NavigationDrawerAdapter.this, rssFeed);
            }
        }
    }
}
