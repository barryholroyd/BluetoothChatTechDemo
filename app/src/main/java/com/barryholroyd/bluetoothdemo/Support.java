package com.barryholroyd.bluetoothdemo;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Barry on 12/13/2016.
 */

public class Support {
    public static void userError(Context c, String msg) {
        Toast t = Toast.makeText(c, msg, Toast.LENGTH_LONG);
        t.show();
    }
}
