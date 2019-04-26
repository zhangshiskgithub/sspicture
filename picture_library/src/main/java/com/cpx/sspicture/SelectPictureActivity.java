package com.cpx.sspicture;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.ListPopupWindow;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cpx.sspicture.bean.ImageBucket;
import com.cpx.sspicture.utils.ImageAlbumHelper;
import com.cpx.sspicture.utils.SelectPictureConfig;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * desc: 选取/拍摄图片的activity <br>
 * 开启时需要通过Intent传入已选择的图片列表及最多能选取几张图片,否则默认最多能选择一张图片<br>
 * 已选的图片通过onActivityResult()方法回传<br/>
 * List<String> list = (List<String>) data.getSerializableExtra(SelectPictureActivity.EXTRA_IMG_LIST);<br/>
 * <p/>
 * author: zsq <br>
 * date: 15/11/4 <br>
 */
@Deprecated
public class SelectPictureActivity extends FragmentActivity implements View.OnClickListener, AdapterView.OnItemClickListener, EasyPermissions.PermissionCallbacks {
    /**
     * 设定最多能选择几张图片的intent key;
     */
    public static final String EXTRA_KEY_MAX_SELECT = "maxSelectNum";
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
     * 默认能够最多选取图片的张数
     */
    private static final int DEFAULT_MAX_SELECT_NUM = 1;
    /**
     * gradview 列数
     */
    private static final int COLUM_NUM = 3;
    /**
     * 请求权限code
     */
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 200;
    /**
     * 通过相机选取图片的item的url
     */
//    private static final String CAMERA_ACTION_URL = "CAMERA_ACTION_URL";
    /**
     * 已选择图片列表
     */
    private List<String> checkedImageList;
    /**
     * 手机所有相册列表
     */
    private List<ImageBucket> imagesBucketList;
    /**
     * 最多能选择几张照片
     */
    private int maxPicNum = 1;
    /**
     * 返回
     */
    private LinearLayout ll_select_pic_title_left;
    /**
     * 选取
     */
    private LinearLayout ll_select_pic_title_right;
    /**
     * 确定按钮
     */
    private TextView tv_title_right;
    /**
     * 图片内容
     */
    private GridView gv_select_pic;
    private Context mContext;
    /**
     * 图片grad的适配器
     */
    private SelectPicAdapter selectPicAdapter;
    /**
     * 相册帮助类
     */
    private ImageAlbumHelper helper;
    /**
     * 拍照图片的储存文件夹
     */
    private File cameraPicCacheDir;
    /**
     * 拍照文件的文件名
     */
    private Uri capturePicUri;
    private mCheckedListener checkedListener;
    /**
     * 当前显示的相册对象
     */
    private ImageBucket currentBucket;
    private ListPopupWindow folderPopupWindow;
    private FolderAdapter folderAdapter;
    private View popupAnchorView;
    private Button category_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            String cameraUri = savedInstanceState.getString("cameraUri");
            if (!TextUtils.isEmpty(cameraUri)) {
                capturePicUri = Uri.fromFile(new File(cameraUri));
            }
        }
        initLayout();
        findViewById();
        setListener();
        locgic();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (capturePicUri != null)
            outState.putString("cameraUri", capturePicUri.getPath());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        bc.clear();
//        bc = null;
        helper.destory();
        imagesBucketList = null;
    }

    /**
     * Author: zsq <br>
     * Date: 15/11/4 13:17<br>
     * 查找控件
     */
    private void findViewById() {
        initCacheDir();

        ll_select_pic_title_left = (LinearLayout) findViewById(R.id.ll_select_pic_title_left);
        ll_select_pic_title_right = (LinearLayout) findViewById(R.id.ll_select_pic_title_right);
        tv_title_right = (TextView) findViewById(R.id.tv_title_right);
        tv_title_right.setText(getTitleRightText());
        gv_select_pic = (GridView) findViewById(R.id.gv_select_pic);
        popupAnchorView = findViewById(R.id.ll_foot_view);
        category_button = (Button) findViewById(R.id.btn_category_button);
        selectPicAdapter = new SelectPicAdapter();
        folderAdapter = new FolderAdapter();
        checkedListener = new mCheckedListener();
        helper = ImageAlbumHelper.getHelper();
        helper.init(mContext);
        if (checkPermission()) {
            initBuck();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * 检查权限
     *
     * @return
     */
    private boolean checkPermission() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            return true;
        } else {
            EasyPermissions.requestPermissions(this, getResources().getString(R.string.request_read_external_storage_permission),
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE, perms);
            return false;
        }
    }

    /**
     * Author: zsq <br>
     * Date: 15/11/5 16:52<br>
     * 初始化所有图片bucket
     */
    private void initBuck() {
        imagesBucketList = helper.getImagesBucketList(true);
        folderAdapter.setData(imagesBucketList);
        for (ImageBucket bucket : imagesBucketList) {
            if (bucket.bucketId.equalsIgnoreCase(ImageAlbumHelper.ALL_BUCKET_LIST_ID)) {
                currentBucket = bucket;
//                currentBucket.imageList.add(0, CAMERA_ACTION_URL);
            }
        }
    }

    /**
     * Author: zsq <br>
     * Date: 15/11/4 16:38<br>
     * 初始化拍照缓存文件夹
     */
    private void initCacheDir() {
        cameraPicCacheDir = CacheConfigure.getCameraPictureCacheDir(this);
    }


    /**
     * Author: zsq <br>
     * Date: 15/11/4 13:17<br>
     * 初始化布局文件
     */
    private void initLayout() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// 锁定竖屏
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 没有标题栏
        setContentView(R.layout.activity_select_picture);
        Intent intent = getIntent();
        maxPicNum = intent.getIntExtra(EXTRA_KEY_MAX_SELECT, DEFAULT_MAX_SELECT_NUM);
        checkedImageList = (List<String>) getIntent().getSerializableExtra(EXTRA_IMG_LIST);
        if (checkedImageList == null) {
            checkedImageList = new ArrayList<String>();
        }
        mContext = this;
    }

    /**
     * Author: zsq <br>
     * Date: 15/11/4 13:18<br>
     * 设置监听
     */
    private void setListener() {
        gv_select_pic.setAdapter(selectPicAdapter);
        gv_select_pic.setOnItemClickListener(this);
        ll_select_pic_title_left.setOnClickListener(this);
        ll_select_pic_title_right.setOnClickListener(this);
        category_button.setOnClickListener(this);
    }

    /**
     * Author: zsq <br>
     * Date: 15/11/4 13:18<br>
     * 页面逻辑
     */
    private void locgic() {
        DebugLog.d("locgic");
        String action = getIntent().getAction();
        if(!TextUtils.isEmpty(action) && ACTION_CAMERA.equalsIgnoreCase(action) && capturePicUri == null){
            if (checkPermission()) {
                cameraPicture();
            }
        }
    }

    /**
     * 判断已选择图片是否包含ImageItem
     * Author: zsq <br>
     * Date: 15/11/4 15:07<br>
     *
     * @param imageItem
     * @return true包含, false不包含
     */
    private boolean containsImage(String imageItem) {
        for (String path : checkedImageList) {
            if (path.equals(imageItem)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从已选取图片列表中删除Image
     *
     * @param imageItem
     */
    private void removeImage(String imageItem) {
        String temp = null;
        for (String ii : checkedImageList) {
            if (ii.equals(imageItem)) {
                temp = ii;
            }
        }
        if (temp != null)
            checkedImageList.remove(temp);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ll_select_pic_title_left) {//取消
            finish();

        } else if (i == R.id.ll_select_pic_title_right) {//选取
            if (checkedImageList.size() > 0) {
                setResultAndFinish();
            } else {
                //showToast("您还没有选择图片!");
            }

        } else if (i == R.id.btn_category_button) {
            if (folderPopupWindow == null) {
                createPopupFolderList(getMobileWidth(), getMobileHeight());
            }

            if (folderPopupWindow.isShowing()) {
                folderPopupWindow.dismiss();
            } else {
                folderPopupWindow.show();
            }

        }
    }

    /**
     * 设置activity返回结果并关闭页面
     */
    private void setResultAndFinish() {
        Intent i = new Intent();
        i.putExtra(EXTRA_IMG_LIST, (Serializable) checkedImageList);
        setResult(RESULT_OK, i);
        this.finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String imageItem = currentBucket.imageList.get(position);
//        if (imageItem.equalsIgnoreCase(CAMERA_ACTION_URL)) {
//            if (checkedImageList.size() <= (maxPicNum - 1)) {
//                cameraPicture();
//            } else {
//                Toast.makeText(mContext, "最多只能选择" + maxPicNum + "张图片!", Toast.LENGTH_SHORT).show();
//            }
//            return;
//        }
        List<String> display = new ArrayList<>();
        display.add(imageItem);
        Intent intent = new Intent(this, DisplayPictureActivity.class);
        intent.putExtra(DisplayPictureActivity.EXTRA_IMG_LIST, (Serializable) display);
        startActivity(intent);
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
            String fileprovider = mContext.getPackageName() + ".fileprovider";
            saveUri = FileProvider.getUriForFile(this, fileprovider, srcFile);    //第二个参数是manifest中定义的`authorities`
        } else {
            saveUri = capturePicUri;
        }
        intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, saveUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//这一步很重要。给目标应用一个临时的授权。
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            checkedImageList.add(capturePicUri.getPath());
            setResultAndFinish();
        } else {
            //清空已生成的uri
            capturePicUri = null;
            String action = getIntent().getAction();
            if(!TextUtils.isEmpty(action) &&ACTION_CAMERA.equalsIgnoreCase(action)){
                finish();
            }
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        initBuck();
        selectPicAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Toast.makeText(this, "无法读取相册,请您检查权限设置",Toast.LENGTH_SHORT).show();
    }


    private class SelectPicAdapter extends BaseAdapter {

        protected final String TAG = SelectPicAdapter.class.getSimpleName();

        @Override
        public int getCount() {
            return currentBucket == null ? 0 : currentBucket.imageList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int width = parent.getWidth() / COLUM_NUM;
            String imageItem = currentBucket.imageList.get(position);
//            if (imageItem.equalsIgnoreCase(CAMERA_ACTION_URL)) {
//                View viewCamera = LayoutInflater.from(mContext).inflate(R.layout.activity_select_picture_camera_item, null);
//                viewCamera.setLayoutParams(new AbsListView.LayoutParams(width, width));
////                ImageView iv = (ImageView) viewCamera.findViewById(R.id.iv_select_pic_img);
////                iv.setImageResource(R.drawable.select_pic_default);
//                return viewCamera;
//            } else {
                ViewHolder holder;
                RelativeLayout view;
                if (convertView != null && convertView instanceof RelativeLayout) {
                    view = (RelativeLayout) convertView;
                    holder = (ViewHolder) view.getTag();
                } else {
                    view = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.activity_select_picture_item, null);
                    holder = new ViewHolder();
                    holder.iv_select_pic_img = (ImageView) view.findViewById(R.id.iv_select_pic_img);
                    holder.cb_select_pic = (CheckBox) view.findViewById(R.id.cb_select_pic);
                    view.setTag(holder);
                }
                view.setLayoutParams(new AbsListView.LayoutParams(width, width));
                SelectPictureConfig.imageLoader.displayImage(mContext, imageItem, holder.iv_select_pic_img);
//                bc.displayBmp(holder.iv_select_pic_img, imageItem, inScroll);
                // 设置checkbox的状态
                holder.cb_select_pic.setTag(position);
                holder.cb_select_pic.setOnCheckedChangeListener(checkedListener);
                if (containsImage(imageItem)) {
                    holder.cb_select_pic.setChecked(true);
                } else {
                    holder.cb_select_pic.setChecked(false);
                }
                return view;
//            }
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        class ViewHolder {
            /**
             * 图片内容
             */
            ImageView iv_select_pic_img;
            /**
             * 选择框
             */
            CheckBox cb_select_pic;

        }
    }

    /**
     * 获取屏幕宽度(px)
     *
     * @return 屏幕宽度, 像素
     */
    public int getMobileWidth() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        return width;
    }

    /**
     * 获取屏幕高度(px)
     *
     * @return 屏幕宽度, 像素
     */
    public int getMobileHeight() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.heightPixels;
        return width;
    }

    private class mCheckedListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Integer position = (Integer) buttonView.getTag();
            if (isChecked) {
                if (!containsImage(currentBucket.imageList.get(position))) {
                    if (checkedImageList.size() < maxPicNum ) {
                        checkedImageList.add(currentBucket.imageList.get(position));
                    } else {
                        Toast.makeText(mContext, "最多只能选择" + maxPicNum + "张图片!", Toast.LENGTH_SHORT).show();
                        buttonView.setChecked(false);
                    }
                }
            } else {
                removeImage(currentBucket.imageList.get(position));
            }
            tv_title_right.setText(getTitleRightText());
//            tv_select_pic_bottom_can_select_num.setText((maxPicNum - checkedImageList.size()) + "");
        }
    }

    /**
     * 获取已选择的图片个数
     *
     * @return
     */
    private int getSelectPictureCount() {
        return checkedImageList.size();
    }

    /**
     * 获取标题栏右侧文字
     *
     * @return
     */
    private String getTitleRightText() {
        int selectPictureCount = getSelectPictureCount();
        if (selectPictureCount == 0) {
            return "确定";
        }
        return "确定(" + selectPictureCount + "/" + maxPicNum + ")";
    }

    private void createPopupFolderList(int width, int height) {
        folderPopupWindow = new ListPopupWindow(this);
        folderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        folderPopupWindow.setAdapter(folderAdapter);
        folderPopupWindow.setContentWidth(width);
        folderPopupWindow.setWidth(width);
        folderPopupWindow.setHeight(height * 5 / 8);
        folderPopupWindow.setAnchorView(popupAnchorView);
        folderPopupWindow.setModal(true);
        folderPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageBucket imageBucket = imagesBucketList.get(position);
                if (imageBucket.bucketId.equalsIgnoreCase(folderAdapter.getLastSelectBucketId())) {
                    return;
                }
                currentBucket = imageBucket;
                selectPicAdapter.notifyDataSetChanged();
                folderAdapter.setLastSelectBucketId(imageBucket.bucketId);
                category_button.setText(imageBucket.bucketName);
                folderPopupWindow.dismiss();
                gv_select_pic.smoothScrollToPosition(0);
            }
        });
    }
}
