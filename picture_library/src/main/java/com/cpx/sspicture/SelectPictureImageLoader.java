package com.cpx.sspicture;

import android.content.Context;
import android.widget.ImageView;

/**
 * desc: 选择图片时加载图片的接口<br>
 * author by zsq <br>
 * create on 16/5/8 19:02<br>
 */
public interface SelectPictureImageLoader {
    void displayImage(Context context, String path, ImageView imageView);

    void displayBigImage(Context context,String path,ImageView imageView,BigImageLoadCallback callback);
}
