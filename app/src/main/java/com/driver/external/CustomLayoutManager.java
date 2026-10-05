package com.driver.external;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * Created by hitasoft on 13/2/18.
 */

public class CustomLayoutManager extends LinearLayoutManager

{
    private boolean isScrollEnabled = true;

    public CustomLayoutManager(Context context) {
        super(context);
    }

    public void setScrollEnabled(boolean flag) {
        this.isScrollEnabled = flag;
    }

    @Override
    public boolean canScrollVertically() {
        //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
        return isScrollEnabled && super.canScrollVertically();
    }
}

