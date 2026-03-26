package de.nulide.shiftcal.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;

public class ColorHelper {
    public static int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        if (hsv[2] < 0.03) {
            hsv[2] = 0.0f;
            hsv[1] += 0.2f;
        } else {
            hsv[2] -= 0.2f;
        }
        return Color.HSVToColor(hsv);
    }

    public static int brightenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        if (hsv[2] > 0.97) {
            hsv[2] = 1.0f;
            hsv[1] -= 0.2f;
        } else {
            hsv[2] += 0.2f;
        }
        return Color.HSVToColor(hsv);
    }

    public static boolean isTooBright(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return hsv[2] > 0.75;
    }

    public static int getColorAttr(Context context, Integer attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

}
