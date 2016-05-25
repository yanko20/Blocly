package io.bloc.android.blocly.api.model;

/**
 * Created by yankomizorov on 5/23/16.
 */
public abstract class Model {

    private final long rowId;

    public Model(long rowId){
        this.rowId = rowId;
    }

    public long getRowId(){
        return rowId;
    }

}
