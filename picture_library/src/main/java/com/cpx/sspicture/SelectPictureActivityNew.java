package com.cpx.sspicture;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cpx.sspicture.bean.ImageBucket;
import com.cpx.sspicture.utils.ImageAlbumHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * desc: 选取图片的activity <br>
 * 开启时需要通过Intent传入已选择的图片列表及最多能选取几张图片,否则默认最多能选择一张图片<br>
 * 已选的图片通过onActivityResult()方法回传<br/>
 * List<String> list = (List<String>) data.getSerializableExtra(SelectPictureActivity.EXTRA_IMG_LIST);<br/>
 * <p/>
 * author: zsq <br>
 * date: 15/11/4 <br>
 */
public class SelectPictureActivityNew extends FragmentActivity implements View.OnClickListener, SelectPictureImageAdapter.OnItemCheckStatusChangedListener {
    /**
     * 设定最多能选择几张图片的intent key;
     */
    public static final String EXTRA_KEY_MAX_SELECT = "maxSelectNum";
    public static final String EXTRA_KEY_TYPE = "type";
    /**
     * 开启activity时带数据的key,已选择图片列表
     */
    public static final String EXTRA_IMG_LIST = "imgList";
    /**
     * 默认能够最多选取图片的张数
     */
    private static final int DEFAULT_MAX_SELECT_NUM = 1;
    /**
     * 手机所有相册列表
     */
    private List<ImageBucket> imagesBucketList;
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
    private RecyclerView rv_select_pic;
    /**
     * 相册帮助类
     */
    private ImageAlbumHelper helper;
    /**
     * 当前显示的相册对象
     */
    private ImageBucket currentBucket;
    private ListPopupWindow folderPopupWindow;
    private FolderAdapter folderAdapter;
    private View popupAnchorView;
    private Button category_button;

    private SelectPictureImageAdapter mAdapter;

    /**
     * 开启页面
     * @param activity
     * @param selectImgs    已选中的图片
     * @param maxSelect     最大选中数
     * @param requestCode
     */
    public static void startPage(Activity activity,ArrayList<String> selectImgs,int maxSelect,int requestCode,int type){
        Intent intent = new Intent(activity,SelectPictureActivityNew.class);
        if(selectImgs != null) {
            intent.putStringArrayListExtra(EXTRA_IMG_LIST,selectImgs);
        }
        intent.putExtra(EXTRA_KEY_MAX_SELECT,maxSelect);
        intent.putExtra(EXTRA_KEY_TYPE,type);
        activity.startActivityForResult(intent,requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        findViewById();
        setListener();
        locgic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        helper.destory();
        imagesBucketList = null;
    }

    /**
     * Author: zsq <br>
     * Date: 15/11/4 13:17<br>
     * 查找控件
     */
    private void findViewById() {
        ll_select_pic_title_left = (LinearLayout) findViewById(R.id.ll_select_pic_title_left);
        ll_select_pic_title_right = (LinearLayout) findViewById(R.id.ll_select_pic_title_right);
        tv_title_right = (TextView) findViewById(R.id.tv_title_right);

        rv_select_pic = (RecyclerView) findViewById(R.id.rv_select_pic);
        GridLayoutManager layoutManager = new GridLayoutManager(this,3);
        rv_select_pic.setHasFixedSize(true);
        rv_select_pic.setLayoutManager(layoutManager);
        popupAnchorView = findViewById(R.id.ll_foot_view);
        category_button = (Button) findViewById(R.id.btn_category_button);
        mAdapter = new SelectPictureImageAdapter(this,getIntentMaxPicNum());
        mAdapter.setSelectList(getIntentCheckImageList());
        rv_select_pic.setAdapter(mAdapter);

        folderAdapter = new FolderAdapter();
        helper = ImageAlbumHelper.getHelper();
        helper.init(this,getIntentType());
        setTitleRightStr();
        initBuck();
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
            }
        }
        mAdapter.setImgList(currentBucket.imageList);
    }


    /**
     * Author: zsq <br>
     * Date: 15/11/4 13:17<br>
     * 初始化布局文件
     */
    private void initLayout() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// 锁定竖屏
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 没有标题栏
        setContentView(R.layout.activity_select_picture_new);
    }

    /**
     * 获取默认选中的图片
     * @return
     */
    private List<String> getIntentCheckImageList(){
        List<String> list = getIntent().getStringArrayListExtra(EXTRA_IMG_LIST);
        if(list == null){
            list = new ArrayList<>();
        }
        return list;
    }

    /**
     * 获取最大选则数
     * @return
     */
    private int getIntentMaxPicNum(){
        return getIntent().getIntExtra(EXTRA_KEY_MAX_SELECT, DEFAULT_MAX_SELECT_NUM);
    }

    private int getIntentType(){
        return getIntent().getIntExtra(EXTRA_KEY_TYPE, SelectPictureDispatchActivity.BUCKET_TYPE_IMAGE);
    }
    /**
     * Author: zsq <br>
     * Date: 15/11/4 13:18<br>
     * 设置监听
     */
    private void setListener() {
        ll_select_pic_title_left.setOnClickListener(this);
        ll_select_pic_title_right.setOnClickListener(this);
        category_button.setOnClickListener(this);
        mAdapter.setOnItemCheckStatusChangedListener(this);
    }

    /**
     * Author: zsq <br>
     * Date: 15/11/4 13:18<br>
     * 页面逻辑
     */
    private void locgic() {
        DebugLog.d("locgic");
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ll_select_pic_title_left) {//取消
            finish();

        } else if (i == R.id.ll_select_pic_title_right) {//选取
            if (mAdapter.getSelectList().size() > 0) {
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
        i.putExtra(EXTRA_IMG_LIST, (Serializable) mAdapter.getSelectList());
        setResult(RESULT_OK, i);
        this.finish();
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

    /**
     * 获取已选择的图片个数
     *
     * @return
     */
    private int getSelectPictureCount() {
        return mAdapter.getSelectList().size();
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
        return "确定(" + selectPictureCount + "/" + getIntentMaxPicNum() + ")";
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
                mAdapter.setImgList(currentBucket.imageList);
                folderAdapter.setLastSelectBucketId(imageBucket.bucketId);
                category_button.setText(imageBucket.bucketName);
                folderPopupWindow.dismiss();
            }
        });
    }

    @Override
    public void onCheckStatusChanged() {
        setTitleRightStr();
    }

    private void setTitleRightStr(){
        tv_title_right.setText(getTitleRightText());
    }
}
