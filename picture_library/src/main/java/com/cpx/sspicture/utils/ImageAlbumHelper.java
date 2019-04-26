package com.cpx.sspicture.utils;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Images.Media;

import com.cpx.sspicture.bean.ImageBucket;

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
    List<String> imageList = new ArrayList<>();
    /**
     * 全部图片相册的bucketID
     */
    public static final String ALL_BUCKET_LIST_ID = "allImg";
    /**
     * 全部图片相册的bucketID
     */
    public static final String ALL_BUCKET_LIST_NAME = "全部";


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
    public void init(Context context) {
        if (this.context == null) {
            this.context = context;
            cr = context.getContentResolver();
        }
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
                String _id = cur.getString(photoIDIndex);
                String name = cur.getString(photoNameIndex);
                String path = cur.getString(photoPathIndex);
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
                    bucket.imageList = new ArrayList<String>();
                    bucket.bucketName = bucketName;
                    bucket.bucketId = bucketId;
                }
                bucket.count++;
                /**
                 * 向相册中添加
                 */
                bucket.imageList.add(path);
                //向所有图片列表中添加
                imageList.add(path);
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
     *
     * @param refresh 是否刷新数据
     * @return
     */
    public List<ImageBucket> getImagesBucketList(boolean refresh) {
        if (refresh || (!refresh && !hasBuildImagesBucketList)) {
            //清空所有图片数据
            imageList.clear();
            bucketList.clear();
            buildImagesBucketList();
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
    public List<String> getAllImageItem(boolean refresh) {
        if (refresh || (!refresh && !hasBuildImagesBucketList)) {
            //清空所有图片数据
            imageList.clear();
            bucketList.clear();
            buildImagesBucketList();
        }

        return imageList;
    }

}
