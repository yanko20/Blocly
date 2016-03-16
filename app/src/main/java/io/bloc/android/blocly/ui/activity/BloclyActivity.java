package io.bloc.android.blocly.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import io.bloc.android.blocly.R;

/**
 * Created by yankomizorov on 3/15/16.
 */
public class BloclyActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocly);
    }
}
