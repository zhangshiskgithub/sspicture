package com.cpx.sspicture;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cpx.sspicture.utils.BitmapUtil;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 * desc: <br>
 * author by zsq <br>
 * create on 2018/10/31 16:21<br>
 */

public class CropImageActivity extends AppCompatActivity {
    private LinearLayout ll_back;
    private TextView tv_title_right;
    private CropImageView clip_image_view;
    public static final String EXTRA_IMG = "image";
    public static final String EXTRA_WIDTH = "width";
    public static final String EXTRA_HEIGHT = "height";
    public static final String EXTRA_PADDING_SCALE = "paddingScale";
    // 大图被设置之前的缩放比例
    private int mSampleSize;
    private int mSourceWidth;
    private int mSourceHeight;
    private int mDegree;
    private File cropCacheDir;
    private int mMaxWidth = 720;

    /**
     * 开启页面
     * @param activity
     * @param fromPath      图片地址
     * @param requestCode
     */
    public static void startPage(Activity activity, String fromPath,int width,int height,int requestCode){
        Intent intent = new Intent(activity,CropImageActivity.class);
        intent.putExtra(EXTRA_IMG,fromPath);
        intent.putExtra(EXTRA_HEIGHT,height);
        intent.putExtra(EXTRA_WIDTH,width);
        activity.startActivityForResult(intent,requestCode);
        startPage(activity, fromPath, width, height,0.2f, requestCode);
    }

    /**
     * 开启页面
     * @param activity
     * @param fromPath      图片地址
     * @param requestCode
     */
    public static void startPage(Activity activity, String fromPath,int width,int height,float paddingScale, int requestCode){
        Intent intent = new Intent(activity,CropImageActivity.class);
        intent.putExtra(EXTRA_IMG,fromPath);
        intent.putExtra(EXTRA_HEIGHT,height);
        intent.putExtra(EXTRA_WIDTH,width);
        intent.putExtra(EXTRA_PADDING_SCALE,paddingScale);
        activity.startActivityForResult(intent,requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        findView();
        setListener();
        locgic();
        cropCacheDir = CacheConfigure.getCropPictureCacheDir(this);
    }

    private void initLayout() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// 锁定竖屏
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 没有标题栏
        setContentView(R.layout.activity_crop_picture);
    }

    private void findView() {
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        tv_title_right = (TextView) findViewById(R.id.tv_title_right);
        clip_image_view = (CropImageView) findViewById(R.id.clip_image_view);
        clip_image_view.setAspect(getCropWidth(),getCropHeight());
        clip_image_view.setmClipPadding((int) (getMobileWidth()*getPaddingScale()));
    }

    private void setListener() {
        tv_title_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCropImage();

            }
        });
        ll_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void saveCropImage(){
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                String name = DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance()) + ".jpg";
                File srcFile = new File(cropCacheDir, name);
                try {
                    Bitmap bitmap = createClippedBitmap();
                    BitmapUtil.saveBitmapToFile(bitmap,srcFile);
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                    Intent data = new Intent();
                    data.putExtra(EXTRA_IMG,srcFile.getPath());
                    setResult(Activity.RESULT_OK, data);
                } catch (Exception e) {
                    Toast.makeText(CropImageActivity.this, "保存图片失败", Toast.LENGTH_SHORT).show();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                finish();
            }
        };
        task.execute();
    }

    private String getFromUri(){
        return getIntent().getStringExtra(EXTRA_IMG);
    }

    private int getCropWidth(){
        return getIntent().getIntExtra(EXTRA_WIDTH,250);
    }
    private int getCropHeight(){
        return getIntent().getIntExtra(EXTRA_HEIGHT,330);
    }
    private float getPaddingScale(){
        return getIntent().getFloatExtra(EXTRA_PADDING_SCALE,0.2f);
    }
    private void locgic() {
        setImageAndClipParams();
    }

    private void setImageAndClipParams() {
        clip_image_view.post(new Runnable() {
            @Override
            public void run() {
                mDegree = readPictureDegree(getFromUri());

                final boolean isRotate = (mDegree == 90 || mDegree == 270);
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(getFromUri(), options);

                mSourceWidth = options.outWidth;
                mSourceHeight = options.outHeight;

                // 如果图片被旋转，则宽高度置换
                int w = isRotate ? options.outHeight : options.outWidth;

                // 裁剪是宽高比例3:2，只考虑宽度情况，这里按border宽度的两倍来计算缩放。
                mSampleSize = findBestSample(w, clip_image_view.getClipBorder().width());

                options.inJustDecodeBounds = false;
                options.inSampleSize = mSampleSize;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                final Bitmap source = BitmapFactory.decodeFile(getFromUri(), options);
// 解决图片被旋转的问题
                Bitmap target;
                if (mDegree == 0) {
                    target = source;
                } else {
                    final Matrix matrix = new Matrix();
                    matrix.postRotate(mDegree);
                    target = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
                    if (target != source && !source.isRecycled()) {
                        source.recycle();
                    }
                }
                clip_image_view.setImageBitmap(target);
            }
        });
    }
    public int getMobileWidth() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        return width;
    }
    /**
     * 计算最好的采样大小。
     *
     * @param origin 当前宽度
     * @param target 限定宽度
     * @return sampleSize
     */
    private static int findBestSample(int origin, int target) {
        int sample = 1;
        for (int out = origin / 2; out > target; out /= 2) {
            sample *= 2;
        }
        return sample;
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    private Bitmap createClippedBitmap() {
        if (mSampleSize <= 1) {
            return clip_image_view.clip();
        }

        // 获取缩放位移后的矩阵值
        final float[] matrixValues = clip_image_view.getClipMatrixValues();
        final float scale = matrixValues[Matrix.MSCALE_X];
        final float transX = matrixValues[Matrix.MTRANS_X];
        final float transY = matrixValues[Matrix.MTRANS_Y];

        // 获取在显示的图片中裁剪的位置
        final Rect border = clip_image_view.getClipBorder();
        final float cropX = ((-transX + border.left) / scale) * mSampleSize;
        final float cropY = ((-transY + border.top) / scale) * mSampleSize;
        final float cropWidth = (border.width() / scale) * mSampleSize;
        final float cropHeight = (border.height() / scale) * mSampleSize;

        // 获取在旋转之前的裁剪位置
        final RectF srcRect = new RectF(cropX, cropY, cropX + cropWidth, cropY + cropHeight);
        final Rect clipRect = getRealRect(srcRect);

        final BitmapFactory.Options ops = new BitmapFactory.Options();
        final Matrix outputMatrix = new Matrix();

        outputMatrix.setRotate(mDegree);
        // 如果裁剪之后的图片宽高仍然太大,则进行缩小
        if (mMaxWidth > 0 && cropWidth > mMaxWidth) {
            ops.inSampleSize = findBestSample((int) cropWidth, mMaxWidth);

            final float outputScale = mMaxWidth / (cropWidth / ops.inSampleSize);
            outputMatrix.postScale(outputScale, outputScale);
        }

        // 裁剪
        BitmapRegionDecoder decoder = null;
        try {
            decoder = BitmapRegionDecoder.newInstance(getFromUri(), false);
            final Bitmap source = decoder.decodeRegion(clipRect, ops);
            recycleImageViewBitmap();
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), outputMatrix, false);
        } catch (Exception e) {
            return clip_image_view.clip();
        } finally {
            if (decoder != null && !decoder.isRecycled()) {
                decoder.recycle();
            }
        }
    }
    private void recycleImageViewBitmap() {
        clip_image_view.post(new Runnable() {
            @Override
            public void run() {
                clip_image_view.setImageBitmap(null);
            }
        });
    }

    private Rect getRealRect(RectF srcRect) {
        switch (mDegree) {
            case 90:
                return new Rect((int) srcRect.top, (int) (mSourceHeight - srcRect.right),
                        (int) srcRect.bottom, (int) (mSourceHeight - srcRect.left));
            case 180:
                return new Rect((int) (mSourceWidth - srcRect.right), (int) (mSourceHeight - srcRect.bottom),
                        (int) (mSourceWidth - srcRect.left), (int) (mSourceHeight - srcRect.top));
            case 270:
                return new Rect((int) (mSourceWidth - srcRect.bottom), (int) srcRect.left,
                        (int) (mSourceWidth - srcRect.top), (int) srcRect.right);
            default:
                return new Rect((int) srcRect.left, (int) srcRect.top, (int) srcRect.right, (int) srcRect.bottom);
        }
    }
}
