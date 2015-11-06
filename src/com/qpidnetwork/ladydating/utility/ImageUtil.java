package com.qpidnetwork.ladydating.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.os.Build;

public class ImageUtil {
	

	public static Bitmap compressImage(Bitmap image, int sizeLimit) {  
		  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这�?100表示不压缩，把压缩后的数据存放到baos�?  
        int options = 100;  
        while ( baos.toByteArray().length / 1024 > sizeLimit) {  //循环判断如果压缩后图片是否大�?100kb,大于继续压缩         
            baos.reset();//重置baos即清空baos  
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos�?  
            options -= 10;//每次都减�?10  
        }  
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream�?  
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片  
        
        return bitmap;  
    } 
	

	public static Bitmap scaleImageFile(String srcPath, float destWidth, float destHeight) { 
		
		if((destWidth == 0)||(destHeight == 0)){
			//Log.i("hunter", "scaleImageFile destWidth: " + destWidth + " destHeight: " + destHeight);
			return null;
		}
		
        BitmapFactory.Options newOpts = new BitmapFactory.Options();  
        //�?始读入图片，此时把options.inJustDecodeBounds 设回true�?  
        newOpts.inJustDecodeBounds = true;  
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//此时返回bm为空  
          
        int w = newOpts.outWidth;  
        int h = newOpts.outHeight;  
        
        float widthRadio = w/destWidth;
        float heightRadio = h/destHeight;
        int be = 1;//默认1表示不缩�?
        if (widthRadio > heightRadio) {//如果宽度比大于高度比，按高缩�?
            be = (int) (heightRadio);  
        } else {//如果宽度比小于高度比，按宽缩�?  
            be = (int) (widthRadio);  
        }  
        if (be <= 0)  
            be = 1;  
        newOpts.inSampleSize = be;//设置缩放比例  
        newOpts.inJustDecodeBounds = false;
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false�?  
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);  
        return bitmap; 
    }
	

	public static Bitmap scaleBitmap(Bitmap image, float destWidth, float destHeight) {
		
		if((destWidth == 0)||(destHeight == 0)){
			//Log.i("hunter", "scaleBitmap destWidth: " + destWidth + " destHeight: " + destHeight);
			return null;
		}
	      
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();         
	    image.compress(Bitmap.CompressFormat.JPEG, 100, baos);  
	    if( baos.toByteArray().length / 1024 > 1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出    
	        baos.reset();//重置baos即清空baos  
	        image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos�?  
	    }  
	    ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());  
	    BitmapFactory.Options newOpts = new BitmapFactory.Options();  
	    //�?始读入图片，此时把options.inJustDecodeBounds 设回true�?  
	    newOpts.inJustDecodeBounds = true;  
	    Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);  
	      
	    int w = newOpts.outWidth;  
	    int h = newOpts.outHeight;  
 
	    float widthRadio = w/destWidth;
        float heightRadio = h/destHeight;
        int be = 1;//默认1表示不缩�?
        if (widthRadio > heightRadio) {//如果宽度比大于高度比，按高缩�?
            be = (int) (heightRadio);  
        } else {//如果宽度比小于高度比，按宽缩�?  
            be = (int) (widthRadio);  
        }  
        if (be <= 0)  
            be = 1;   
        
	    newOpts.inSampleSize = be;//设置缩放比例  
	    newOpts.inJustDecodeBounds = false;
	    //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false�?  
	    isBm = new ByteArrayInputStream(baos.toByteArray());  
	    bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);  
	    return bitmap;//压缩好比例大小后再进行质量压�?  
	}
	

	public static Bitmap crop(Bitmap bitmap, int srcWidth, int srcHeight, float radio){
		
		if((srcWidth == 0)||(srcHeight == 0)||(radio == 0)){
			//Log.i("hunter", "crop srcWidth: " + srcWidth + " srcHeight: " + srcHeight + " radio: " + radio);
			return null;
		}

		int destWidth = 0;
		int destHeight = 0;
		float tempRadio = ((float)srcHeight)/srcWidth;
		if(tempRadio > radio){
			destWidth = srcWidth;
			destHeight = (int)(srcWidth*radio);
		}else{
			destWidth = (int)(srcHeight/radio);
			destHeight = srcHeight;
		}
		Bitmap newBitmap = null; 
		try {
			return Bitmap.createBitmap(bitmap, 0, 0, destWidth, destHeight);
		} catch (Exception e) {

		}finally{
		}
		return newBitmap;
	}
	

	public static Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(filePath, options);
	    
	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(filePath, options);
	}
	
	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
//	
//	        final int halfHeight = height / 2;
//	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((height / inSampleSize) > reqHeight
	                || (width / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	    
	    return inSampleSize;
	}
	
	
	public static Bitmap get2DpRoundedImage(Context context, Bitmap inBmp){
		if( inBmp == null ) {
			return null;
		}
			
		final float roundPx = 2.0f * context.getResources().getDisplayMetrics().density;

		if (inBmp.getWidth() <= roundPx || inBmp.getHeight() <= roundPx) return inBmp;
		
		      Bitmap output = Bitmap.createBitmap(inBmp.getWidth(), inBmp
		                .getHeight(), Config.ARGB_8888);
		        Canvas canvas = new Canvas(output);

		        final int color = 0xff424242;
		        final Paint paint = new Paint();
		        final Rect rect = new Rect(0, 0, inBmp.getWidth(), inBmp.getHeight());
		        final RectF rectF = new RectF(rect);
		        


		        paint.setAntiAlias(true);
		        paint.setColor(color);
		        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		        canvas.drawBitmap(inBmp, rect, rect, paint);
		        inBmp.recycle();
		        return output;

	}
	
	
	public static Bitmap get2DpRoundedImage(Context context, Bitmap inBmp, float border, int borderColor){
		return get2DpRoundedImage(context, inBmp, border, borderColor, true);
	}
	
	public static Bitmap get2DpRoundedImage(Context context, Bitmap inBmp, float border, int borderColor, boolean recycleInputBmp){
		
		
		final float roundPx = 2.0f * context.getResources().getDisplayMetrics().density;
		if (inBmp.getWidth() <= roundPx || inBmp.getHeight() <= roundPx) return inBmp;
		
		      Bitmap output = Bitmap.createBitmap(inBmp.getWidth(), inBmp
		                .getHeight(), Config.ARGB_8888);
		        Canvas canvas = new Canvas(output);

		        Paint paint = new Paint();

		        RectF rectF = new RectF(0, 0, inBmp.getWidth(), inBmp.getHeight());
		        Rect rect = new Rect((int)border, (int)border, (int)((float)inBmp.getWidth() - border), (int)((float)inBmp.getHeight() - border));
		        

		        paint.setAntiAlias(true);
		        paint.setColor(borderColor);
		        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		        canvas.drawBitmap(inBmp, rect, rect, paint);
		        if (recycleInputBmp) inBmp.recycle();
		        return output;

	}
	
	/**
	 * 读取图片文件，并按照长边缩放�?获取的图�?
	 * @param filePath
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap decodeAndScaleBitmapFromFile(String filePath, int reqWidth, int reqHeight){
		/*初步使用inSampleSize方式缩放，防止读取大图直接导致溢�?*/
		Bitmap resizeBitmap = null;
		Bitmap tempBitmap = decodeSampledBitmapFromFile(filePath, reqWidth, reqHeight);
		if (null != tempBitmap) {
			/*使用Matrix再处理生成图片，以长边缩�?*/
			int bmpWidth  = tempBitmap.getWidth();   
		    int bmpHeight  = tempBitmap.getHeight(); 
		    Matrix matrix = new Matrix();
		    float scaleWidth  = (float) reqWidth / bmpWidth;     
		    float scaleHeight = (float) reqHeight / bmpHeight; 
		    if(scaleHeight > scaleWidth){
		    	matrix.postScale(scaleWidth, scaleWidth);
		    }else{
		    	matrix.postScale(scaleHeight, scaleHeight);
		    }
		    resizeBitmap = Bitmap.createBitmap(tempBitmap, 0, 0, bmpWidth, bmpHeight, matrix, false);  
		    tempBitmap.recycle();
		}
	    return resizeBitmap;
	}
	

	public static Bitmap decodeHeightDependedBitmapFromFile(String filePath,  int reqHeight){
		/*初步使用inSampleSize方式缩放，防止读取大图直接导致溢�?*/
		Bitmap resizeBitmap = null;
		Bitmap tempBitmap = decodeSampledBitmapFromFile(filePath, reqHeight, reqHeight);
		if (null != tempBitmap) {
			/*使用Matrix再处理生成图片，以长边缩�?*/
			int bmpWidth  = tempBitmap.getWidth();   
		    int bmpHeight  = tempBitmap.getHeight(); 
		    Matrix matrix = new Matrix();    
		    float scaleHeight = (float) reqHeight / bmpHeight; 
		    matrix.postScale(scaleHeight, scaleHeight);
		    resizeBitmap = Bitmap.createBitmap(tempBitmap, 0, 0, bmpWidth, bmpHeight, matrix, false);  
		    tempBitmap.recycle();
		}
	    return resizeBitmap;
	}
	
	public static Bitmap decodeHeightDependedBitmapFromFile(Bitmap inBmp,  int reqHeight){

		/*使用Matrix再处理生成图片，以长边缩�?*/
		int bmpWidth  = inBmp.getWidth();   
	    int bmpHeight  = inBmp.getHeight(); 
	    Matrix matrix = new Matrix();    
	    float scaleHeight = (float) reqHeight / bmpHeight; 
	    matrix.postScale(scaleHeight, scaleHeight);
	    Bitmap resizeBitmap = Bitmap.createBitmap(inBmp, 0, 0, bmpWidth, bmpHeight, matrix, false);  
	    inBmp.recycle();
	    return resizeBitmap;
	}
	
	/**
	 * 把Bitmap变成圆角
	 * @param bitmap		待处理的Bitmap
	 * @param topRadius		顶部圆角的半�?(0：不处理)
	 * @param bottomRadius	底部圆角的半�?(0：不处理)
	 * @return
	 */
	
	public static Bitmap filletBitmap(Bitmap bitmap, int topRadius, int bottomRadius, float density)
	{
		Bitmap desBitmap = bitmap;
		if (null != bitmap
			&& (topRadius > 0 || bottomRadius > 0)) 
		{
			int topRadiusPixel = (int)(topRadius > 0 ? topRadius*density : 0);
			int bottomRadiusPixel = (int)(bottomRadius > 0 ? bottomRadius*density : 0);

			desBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
			Canvas c = new Canvas(desBitmap);
			
			Shader shader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
			Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
		    paint.setAntiAlias(true);
		    paint.setShader(shader);
		    
		    // 画顶�?
			RectF topRect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight() - bottomRadiusPixel);
			c.drawRoundRect(topRect, topRadiusPixel, topRadiusPixel, paint);
			// 画底�?
			RectF bottomRect = new RectF(0, topRadiusPixel, bitmap.getWidth(), bitmap.getHeight());
			c.drawRoundRect(bottomRect, bottomRadiusPixel, bottomRadiusPixel, paint);
			// 画中�?
			c.drawRect(new RectF(0, topRadiusPixel, bitmap.getWidth(), bitmap.getHeight() - bottomRadiusPixel),  paint);
		}
		return desBitmap;
	}
	

	public static boolean saveBitmapToFile(String filePath, Bitmap bitmap, Bitmap.CompressFormat format, int quality) 
	{
		if (filePath.isEmpty()
			|| bitmap == null
			|| quality <= 0)
		{
			return false;
		}
		
		boolean result = false;
		
		try {
			File file = new File(filePath);
			
			// 删除已存在的文件
			if ( file.exists() && file.isFile() ) {
				file.delete();
			}
			
			// 写入压纹图片数据
			FileOutputStream fOut = null;
			fOut = new FileOutputStream(file);
		    FileLock fl = ((FileOutputStream) fOut).getChannel().tryLock();  
		    if (fl != null) {
		    	bitmap.compress(format, quality, fOut);
		    	fl.release();
		    }
		    fOut.close();
	        
	        // 标记成功
	        result = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
}
