package com.cpx.sspicture.bean;

import java.io.Serializable;

/**
 * 一个图片对象
 *
 * @author zsq
 *
 */
public class ImageItem implements Serializable {
	public static int TYPE_IMAGE = 0;
	public static int TYPE_VIDEO = 1;
	/**
	 * 缩略图路径
	 */
	public String thumbnailPath;
	/**
	 * 真实路径
	 */
	public String path = "";

	public String name = "";
	public int type =TYPE_IMAGE;

	public ImageItem() {
	}

	public ImageItem(String name,String path,String thumbnailPath,int type) {
		this.name = name;
		this.path = path;
		this.thumbnailPath = thumbnailPath;
		this.type = type;
	}

	public String getFileName() {
		int separatorIndex = path.lastIndexOf(String.valueOf(System.getProperty("file.separator", "/").charAt(0)));
		return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
	}

	@Override
	public String toString() {
		return "ImageItem{" +
				"thumbnailPath='" + thumbnailPath + '\'' +
				", path='" + path + '\'' +
				", type=" + type +
				'}';
	}
}
