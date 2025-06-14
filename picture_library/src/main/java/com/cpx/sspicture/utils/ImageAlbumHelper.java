package com.cpx.sspicture.utils;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;

import com.cpx.sspicture.SelectPictureDispatchActivity;
import com.cpx.sspicture.bean.ImageBucket;
import com.cpx.sspicture.bean.ImageItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * 相册专辑帮助类
 *
 * @author Administrator
 */
public class ImageAlbumHelper {
    final String TAG = getClass().getSimpleName();
    Context context;
    ContentResolver cr;
    /**
     * Author: zsq <br>
     * Date: 15/11/5 10:30<br>
     * 相册列表
     */
    HashMap<String, ImageBucket> bucketList = new HashMap<String, ImageBucket>();
    /**
     * 手机中所有图片
     */
    List<ImageItem> imageList = new ArrayList<>();
    /**
     * 全部图片相册的bucketID
     */
    public static final String ALL_BUCKET_LIST_ID = "allImg";
    /**
     * 全部图片相册的bucketID
     */
    public static final String ALL_BUCKET_LIST_NAME = "全部";

    private int type = SelectPictureDispatchActivity.BUCKET_TYPE_IMAGE;


    private static ImageAlbumHelper instance;

    private ImageAlbumHelper() {
    }

    public static ImageAlbumHelper getHelper() {
        if (instance == null) {
            instance = new ImageAlbumHelper();
        }
        return instance;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context,int type) {
        if (this.context == null) {
            this.context = context;
            cr = context.getContentResolver();
        }
        this.type = type;
    }

    public void destory(){
        this.context = null;
    }


    /**
     * 是否创建了图片集
     */
    boolean hasBuildImagesBucketList = false;

    /**
     * 得到图片集
     */
    void buildImagesBucketList() {
        long startTime = System.currentTimeMillis();
        // 构造相册索引
        String columns[] = new String[]{Media._ID, Media.BUCKET_ID, Media.PICASA_ID, Media.DATA, Media.DISPLAY_NAME, Media.TITLE, Media.SIZE,
                Media.BUCKET_DISPLAY_NAME};
        // 得到一个游标
        Cursor cur = cr.query(Media.EXTERNAL_CONTENT_URI, columns, null, null, Media.DATE_ADDED + " desc");
        if (cur.moveToFirst()) {
            // 获取指定列的索引
            int photoIDIndex = cur.getColumnIndexOrThrow(Media._ID);
            int photoPathIndex = cur.getColumnIndexOrThrow(Media.DATA);
            int photoNameIndex = cur.getColumnIndexOrThrow(Media.DISPLAY_NAME);
            int photoTitleIndex = cur.getColumnIndexOrThrow(Media.TITLE);
            int photoSizeIndex = cur.getColumnIndexOrThrow(Media.SIZE);
            int bucketDisplayNameIndex = cur.getColumnIndexOrThrow(Media.BUCKET_DISPLAY_NAME);
            int bucketIdIndex = cur.getColumnIndexOrThrow(Media.BUCKET_ID);
            int picasaIdIndex = cur.getColumnIndexOrThrow(Media.PICASA_ID);
            // 获取图片总数
            int totalNum = cur.getCount();

            do {
                //遍历所有图片,添加入相应的bucket中
                long _id = cur.getLong(photoIDIndex);
                String name = cur.getString(photoNameIndex);
                String path = cur.getString(photoPathIndex);
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                    path = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, _id).toString();
                }
                String title = cur.getString(photoTitleIndex);
                String size = cur.getString(photoSizeIndex);
                String bucketName = cur.getString(bucketDisplayNameIndex);
                String bucketId = cur.getString(bucketIdIndex);
                String picasaId = cur.getString(picasaIdIndex);

//				Log.i(TAG, _id + ", bucketId: " + bucketId + ", picasaId: " + picasaId + " name:" + name + " path:" + path + " title: " + title + " size: "
//						+ size + " bucket: " + bucketName + "---");

                //如果配置合并相同名称相册,则用bucketName做key,否则用id做key

                ImageBucket bucket = SelectPictureConfig.MERGE_SAME_NAME_BUCKET ? bucketList.get(bucketName) : bucketList.get(bucketId);
                if (bucket == null) {
                    bucket = new ImageBucket();
                    bucketList.put(SelectPictureConfig.MERGE_SAME_NAME_BUCKET ? bucketName : bucketId, bucket);
                    bucket.imageList = new ArrayList<ImageItem>();
                    bucket.bucketName = bucketName;
                    bucket.bucketId = bucketId;
                }
                bucket.count++;
                /**
                 * 向相册中添加
                 */
                bucket.imageList.add(new ImageItem(name,path,path,ImageItem.TYPE_IMAGE));
                imageList.add(new ImageItem(name,path,path,ImageItem.TYPE_IMAGE));
            } while (cur.moveToNext());
        }

        Iterator<Entry<String, ImageBucket>> itr = bucketList.entrySet().iterator();
//        Log.d(TAG, bucketList.size() + "******");
        while (itr.hasNext()) {
            Entry<String, ImageBucket> entry = itr.next();
            ImageBucket bucket = entry.getValue();
//            Log.d(TAG, entry.getKey() + ", " + bucket.bucketName + ", " + bucket.count + " ---------- ");
        }
        hasBuildImagesBucketList = true;
        long endTime = System.currentTimeMillis();
//        Log.d(TAG, "use time: " + (endTime - startTime) + " ms");
    }
    /**
     * 得到图片集
     */
    void buildVideosBucketList() {
        // 构造相册索引
        String columns[] = new String[]{ MediaStore.Video.Media._ID,MediaStore.Video.Media.DATA,MediaStore.Video.Media.SIZE,MediaStore.Video.Media.BUCKET_DISPLAY_NAME,MediaStore.Video.Media.BUCKET_ID};
        // 得到一个游标
        Cursor cur = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns, null, null, Media.DATE_ADDED + " desc");
        if (cur.moveToFirst()) {
            // 获取指定列的索引
            // 获取图片总数
            int totalNum = cur.getCount();

            do {
                //遍历所有图片,添加入相应的bucket中
                int videoId = cur.getInt(cur.getColumnIndex(MediaStore.Video.Media._ID));
                String name = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                String path = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                    path = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoId).toString();
                }
                String size = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                String bucketName = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                String bucketId = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID));

                //如果配置合并相同名称相册,则用bucketName做key,否则用id做key

                ImageBucket bucket = SelectPictureConfig.MERGE_SAME_NAME_BUCKET ? bucketList.get(bucketName) : bucketList.get(bucketId);
                if (bucket == null) {
                    bucket = new ImageBucket();
                    bucketList.put(SelectPictureConfig.MERGE_SAME_NAME_BUCKET ? bucketName : bucketId, bucket);
                    bucket.imageList = new ArrayList<ImageItem>();
                    bucket.bucketName = bucketName;
                    bucket.bucketId = bucketId;
                }
                bucket.count++;
                /**
                 * 向相册中添加
                 */
                bucket.imageList.add(new ImageItem(name,path,path,ImageItem.TYPE_VIDEO));
                imageList.add(new ImageItem(name,path,path,ImageItem.TYPE_VIDEO));
            } while (cur.moveToNext());
        }
        hasBuildImagesBucketList = true;
    }

    /**
     * 得到图片集
     *
     * @param refresh 是否刷新数据
     * @return
     */
    public List<ImageBucket> getImagesBucketList(boolean refresh) {
        if (refresh || (!refresh && !hasBuildImagesBucketList)) {
            //清空所有图片数据
            imageList.clear();
            bucketList.clear();
            if(type == SelectPictureDispatchActivity.BUCKET_TYPE_VIDEO){
                buildVideosBucketList();
            }else {
                buildImagesBucketList();
            }
        }
        //添加所有
        ImageBucket ib = new ImageBucket();
        ib.bucketId = ALL_BUCKET_LIST_ID;
        ib.bucketName = ALL_BUCKET_LIST_NAME;
        ib.count = imageList.size();
        ib.imageList = imageList;
        bucketList.put(SelectPictureConfig.MERGE_SAME_NAME_BUCKET ? ALL_BUCKET_LIST_NAME : ALL_BUCKET_LIST_ID, ib);
        List<ImageBucket> tmpList = new ArrayList<ImageBucket>();
        Iterator<Entry<String, ImageBucket>> itr = bucketList.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<String, ImageBucket> entry = itr.next();
            tmpList.add(entry.getValue());
        }
        return tmpList;
    }

    /**
     * Author: zsq <br>
     * Date: 15/11/5 10:32<br>
     *
     * @param refresh 是否刷新数据
     * @return 手机中所有的图片
     */
    public List<ImageItem> getAllImageItem(boolean refresh) {
        if (refresh || (!refresh && !hasBuildImagesBucketList)) {
            //清空所有图片数据
            imageList.clear();
            bucketList.clear();
            buildImagesBucketList();
        }

        return imageList;
    }

}
