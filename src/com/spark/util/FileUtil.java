package com.spark.util;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class FileUtil {

	private static final String TAG = FileUtil.class.getSimpleName();

	public static String get_IMG_PATH(Context context){
//		 boolean sdCardExist = Environment.getExternalStorageState()    
//                 .equals(android.os.Environment.MEDIA_MOUNTED);  //判断sd卡是否存
//		 if(sdCardExist){
//			 return Environment.getExternalStorageDirectory()+ "/spark/img/"; 
//		 }else{
			 return context.getFilesDir().getAbsolutePath()+ "/spark/img/";
//		 }
	}

	public static String get_TABLE_PATH(Context context){
//		 boolean sdCardExist = Environment.getExternalStorageState()    
//                .equals(android.os.Environment.MEDIA_MOUNTED);  //判断sd卡是否存
//		 if(sdCardExist){
//			 return Environment.getExternalStorageDirectory()+ "/spark/table/"; 
//		 }else{
			 return context.getFilesDir().getAbsolutePath()+ File.separator;
//		 }
	}	
	
	
	public static final int UPLOAD_OK = 1;

	/**
	 * 文件上传
	 *
	 * @param file
	 *            文件
	 * @param handler
	 *            上传过后的处理，即将服务器返回的MD5值存到handler的message里面
	 */
	private static void upLoad(final File file, final Handler handler) {
		new Thread() {
			@Override
			public void run() {
				String reulst = null;
				try {
					reulst = HttpReqImpl.getInstance().upload(
							Constant.FILE_TYPE_IMG, file, 0, file.length());
					if (!StringUtils.isEmpty(reulst)) {
						Trace.e(TAG, "------------" + reulst);

						JSONObject jsonObject = new JSONObject(reulst);
						if (jsonObject.getInt(CodeNum.ERR_CODE) == 0) {
							Message message = new Message();
							message.what = UPLOAD_OK;
							message.obj = jsonObject.getString("errmsg");
							handler.sendMessage(message);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					Trace.e(TAG, "IO异常" + e);
				}
				Trace.e(TAG, "_________________________" + reulst);
			}
		}.start();
	}

	/**
	 * 检查文件是否存在
	 */
	public static void isExist(final File file, final Handler handler,
			final Context context) throws JSONException, IOException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("md5", getFileMD5(file));
		jsonObject.put("type", Constant.FILE_TYPE_IMG);
		@SuppressWarnings("resource")
		final int fileLenth = new FileInputStream(file).available();
		jsonObject.put("length", fileLenth);
		AsyncHttpUtil.post(Constant.CHECK_FILE_EXIST, jsonObject.toString(),
				new TextHttpResponseHandler() {
					@Override
					public void onFailure(int i, Header[] headers, String s,
							Throwable throwable) {
						 Toast.makeText(context, "网络故障", Toast.LENGTH_LONG).show();  
					}

					@Override
					public void onSuccess(int i, Header[] headers, String s) {
						Trace.e(TAG, "检查文件是否存在:" + s);
						if (!StringUtils.isEmpty(s)) {
							try {
								JSONObject object = new JSONObject(s);
								int code = object.getInt(CodeNum.ERR_CODE);
								switch (code) {
								/**
								 * 如果文件不存在，则调用文件上传方法
								 */
								case CodeNum.RESOURCE_NOT_EXIST:
									upLoad(file, handler);
									break;

								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
	}

	// 取得文件MD5值
	private static String getFileMD5(File file) {
		if (!file.isFile()) {
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte buffer[] = new byte[5 * 1024];
		int len;
		try {
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while ((len = in.read(buffer, 0, 5 * 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		BigInteger bigInt = new BigInteger(1, digest.digest());
		String md5 = bigInt.toString(16);
		return md5;
	}

	public static String getPath(Context context, Intent data) {
		String picPath = "";
		String[] pojo = { MediaStore.Images.Media.DATA };
		Uri uri = data.getData();
		CursorLoader cursorLoader = new CursorLoader(context, uri, pojo, null,
				null, null);
		Cursor cursor = cursorLoader.loadInBackground();
		cursor.moveToFirst();
		String path = cursor.getString(cursor.getColumnIndex(pojo[0]));
		if (path != null && path.length() > 0) {
			picPath = path;
		}
		return picPath;
	}

	
	
	public static boolean saveBitmap2file(Context context, Bitmap bitmap, String filename) {
		String img_path = get_IMG_PATH(context);
		File dir = new File(img_path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		filename = img_path + filename;
		File file = new File(filename);
		FileOutputStream fOut = null;
		try {
			file.createNewFile();
			fOut = new FileOutputStream(file);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		try {
			if (bitmap != null && fOut != null) {
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);// 把Bitmap对象解析成流
				fOut.flush();
				fOut.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 生成一个随机的文件名
	 * 
	 * @return
	 */
	public static String getRandomFileName() {
		String rel = "";
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss",
				Locale.getDefault());
		Date curDate = new Date(System.currentTimeMillis());
		rel = formatter.format(curDate);
		rel = rel + new Random().nextInt(1000);
		return rel;
	}

	public static String getResourcesPath(Context context, int index) {
		String img_path = FileUtil.get_IMG_PATH(context);
		return img_path + "fetal_d" + index + ".png";
	}

	/**
	 * 
	 * 获取裁剪后的圆形图片
	 * 
	 * @param radius半径
	 */

	public static Bitmap getCroppedRoundBitmap(Bitmap bmp, int radius) {
		Bitmap scaledSrcBmp;
		int diameter = radius * 2;
		// 为了防止宽高不相等，造成圆形图片变形，因此截取长方形中处于中间位置最大的正方形图片
		int bmpWidth = bmp.getWidth();
		int bmpHeight = bmp.getHeight();
		int squareWidth = 0, squareHeight = 0;
		int x = 0, y = 0;
		Bitmap squareBitmap;
		if (bmpHeight > bmpWidth) {// 高大于宽
			squareWidth = squareHeight = bmpWidth;
			x = 0;
			y = (bmpHeight - bmpWidth) / 2;
			// 截取正方形图片
			squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth, squareHeight);
		} else if (bmpHeight < bmpWidth) {// 宽大于高
			squareWidth = squareHeight = bmpHeight;
			x = (bmpWidth - bmpHeight) / 2;
			y = 0;
			squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth, squareHeight);
		} else {
			squareBitmap = bmp;
		}

		if (squareBitmap.getWidth() != diameter || squareBitmap.getHeight() != diameter) {
			scaledSrcBmp = Bitmap.createScaledBitmap(squareBitmap, diameter, diameter, true);
		} else {
			scaledSrcBmp = squareBitmap;
		}

		Bitmap output = Bitmap.createBitmap(scaledSrcBmp.getWidth(), scaledSrcBmp.getHeight(),Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		Paint paint = new Paint();
		Rect rect = new Rect(0, 0, scaledSrcBmp.getWidth(), scaledSrcBmp.getHeight());
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawCircle(scaledSrcBmp.getWidth() / 2, scaledSrcBmp.getHeight() / 2, scaledSrcBmp.getWidth() / 2, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(scaledSrcBmp, rect, rect, paint);
		bmp = null;
		squareBitmap = null;
		scaledSrcBmp = null;
		return output;
	}
	
	public static void copyAssetFileToFiles(Context context, String filename, String newPath)
			throws IOException {
		InputStream is = context.getAssets().open(filename);
		byte[] buffer = new byte[is.available()];
		is.read(buffer);
		is.close();
		
		File of = new File(newPath);
		of.createNewFile();
		FileOutputStream os = new FileOutputStream(of);
		os.write(buffer);
		os.close();
	}	
}
