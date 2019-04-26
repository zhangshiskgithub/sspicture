package com.cpx.sspicture;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.Toast;

import com.cpx.sspicture.utils.SelectPictureConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * desc: <br>
 * author by zsq <br>
 * create on 2018/8/2 10:32<br>
 */

public class SelectPictureImageAdapter extends RecyclerView.Adapter<SelectPictureImageAdapter.ItemViewHolder> {
    /**
     * 选中的图片
     */
    private Set<String> selectList = new HashSet<>();
    /**
     * 图片列表
     */
    private List<String> imgList = new ArrayList<>();
    /**
     * 最多选择几张
     */
    private int maxSelect = 1;
    private Context mContext;

    private OnItemCheckStatusChangedListener listener;
    private int imageWidth;

    public SelectPictureImageAdapter(Context context,int maxSelect) {
        this.mContext = context;
        this.maxSelect = maxSelect;
    }

    public List<String> getSelectList(){
        return new ArrayList<>(selectList);
    }
    /**
     * 设置选中的列表
     * @param list
     */
    public void setSelectList(List<String> list){
        if(list != null){
            selectList.addAll(list);
        }
    }
    /**
     * 设置数据
     * @param list
     */
    public void setImgList(List<String> list){
        imgList.clear();
        if(list != null){
            imgList.addAll(list);
        }
        notifyDataSetChanged();
    }
    @Override
    public SelectPictureImageAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(mContext).inflate(R.layout.activity_select_picture_item_new, null);
        if(imageWidth == 0) {
            imageWidth = parent.getMeasuredWidth() / 3;
        }
        view.setLayoutParams(new AbsListView.LayoutParams(imageWidth, imageWidth));
        ItemViewHolder itemViewHolder = new ItemViewHolder(view);
        itemViewHolder.iv_select_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                clickSelectStatus(position);
            }
        });
        itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String image = (String) v.getTag();
                List<String> display = new ArrayList<>();
                display.add(image);
                Intent intent = new Intent(mContext, DisplayPictureActivity.class);
                intent.putExtra(DisplayPictureActivity.EXTRA_IMG_LIST, (Serializable) display);
                mContext.startActivity(intent);
            }
        });
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(SelectPictureImageAdapter.ItemViewHolder holder, int position) {
        String image = imgList.get(position);
        if(selectList.contains(image)) {
            holder.iv_select_status.setImageResource(R.mipmap.image_grad_checkbox_select);
        }else {
            holder.iv_select_status.setImageResource(R.mipmap.image_grad_checkbox_normal);
        }
        holder.iv_select_status.setTag(position);
        holder.itemView.setTag(image);
        SelectPictureConfig.imageLoader.displayImage(mContext, image, holder.iv_select_pic_img);
    }

    @Override
    public int getItemCount() {
        return imgList.size();
    }

    public void setOnItemCheckStatusChangedListener(OnItemCheckStatusChangedListener listener){
        this.listener = listener;
    }
    public interface OnItemCheckStatusChangedListener{

        void onCheckStatusChanged();
    }

    private void clickSelectStatus(int position){
        String img = imgList.get(position);
        if(selectList.contains(img)){
            selectList.remove(img);
        }else {
            if(selectList.size() < maxSelect){
                selectList.add(img);
            }else {
                Toast.makeText(mContext, "最多只能选择" + maxSelect + "张图片!", Toast.LENGTH_SHORT).show();
            }
        }
        notifyItemChanged(position);
        if(listener != null){
            listener.onCheckStatusChanged();
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        /**
         * 图片内容
         */
        private ImageView iv_select_pic_img;
        /**
         * 选择框
         */
        private ImageView iv_select_status;

        public ItemViewHolder(View itemView) {
            super(itemView);
            iv_select_pic_img = (ImageView) itemView.findViewById(R.id.iv_select_pic_img);
            iv_select_status = (ImageView) itemView.findViewById(R.id.iv_select_status);
        }
    }
}
