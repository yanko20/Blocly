package io.bloc.android.blocly.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;

/**
 * Created by yankomizorov on 3/15/16.
 */
public class BloclyActivity extends Activity{

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocly);
        Toast.makeText(this, BloclyApplication.getSharedDataSource().getFeeds().get(0).getTitle(),
        Toast.LENGTH_LONG).show();
        textView = (TextView)findViewById(R.id.mainTextView);
        textView.setText(BloclyApplication.getSharedDataSource().getFeeds().get(0).getDescription());
    }
}
