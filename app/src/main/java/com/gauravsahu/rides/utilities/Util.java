package com.gauravsahu.rides.utilities;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by gasahu on 11-Aug-17.
 */

public class Util {
    private static Toast toast;

    public static void showToastMessage(Context context, String toastText) {
        if(toast != null) {
            toast.cancel();
        }

        toast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT);
        toast.show();
    }
}
