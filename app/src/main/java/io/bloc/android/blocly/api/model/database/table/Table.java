package io.bloc.android.blocly.api.model.database.table;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by yankomizorov on 5/9/16.
 */
public abstract class Table {

    public static interface Builder{
        public long insert(SQLiteDatabase writabeDB);
    }

    protected static final String COLUMN_ID = "id";

    public abstract String getName();
    public abstract String getCreateStatement();

    public void onUpgrade(SQLiteDatabase writableDatabase, int oldVersion, int newVersion){

    }
}
