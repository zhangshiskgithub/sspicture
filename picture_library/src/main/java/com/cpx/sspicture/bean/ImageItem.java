package com.cpx.sspicture.bean;

import java.io.Serializable;

/**
 * 一个图片对象
 * 
 * @author zsq
 * 
 */
public class ImageItem implements Serializable {
	public String imageId = "";// 图片id
	// public String thumbnailPath;
	public String imagePath = "";// 图片真实路径

	public String getFileName() {
		int separatorIndex = imagePath.lastIndexOf(String.valueOf(System.getProperty("file.separator", "/").charAt(0)));
		return (separatorIndex < 0) ? imagePath : imagePath.substring(separatorIndex + 1, imagePath.length());
	}

	@Override
	public String toString() {
		return "ImageItem{" +
				"imageId='" + imageId + '\'' +
				", imagePath='" + imagePath + '\'' +
				'}';
	}
}
