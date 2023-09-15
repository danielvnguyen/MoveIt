package com.danielvnguyen.moveit.model.theme;

import android.content.Context;
import android.util.TypedValue;

//Utility class to obtain theme-specific attributes
public class ThemeUtils {
    public static int getTextColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.textColor, typedValue, true);
        return typedValue.data;
    }
}