package com.driver.helper;

import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.driver.cabiappdriver.R;

/** Presentation helpers for driver cards built in Java instead of XML. */
public final class DriverUi {
    private DriverUi() { }

    public static void card(LinearLayout view) {
        int inset = view.getResources().getDimensionPixelSize(R.dimen.driver_space_medium);
        int gap = view.getResources().getDimensionPixelSize(R.dimen.driver_space_small);
        view.setBackgroundResource(R.drawable.driver_card);
        view.setPadding(inset, inset, inset, inset);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = gap;
        view.setLayoutParams(params);
    }

    public static void title(TextView view) {
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, view.getResources().getDimension(R.dimen.driver_type_body));
        view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.textPrimary));
        view.setTypeface(ResourcesCompat.getFont(view.getContext(), R.font.font_regular), Typeface.BOLD);
        int gap = view.getResources().getDimensionPixelSize(R.dimen.driver_space_small);
        view.setPadding(0, 0, 0, gap);
    }

    public static void body(TextView view) {
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, view.getResources().getDimension(R.dimen.driver_type_body));
        view.setTypeface(ResourcesCompat.getFont(view.getContext(), R.font.font_regular));
        view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.driver_muted));
        int gap = view.getResources().getDimensionPixelSize(R.dimen.driver_space_small);
        view.setPadding(0, 0, 0, gap);
    }
}
