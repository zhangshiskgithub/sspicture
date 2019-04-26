package com.cpx.sspicture;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * desc: 所有本地缓存路径的配置文件<br>
 * author by zsq <br>
 * create on 15/11/11 16:56<br>
 */
public class CacheConfigure {

    private static final String TAG = CacheConfigure.class.getSimpleName();
    /**
     * BASE64文件缓存目录
     */
    private static final String BASE64_CACHE_DIR = "/BASE64";
    /**
     * 上传图片前的压缩文件缓存目录
     */
    private static final String UPLOAD_COMPRESS_CACHE_DIR = "/CompressCache";
    /**
     * 相册bitmap处理后的缓存
     */
    public static final String SELECT_PICTURE_BITMAP_CACHE_DIR = "/ImageCache";
    /**
     * 拍照的图片存储的缓存
     */
    public static final String CAMERA_PICTURE_CACHE_DIR = "/CameraCache";
    /**
     * 裁剪图片缓存的目录
     */
    public static final String CROP_PICTURE_CACHE_DIR = "/CropCache";

    /**
     * author by zsq <br>
     * create on 15/11/11 17:04<br>
     * 获取上传图片前本地压缩文件的缓存目录
     *
     * @param ctx
     * @return 缓存目录
     */
    public static File getCompressCacheDir(Context ctx) {
        File dir;
        try {
            DebugLog.d("TTT", "File----> ctx:: " + ctx);
            DebugLog.d("TTT", "File----> ctx.getExternalCacheDir()::  " + ctx.getExternalCacheDir());
            dir = new File(ctx.getExternalCacheDir() + UPLOAD_COMPRESS_CACHE_DIR);
            if (!dir.exists()) {
                DebugLog.d(TAG, "compressCacheDir no exists");
                dir.mkdirs();
            }
            return dir;
        } catch (Exception e) {
            DebugLog.d("TTT", "File----> Environment.getExternalStorageDirectory()::  " + Environment.getExternalStorageDirectory());
            dir = new File(Environment.getExternalStorageDirectory() + UPLOAD_COMPRESS_CACHE_DIR);
            if (!dir.exists()) {
                DebugLog.d(TAG, "compressCacheDir no exists");
                dir.mkdirs();
            }
            return dir;
        }
    }

    /**
     * author by zsq <br>
     * create on 15/11/11 17:04<br>
     * 获取base64缓存文件的缓存目录
     *
     * @param ctx
     * @return 缓存目录
     */
    public static File getBase64CacheDir(Context ctx) {
        File dir;
        try {
            DebugLog.d("TTT", "File----> ctx:: " + ctx);
            DebugLog.d("TTT", "File----> ctx.getExternalCacheDir()::  " + ctx.getExternalCacheDir());
            dir = new File(ctx.getExternalCacheDir() + BASE64_CACHE_DIR);
            if (!dir.exists()) {
                DebugLog.d(TAG, "base64cache dir no exists");
                dir.mkdirs();
            }
            return dir;
        } catch (Exception e) {
            DebugLog.d("TTT", "File----> Environment.getExternalStorageDirectory()::  " + Environment.getExternalStorageDirectory());
            dir = new File(Environment.getExternalStorageDirectory() + BASE64_CACHE_DIR);
            if (!dir.exists()) {
                DebugLog.d(TAG, "base64cache dir no exists");
                dir.mkdirs();
            }
            return dir;
        }
    }

    /**
     * author by zsq <br>
     * create on 15/11/11 17:04<br>
     * 获取选择图片显示时bitmap的缓存目录
     *
     * @param ctx
     * @return 缓存目录
     */
    public static File getSelectPictureBitmapCacheDir(Context ctx) {

        File dir;
        try {
            DebugLog.d("TTT", "File----> ctx:: " + ctx);
            DebugLog.d("TTT", "File----> ctx.getExternalCacheDir()::  " + ctx.getExternalCacheDir());
            dir = new File(ctx.getExternalCacheDir() + SELECT_PICTURE_BITMAP_CACHE_DIR);
            if (!dir.exists()) {
                DebugLog.d(TAG, "compressCacheDir no exists");
                dir.mkdirs();
            }
            return dir;
        } catch (Exception e) {
            DebugLog.d("TTT", "File----> Environment.getExternalStorageDirectory()::  " + Environment.getExternalStorageDirectory());
            dir = new File(Environment.getExternalStorageDirectory() + SELECT_PICTURE_BITMAP_CACHE_DIR);
            if (!dir.exists()) {
                DebugLog.d(TAG, "selectPictureBitmapCacheDir no exists");
                dir.mkdirs();
            }
            return dir;
        }
    }

    /**
     * author by zsq <br>
     * create on 15/11/11 17:04<br>
     * 获取拍摄图片时图片的缓存目录
     *
     * @param ctx
     * @return 缓存目录
     */
    public static File getCameraPictureCacheDir(Context ctx) {

        File dir;
        try {
            DebugLog.d("TTT", "File----> ctx:: " + ctx);
            DebugLog.d("TTT", "File----> ctx.getExternalCacheDir()::  " + ctx.getExternalCacheDir());
            dir = new File(ctx.getExternalCacheDir() + CAMERA_PICTURE_CACHE_DIR);
            if (!dir.exists()) {
                DebugLog.d(TAG, "compressCacheDir no exists");
                dir.mkdirs();
            }
            return dir;
        } catch (Exception e) {
            DebugLog.d("TTT", "File----> Environment.getExternalStorageDirectory()::  " + Environment.getExternalStorageDirectory());
            dir = new File(Environment.getExternalStorageDirectory() + CAMERA_PICTURE_CACHE_DIR);
            if (!dir.exists()) {
                DebugLog.d(TAG, "cameraPictureCacheDir no exists");
                dir.mkdirs();
            }
            return dir;
        }
    }

    /**
     * author by zsq <br>
     * create on 15/11/11 17:04<br>
     * 获取裁剪图片时图片的缓存目录
     *
     * @param ctx
     * @return 缓存目录
     */
    public static File getCropPictureCacheDir(Context ctx) {

        File dir;
        try {
            DebugLog.d("TTT", "File----> ctx:: " + ctx);
            DebugLog.d("TTT", "File----> ctx.getExternalCacheDir()::  " + ctx.getExternalCacheDir());
            dir = new File(ctx.getExternalCacheDir() + CROP_PICTURE_CACHE_DIR);
            if (!dir.exists()) {
                DebugLog.d(TAG, "cropCacheDir no exists");
                dir.mkdirs();
            }
            return dir;
        } catch (Exception e) {
            DebugLog.d("TTT", "File----> Environment.getExternalStorageDirectory()::  " + Environment.getExternalStorageDirectory());
            dir = new File(Environment.getExternalStorageDirectory() + CROP_PICTURE_CACHE_DIR);
            if (!dir.exists()) {
                DebugLog.d(TAG, "cropCacheDir no exists");
                dir.mkdirs();
            }
            return dir;
        }
    }

}
