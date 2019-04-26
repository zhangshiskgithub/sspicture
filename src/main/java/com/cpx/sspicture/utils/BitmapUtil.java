package com.cpx.sspicture.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtil {
	/**
	 * 旋转图片
	 * 
	 * @param b
	 *            bitmap
	 * @param degrees
	 *            旋转角度
	 * @return
	 */
	public static Bitmap rotate(Bitmap b, int degrees) {
		if (degrees == 0) {
			return b;
		}
		Matrix matrix = new Matrix();
		matrix.postRotate(degrees);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
		b.recycle();
		return resizedBitmap;
	}

	/**
	 * 获取图片旋转的角度
	 * 
	 * @param path
	 * @return 图片旋转的角度
	 */
	public static int getPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * bitmap 最大化质量压缩
	 * @param image 压缩前的bitmap
	 * @return
	 */
	public static Bitmap maxCompressImage(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(CompressFormat.JPEG, 0, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		image.recycle();
		return bitmap;
	}
	/**
	 * 根据指定的最大图片大小进行压缩
	 * @param image 压缩前的bitmap
	 * @return
	 */
	public static Bitmap compressImage(Bitmap image,int maxSize) {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int options = 100;
		image.compress(CompressFormat.JPEG, options, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		while (baos.toByteArray().length / 1024 > maxSize) {// 循环判断如果压缩后图片是否大于给定大小并且还能压缩
			baos.reset();// 重置baos即清空baos
			options -= 10;// 每次都减少10
			image.compress(CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			if(options<0){
				break;
			}
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		image.recycle();
		return bitmap;
	}

	/**
	 * 长宽压缩图片,并旋转至正确方向
	 * 
	 * @param path 图片path
	 * @param maxWidth 图片最大宽度
	 * @param maxHeight 图片最大高度
	 * @param isRotate 是否自动旋转图片
	 * @return
	 * @throws IOException
	 */
	public static Bitmap revitionImageSize(String path, int maxWidth, int maxHeight,boolean isRotate) throws IOException {
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(path)));
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(in, null, options);
		in.close();
		int i = 0;
		Bitmap bitmap = null;
		int degress = getPictureDegree(path);
		while (true) {
			if ((options.outWidth >> i <= maxWidth) && (options.outHeight >> i <= maxHeight)) {
				in = new BufferedInputStream(new FileInputStream(new File(path)));
				options.inSampleSize = (int) Math.pow(2.0D, i);
				options.inJustDecodeBounds = false;
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;// 节约一半内存
																	// 一个像素2byte
				bitmap = BitmapFactory.decodeStream(in, null, options);
				if(isRotate){
					bitmap = rotate(bitmap, degress);
				}
				break;
			}
			i += 1;
		}
		return bitmap;
	}

	/**
	 * 保存bitmap到文件
	 * 
	 * @param bitmap
	 * @param file
	 * @throws FileNotFoundException
	 */
	public static boolean saveBitmapToFile(Bitmap bitmap, File file) throws FileNotFoundException {
		return bitmap.compress(CompressFormat.JPEG, 100, new FileOutputStream(file));
	}

	/**
	 * 从文件加载完整图片到bitmap
	 * 
	 * @param file
	 * @return
	 */
	public static Bitmap getBitmapFromFile(File file) {
		return BitmapFactory.decodeFile(file.getPath());
	}
	    public static Bitmap doBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {


	        Bitmap bitmap;
	        if (canReuseInBitmap) {
	            bitmap = sentBitmap;
	        } else {
	            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
	        }

	        if (radius < 1) {
	            return (null);
	        }

	        int w = bitmap.getWidth();
	        int h = bitmap.getHeight();

	        int[] pix = new int[w * h];
	        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

	        int wm = w - 1;
	        int hm = h - 1;
	        int wh = w * h;
	        int div = radius + radius + 1;

	        int r[] = new int[wh];
	        int g[] = new int[wh];
	        int b[] = new int[wh];
	        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
	        int vmin[] = new int[Math.max(w, h)];

	        int divsum = (div + 1) >> 1;
	        divsum *= divsum;
	        int dv[] = new int[256 * divsum];
	        for (i = 0; i < 256 * divsum; i++) {
	            dv[i] = (i / divsum);
	        }

	        yw = yi = 0;

	        int[][] stack = new int[div][3];
	        int stackpointer;
	        int stackstart;
	        int[] sir;
	        int rbs;
	        int r1 = radius + 1;
	        int routsum, goutsum, boutsum;
	        int rinsum, ginsum, binsum;

	        for (y = 0; y < h; y++) {
	            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
	            for (i = -radius; i <= radius; i++) {
	                p = pix[yi + Math.min(wm, Math.max(i, 0))];
	                sir = stack[i + radius];
	                sir[0] = (p & 0xff0000) >> 16;
	                sir[1] = (p & 0x00ff00) >> 8;
	                sir[2] = (p & 0x0000ff);
	                rbs = r1 - Math.abs(i);
	                rsum += sir[0] * rbs;
	                gsum += sir[1] * rbs;
	                bsum += sir[2] * rbs;
	                if (i > 0) {
	                    rinsum += sir[0];
	                    ginsum += sir[1];
	                    binsum += sir[2];
	                } else {
	                    routsum += sir[0];
	                    goutsum += sir[1];
	                    boutsum += sir[2];
	                }
	            }
	            stackpointer = radius;

	            for (x = 0; x < w; x++) {

	                r[yi] = dv[rsum];
	                g[yi] = dv[gsum];
	                b[yi] = dv[bsum];

	                rsum -= routsum;
	                gsum -= goutsum;
	                bsum -= boutsum;

	                stackstart = stackpointer - radius + div;
	                sir = stack[stackstart % div];

	                routsum -= sir[0];
	                goutsum -= sir[1];
	                boutsum -= sir[2];

	                if (y == 0) {
	                    vmin[x] = Math.min(x + radius + 1, wm);
	                }
	                p = pix[yw + vmin[x]];

	                sir[0] = (p & 0xff0000) >> 16;
	                sir[1] = (p & 0x00ff00) >> 8;
	                sir[2] = (p & 0x0000ff);

	                rinsum += sir[0];
	                ginsum += sir[1];
	                binsum += sir[2];

	                rsum += rinsum;
	                gsum += ginsum;
	                bsum += binsum;

	                stackpointer = (stackpointer + 1) % div;
	                sir = stack[(stackpointer) % div];

	                routsum += sir[0];
	                goutsum += sir[1];
	                boutsum += sir[2];

	                rinsum -= sir[0];
	                ginsum -= sir[1];
	                binsum -= sir[2];

	                yi++;
	            }
	            yw += w;
	        }
	        for (x = 0; x < w; x++) {
	            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
	            yp = -radius * w;
	            for (i = -radius; i <= radius; i++) {
	                yi = Math.max(0, yp) + x;

	                sir = stack[i + radius];

	                sir[0] = r[yi];
	                sir[1] = g[yi];
	                sir[2] = b[yi];

	                rbs = r1 - Math.abs(i);

	                rsum += r[yi] * rbs;
	                gsum += g[yi] * rbs;
	                bsum += b[yi] * rbs;

	                if (i > 0) {
	                    rinsum += sir[0];
	                    ginsum += sir[1];
	                    binsum += sir[2];
	                } else {
	                    routsum += sir[0];
	                    goutsum += sir[1];
	                    boutsum += sir[2];
	                }

	                if (i < hm) {
	                    yp += w;
	                }
	            }
	            yi = x;
	            stackpointer = radius;
	            for (y = 0; y < h; y++) {
	                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
	                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

	                rsum -= routsum;
	                gsum -= goutsum;
	                bsum -= boutsum;

	                stackstart = stackpointer - radius + div;
	                sir = stack[stackstart % div];

	                routsum -= sir[0];
	                goutsum -= sir[1];
	                boutsum -= sir[2];

	                if (x == 0) {
	                    vmin[y] = Math.min(y + r1, hm) * w;
	                }
	                p = x + vmin[y];

	                sir[0] = r[p];
	                sir[1] = g[p];
	                sir[2] = b[p];

	                rinsum += sir[0];
	                ginsum += sir[1];
	                binsum += sir[2];

	                rsum += rinsum;
	                gsum += ginsum;
	                bsum += binsum;

	                stackpointer = (stackpointer + 1) % div;
	                sir = stack[stackpointer];

	                routsum += sir[0];
	                goutsum += sir[1];
	                boutsum += sir[2];

	                rinsum -= sir[0];
	                ginsum -= sir[1];
	                binsum -= sir[2];

	                yi += w;
	            }
	        }

	        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

	        return (bitmap);
	    }
}
