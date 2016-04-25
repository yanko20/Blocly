package io.bloc.android.blocly.ui.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;
import io.bloc.android.blocly.ui.adapter.ItemAdapter;
import io.bloc.android.blocly.ui.adapter.NavigationDrawerAdapter;

/**
 * Created by yankomizorov on 3/15/16.
 */
public class BloclyActivity extends AppCompatActivity implements NavigationDrawerAdapter.NavigationDraawerAdapterDelegate, ItemAdapter.ItemAdapterDelegate{

    private ItemAdapter adapter;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationDrawerAdapter navigationDrawerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocly);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_activity_blocly);
        setSupportActionBar(toolbar);
        adapter = new ItemAdapter();
        adapter.setDelegate(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_activity_blocly);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerLayout = (DrawerLayout) findViewById(R.id.dl_activity_blocly);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, 0, 0);
        drawerLayout.addDrawerListener(drawerToggle);

        navigationDrawerAdapter = new NavigationDrawerAdapter();
        navigationDrawerAdapter.setDelegate(this);
        RecyclerView navigationRecyclerView = (RecyclerView)findViewById(R.id.rv_nav_activity_blocly);
        navigationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        navigationRecyclerView.setItemAnimator(new DefaultItemAnimator());
        navigationRecyclerView.setAdapter(navigationDrawerAdapter);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void didSelectNavigationOption(NavigationDrawerAdapter adapter, NavigationDrawerAdapter.NavigationOption navigationOption) {
        drawerLayout.closeDrawers();
        Toast.makeText(
                this,
                "Show the " + navigationOption.name(),
                Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void didSelectFeed(NavigationDrawerAdapter adapter, RssFeed rssFeed) {
        drawerLayout.closeDrawers();
        Toast.makeText(
                this,
                "Show RSS item from  " + rssFeed.getTitle(),
                Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void didExpandItem(RssItem item) {
        Toast.makeText(this, "Expanded item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didContractItem(RssItem item) {
        Toast.makeText(this, "Contracted item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didWishToVisitSite() {
        Toast.makeText(this, "Wish to visit site", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didFavoriteItem(RssItem item) {
        Toast.makeText(this, "Favorite item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didUnfavoriteItem(RssItem item) {
        Toast.makeText(this, "Unfavorite item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didArchiveItem(RssItem item) {
        Toast.makeText(this, "Archived item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didUnarchiveItem(RssItem item) {
        Toast.makeText(this, "Unarchived item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
