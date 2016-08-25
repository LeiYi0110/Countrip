package com.pi9Lin.volley;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class VolleyBins {
	public static void saveBitmap2(Bitmap mBitmap, String imageURL, Context cxt) {

		String bitmapName = imageURL.substring(imageURL.lastIndexOf("/") + 1);

		FileOutputStream fos = null;

		try {
			fos = cxt.openFileOutput(bitmapName, Context.MODE_PRIVATE);
			mBitmap.compress(Bitmap.CompressFormat.PNG, 900, fos);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
			// 这里是保存文件产生异常
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// fos流关闭异常
					e.printStackTrace();
				}
			}
		}
	}

	public static boolean isFileExist(String fileName, Context cxt) {
		String bitmapName = fileName.substring(fileName.lastIndexOf("/") + 1);
		List<String> nameLst = Arrays.asList(cxt.fileList());
		if (nameLst.contains(bitmapName)) {
			return true;
		} else {
			return false;
		}

	}

	public static Bitmap getBitmap2(String fileName, Context cxt) {
		String bitmapName = fileName.substring(fileName.lastIndexOf("/") + 1);
		FileInputStream fis = null;
		try {
			fis = cxt.openFileInput(bitmapName);
			byte[] b = new byte[fis.available()];
			fis.read(b);
			fis.close();
			Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			// 这里是读取文件产生异常
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// fis流关闭异常
					e.printStackTrace();
				}
			}
		}
		// 读取产生异常，返回null
		return null;
	}
}
