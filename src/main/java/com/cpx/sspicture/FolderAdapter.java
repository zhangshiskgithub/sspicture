package com.cpx.sspicture;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cpx.sspicture.bean.ImageBucket;
import com.cpx.sspicture.utils.ImageAlbumHelper;
import com.cpx.sspicture.utils.SelectPictureConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * desc: 选择相册文件夹adapter<br>
 * author by zsq <br>
 * create on 16/5/8 16:30<br>
 */
public class FolderAdapter extends BaseAdapter {
    private String lastSelectBucketId = ImageAlbumHelper.ALL_BUCKET_LIST_ID;
    /**
     * 图片文件夹数据
     */
    private List<ImageBucket> data = new ArrayList<>();
    public void setData(List<ImageBucket> data){
        this.data = data;
    }
    public void setLastSelectBucketId(String id){
        lastSelectBucketId = id;
    }
    public String getLastSelectBucketId(){
        return lastSelectBucketId;
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_select_picture_folder_item, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ImageBucket imageBucket = data.get(position);
        holder.tv_folder_name.setText(imageBucket.bucketName);
        if(imageBucket.bucketId.equalsIgnoreCase(ImageAlbumHelper.ALL_BUCKET_LIST_ID)){
            holder.tv_image_num.setText((imageBucket.imageList.size()-1) + "张");
        }else{
            holder.tv_image_num.setText(imageBucket.imageList.size() + "张");
        }
        if(lastSelectBucketId.equalsIgnoreCase(imageBucket.bucketId)){
            holder.indicator.setVisibility(View.VISIBLE);
        }else{
            holder.indicator.setVisibility(View.INVISIBLE);
        }
        int size = imageBucket.imageList.size();
        if(size == 0){
            SelectPictureConfig.imageLoader.displayImage(parent.getContext(),"",holder.iv_folder_image);
        }else if(size >2){
            SelectPictureConfig.imageLoader.displayImage(parent.getContext(),imageBucket.imageList.get(1),holder.iv_folder_image);
        }else{
            SelectPictureConfig.imageLoader.displayImage(parent.getContext(),imageBucket.imageList.get(0),holder.iv_folder_image);
        }

        return convertView;
    }
    class ViewHolder {

        ImageView iv_folder_image;
        TextView tv_folder_name;
        TextView tv_image_num;
        ImageView indicator;

        ViewHolder(View itemView) {
            iv_folder_image = (ImageView) itemView.findViewById(R.id.iv_folder_image);
            tv_folder_name = (TextView) itemView.findViewById(R.id.tv_folder_name);
            tv_image_num = (TextView) itemView.findViewById(R.id.tv_image_num);
            indicator = (ImageView) itemView.findViewById(R.id.indicator);
            itemView.setTag(this);
        }

    }
}
