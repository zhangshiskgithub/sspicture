package com.cpx.sspicture;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.cpx.sspicture.bean.ImageItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * desc: 选择图片事件分发activity<br>
 * author by zsq <br>
 * create on 2018/8/2 09:53<br>
 */

public class SelectPictureDispatchActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    /**
     * 设定最多能选择几张图片的intent key;
     */
    public static final String EXTRA_KEY_MAX_SELECT = "maxSelectNum";
    public static final String EXTRA_KEY_SELECT_TYPE = "selectType";
    /**
     * 开启activity时带数据的key,已选择图片列表
     */
    public static final String EXTRA_IMG_LIST = "imgList";
    /**
     * 拍照
     */
    public static final String ACTION_CAMERA = "ACTION_CAMERA";
    /**
     * 相册选择
     */
    public static final String ACTION_BUCKET = "ACTION_BUCKET";
    /**
     * 相册选择图片
     */
    public static final int BUCKET_TYPE_IMAGE = 0;
    /**
     * 相册选择视频
     */
    public static final int BUCKET_TYPE_VIDEO = 1;
    /**
     * 默认能够最多选取图片的张数
     */
    private static final int DEFAULT_MAX_SELECT_NUM = 1;

    /**
     * 外部存储请求权限code
     */
    private static final int READ_EXTERNAL_STORAGE_PERMISSIONS_REQUEST = 100;
    /**
     * 照相机权限请求
     */
    private static final int CAMERA_PERMISSIONS_REQUEST = 200;
    /**
     * 相机requestCode
     */
    private static final int CAMERA_REQUEST_CODE = 0;
    /**
     * 相册requestCode
     */
    private static final int BUCKET_REQUEST_CODE = 1;
    /**
     * 拍照文件的文件名
     */
    private Uri capturePicUri;
    private File cameraPicCacheDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            String cameraUri = savedInstanceState.getString("cameraUri");
            if (!TextUtils.isEmpty(cameraUri)) {
                capturePicUri = Uri.fromFile(new File(cameraUri));
            }
        }
        initCacheDir();
        locgic();
    }

    private void locgic() {
        String action = getIntent().getAction();
        if (TextUtils.isEmpty(action)) {
            finish();
            return;
        }
        if (action.equalsIgnoreCase(ACTION_BUCKET)) {
            if (checkStorePermission()) {
                selectBucketImage();
            }
        } else if (action.equalsIgnoreCase(ACTION_CAMERA) && capturePicUri == null) {
            if (checkCameraPermission()) {
                cameraPicture();
            }
        }
    }

    private void initCacheDir() {
        cameraPicCacheDir = CacheConfigure.getCameraPictureCacheDir(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (capturePicUri != null){
            outState.putString("cameraUri", capturePicUri.getPath());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSIONS_REQUEST) {
            selectBucketImage();
        } else if (requestCode == CAMERA_PERMISSIONS_REQUEST) {
            cameraPicture();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSIONS_REQUEST) {
            Toast.makeText(this, "无法读取相册,请您检查权限设置", Toast.LENGTH_SHORT).show();
            finish();
        } else if (requestCode == CAMERA_PERMISSIONS_REQUEST) {
            Toast.makeText(this, "无法访问相机,请您检查权限设置", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            List<String> checkedImageList = new ArrayList<>();
            switch (requestCode) {
                case CAMERA_REQUEST_CODE:
                    checkedImageList.addAll(getSelectedImageList());
                    checkedImageList.add(capturePicUri.getPath());
                    break;
                case BUCKET_REQUEST_CODE:
                    List<ImageItem> list = (List<ImageItem>) data.getSerializableExtra(SelectPictureActivityNew.EXTRA_IMG_LIST);
                    checkedImageList.addAll(processResult(list));
                    break;
            }
            Intent i = new Intent();
            i.putExtra(EXTRA_IMG_LIST, (Serializable) checkedImageList);
            setResult(RESULT_OK, i);
            this.finish();
        } else {
            //清空已生成的uri
            capturePicUri = null;
            finish();
        }
    }

    private List<String> processResult(List<ImageItem> list){
        List<String> result = new ArrayList<>();
        File selectPictureCacheDir = CacheConfigure.getSelectPictureBitmapCacheDir(this);
        ContentResolver contentResolver = getContentResolver();
        for (ImageItem imageItem : list) {
            String path = imageItem.path;
            if(path.startsWith("content")){
                try {
                    File file = new File(selectPictureCacheDir, imageItem.name);
                    InputStream inputStream = contentResolver.openInputStream(Uri.parse(path));
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    byte[] buffer = new byte[1024]; // 创建缓冲区
                    int length;

                    while ((length = inputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, length);
                    }
                    fileOutputStream.close();
                    inputStream.close();
                    result.add(file.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                result.add(path);
            }

        }
        return result;
    }
    /**
     * 检查照相权限
     *
     * @return
     */
    private boolean checkCameraPermission() {
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            return true;
        } else {
            EasyPermissions.requestPermissions(this, getResources().getString(R.string.request_camera_permission),
                    CAMERA_PERMISSIONS_REQUEST, perms);
            return false;
        }
    }

    /**
     * 检查存储权限
     *
     * @return
     */
    private boolean checkStorePermission() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            return true;
        } else {
            EasyPermissions.requestPermissions(this, getResources().getString(R.string.request_read_external_storage_permission),
                    READ_EXTERNAL_STORAGE_PERMISSIONS_REQUEST, perms);
            return false;
        }
    }

    /**
     * 获取最大能选择的照片数量
     *
     * @return
     */
    private int getMaxSelectNum() {
        return getIntent().getIntExtra(EXTRA_KEY_MAX_SELECT, DEFAULT_MAX_SELECT_NUM);
    }

    private ArrayList<String> getSelectedImageList() {
        return (ArrayList<String>) getIntent().getSerializableExtra(EXTRA_IMG_LIST);
    }

    /**
     * Author: zsq <br>
     * Date: 15/11/4 16:40<br>
     * 调用拍照
     */
    private void cameraPicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String name = DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance()) + ".jpg";
        File srcFile = new File(cameraPicCacheDir, name);
        capturePicUri = Uri.fromFile(srcFile);
        Uri saveUri;
        //针对7.0以上系统的处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String fileprovider = getPackageName() + ".fileprovider";
            saveUri = FileProvider.getUriForFile(this, fileprovider, srcFile);    //第二个参数是manifest中定义的`authorities`
        } else {
            saveUri = capturePicUri;
        }
        intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, saveUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//这一步很重要。给目标应用一个临时的授权。
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    private void selectBucketImage(){
        SelectPictureActivityNew.startPage(this,getSelectedImageList(),getMaxSelectNum(),BUCKET_REQUEST_CODE,getBucketType());
    }
    private int getBucketType(){
        return getIntent().getIntExtra(EXTRA_KEY_SELECT_TYPE, BUCKET_TYPE_IMAGE);
    }
}
