package com.cpx.sspicture;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.cpx.sspicture.utils.SelectPictureConfig;

import java.util.List;

import uk.co.senab.photoview.IPhotoView;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * desc: 查看图片的页面<br>开启时需要传入查看图片的列表及从第几张开始查看,默认从第一张查看<br/>
 * 注意:如果传入的图片list为空,会直接关闭页面<br/>
 * List<String> list = new Array<String>();<br/>
 * Intent intent = new Intent(this,DisplayPictureActivity.class);<br/>
 * intent.putExtra(DisplayPictureActivity.EXTRA_IMG_LIST, (Serializable) list)<br/>
 * intent.putExtra(DisplayPictureActivity.EXTRA_DISPLAY_INDEX, 1);<br/>
 * author: zsq <br>
 * date: 15/11/4 <br>
 */
public class DisplayPictureActivity extends Activity implements ViewPager.OnPageChangeListener {
    /**
     * 开启activity时带数据的key,已选择图片列表
     */
    public static final String EXTRA_IMG_LIST = "imgList";
    /**
     * 开启activity时带数据的key,从第几张开始查看
     */
    public static final String EXTRA_DISPLAY_INDEX = "displayIndex";
    /**
     * 需要查看的图片下标
     */
    public int displayIndex = 0;
    /**
     * 加载图片时现实的图片
     */
    private static int DEFAULT_IMG = R.mipmap.select_pic_default;
    /**
     * 图片的集合
     */
    private List<String> imageList;
    private Context mContext;
    private HackyViewPager vp_display_picture;
    private SamplePagerAdapter pagerAdapter;
    /**
     * 屏幕高度
     */
    private int screenHeight;
    /**
     * 屏幕宽度
     */
    private int screenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(getScreenOrientation());// 锁定竖屏
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 没有标题栏
        Intent intent = getIntent();
        displayIndex = intent.getIntExtra(EXTRA_DISPLAY_INDEX, 0);
        imageList = (List<String>) getIntent().getSerializableExtra(EXTRA_IMG_LIST);
        if (imageList == null || imageList.size() == 0) {
            finish();
            return;
        }
        if (displayIndex > (imageList.size() - 1)) {
            //如果需要查看的下标超过了图片总数,默认从第一张开始查看
            displayIndex = 0;
        }
        initLayout();
        findViewById();
        setListener();
        locgic();
    }

    protected int getScreenOrientation(){
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }


    /**
     * Author: zsq <br>
     * Date: 15/11/4 17:47<br>
     * 查找控件
     */
    protected void findViewById() {
        vp_display_picture = (HackyViewPager) findViewById(R.id.vp_display_picture);
        pagerAdapter = new SamplePagerAdapter();
        findViewById(R.id.ll_display_pic_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Author: zsq <br>
     * Date: 15/11/4 17:47<br>
     * 初始化布局文件
     */
    protected void initLayout() {
        mContext = this;
        setContentView(R.layout.activity_display_picture);
        screenHeight = getMobileHeight();
        screenWidth = getMobileWidth();
    }

    /**
     * Author: zsq <br>
     * Date: 15/11/4 17:47<br>
     * 设置监听
     */
    protected void setListener() {
        vp_display_picture.setAdapter(pagerAdapter);
        vp_display_picture.addOnPageChangeListener(this);
    }

    /**
     * Author: zsq <br>
     * Date: 15/11/4 17:47<br>
     * 页面逻辑
     */
    protected void locgic() {
        //跳转到指定的页面开始查看
        vp_display_picture.setCurrentItem(displayIndex);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        PhotoView photoView = (PhotoView) vp_display_picture.getChildAt(position);
        if (photoView != null)
            photoView.setScale(IPhotoView.DEFAULT_MIN_SCALE);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

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
     * 获取屏幕高度度(px)
     *
     * @return 屏幕宽度, 像素
     */
    public int getMobileHeight() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        return height;
    }

    class SamplePagerAdapter extends PagerAdapter {


        @Override
        public int getCount() {
            return imageList.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());
            String path = imageList.get(position);
            //fixed 图片过长的情况下无法放大看清
            SelectPictureConfig.imageLoader.displayBigImage(mContext, path, photoView, new BigImageLoadCallback() {
                @Override
                public void onLoad(View view, Bitmap bitmap) {
                    int height = bitmap.getHeight();
                    int width = bitmap.getWidth();
                    float maxScroll;
                    float medScroll;
                    if (height > width) {
                        //图片是竖向长图,以宽度的缩放为准
                        maxScroll = screenWidth / width;
                        medScroll = screenWidth / width / 2;
                    } else {
                        //图片是横向长图,以高度的缩放为准
                        maxScroll = screenHeight / height;
                        medScroll = screenHeight / height / 2;
                    }
                    if (maxScroll < IPhotoView.DEFAULT_MAX_SCALE) {
                        maxScroll = IPhotoView.DEFAULT_MAX_SCALE;
                    }
                    if (medScroll < IPhotoView.DEFAULT_MID_SCALE) {
                        medScroll = IPhotoView.DEFAULT_MID_SCALE;
                    }
                    PhotoView v = (PhotoView) view;
                    v.setScaleLevels(IPhotoView.DEFAULT_MIN_SCALE, medScroll, maxScroll);
                }
            });
            photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    finish();
                }
            });
            // Now just add PhotoView to ViewPager and return it
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }
}
