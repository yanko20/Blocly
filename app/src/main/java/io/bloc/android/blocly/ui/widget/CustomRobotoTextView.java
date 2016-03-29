package io.bloc.android.blocly.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import io.bloc.android.blocly.R;

/**
 * Created by yankomizorov on 3/26/16.
 */
public class CustomRobotoTextView extends TextView {

    private static Map<String, Typeface> sTypefaces = new HashMap<>();

    public CustomRobotoTextView(Context context){
        super(context);
    }

    public CustomRobotoTextView(Context context, AttributeSet attrs){
        super(context, attrs);
        extractFont(attrs);
    }

    public CustomRobotoTextView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        extractFont(attrs);
    }

    void extractFont(AttributeSet attrs){
        // #5
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            return;
        }
        // #6
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.Roboto, 0, 0);
        // #7
        int robotoFontKey = typedArray.getInteger(R.styleable.Roboto_robotoStyle, -1);
        boolean isCondensed = typedArray.getBoolean(R.styleable.Roboto_condensed, false);
        if(isCondensed){ robotoFontKey++; }
        // #8
        typedArray.recycle();
        // #9
        String[] stringArray = getResources().getStringArray(R.array.roboto_font_file_names);
        int[] integerArray = getResources().getIntArray(R.array.roboto_font_file_values);
        if (robotoFontKey == -1) {
            return;
        }

        HashMap<Integer,String> map = new HashMap<>();
        for(int i =0; i < Math.min(stringArray.length, integerArray.length);i++){
            map.put(integerArray[i], stringArray[i]);
        }
        String robotoFont = map.get(robotoFontKey);
        Typeface robotoTypeface = null;
        // #10
        if (sTypefaces.containsKey(robotoFont)) {
            robotoTypeface = sTypefaces.get(robotoFont);
        } else {
            // #11
            robotoTypeface = Typeface.createFromAsset(getResources().getAssets(), "fonts/RobotoTTF/" + robotoFont);
            sTypefaces.put(robotoFont, robotoTypeface);
        }
        // #12
        setTypeface(robotoTypeface);
    }

}
