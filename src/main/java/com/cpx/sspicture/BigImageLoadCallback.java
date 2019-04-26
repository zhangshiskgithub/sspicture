package com.cpx.sspicture;

import android.graphics.Bitmap;
import android.view.View;

/**
 * desc: <br>
 * author by zsq <br>
 * create on 2018/7/18 16:02<br>
 */

public interface BigImageLoadCallback {
    void onLoad(View view, Bitmap bitmap);
}
