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
        double luminance =
                (0.299 * Color.red(color) +
                        0.587 * Color.green(color) +
                        0.114 * Color.blue(color)) / 255.0;
        return luminance > 0.62;
    }

    public static int getColorAttr(Context context, Integer attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

}
