package com.qpidnetwork.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.ladydating.QpidApplication;
import com.qpidnetwork.manager.WebsiteManager;

/**
 * 下载文件工具
 */
public class FileDownloader {
	/**
	 * 下载文件工具回调
	 */
	public interface FileDownloaderCallback {
		void onSuccess(FileDownloader loader);
		void onFail(FileDownloader loader);
		void onUpdate(FileDownloader loader, int progress);
	}
	
	private String mUrl;
	private String mLocalPath;
	private FileDownloaderCallback mCallback;
	private Context mContext;
	public boolean notModified = false;
	
	private FileRequest mRequest = null;
	private boolean mUseCache = false;
	private boolean mBigFile = false;
	private static RequestQueue mRequestQueue = null;
	
	//记录大文件进度,对应BigFile有效
	Integer downloadProgress = 0;
	
	public FileDownloader() {
	}
	
	public FileDownloader(Context context) {
		mContext = context;
	}
	
	/**
	 * 设置是否使用缓存
	 * @param bUseCache
	 */
	public void SetUseCache(boolean bUseCache) {
		mUseCache = bUseCache;
	}
	
	/**
	 * 设置是否下载大文件
	 * @param bBigFile
	 */
	public void SetBigFile(boolean bBigFile) {
		mBigFile = bBigFile;
	}
	
	public void StartDownload(final String url, final String localPath, final FileDownloaderCallback callback) {
		Stop();
		
		mUrl = url;
		mLocalPath = localPath;
		mCallback = callback;
		notModified = false;
		
		downloadProgress = 0;
		
		Log.d("FileDownloader", "StartDownload( url : " + url + ", localPath : " + localPath + " )");
		
		final FileDownloader loader = this;
		
		// Instantiate the RequestQueue.
		if( mRequestQueue == null ) {
			mRequestQueue = Volley.newRequestQueue(mContext);
		}
		
		// Request a string response from the provided URL.
		mRequest = new FileRequest(Request.Method.GET, url, this,
		            new Response.Listener<FileDownloader>() {
		    @Override
		    public void onResponse(FileDownloader loader) {
		    	Log.d("FileDownloader", "onResponse( url : " + url + ", loader.GetLocalPath() : " + loader.GetLocalPath() + " )");
		    	mRequest = null;
		    }
		}, new Response.ErrorListener() {
		    @Override
		    public void onErrorResponse(VolleyError error) {
		    	Log.d("FileDownloader", "onErrorResponse( url : " + url + ", localPath : " + localPath + ", error : " + error.toString() + " )");
				if( mCallback != null ) {
					mCallback.onFail(loader);
				}
				mRequest = null;
		    }
		});
		mRequest.setShouldCache(mUseCache);
		mRequest.SetBigFile(mBigFile);
		
		// Add the request to the RequestQueue.
		mRequestQueue.add(mRequest);
	}
	
	public void Stop() {
		Log.d("FileDownloader", "Stop( url : " + mUrl + ", localPath : " + mLocalPath + " )");
		
		if( mRequest != null ) {
			mRequest.cancel();
		}
		
		mCallback = null;
	}
	
	public void StopDonotWait() {
		Log.d("FileDownloader", "StopDonotWait( url : " + mUrl + ", localPath : " + mLocalPath + " )");
		
		if( mRequest != null ) {
			mRequest.cancel();
		}
		
		mCallback = null;
	}
	
	public boolean IsDownloading() {
		boolean bFlag = false;
		if( mRequest != null ) {
			bFlag = true;
		}
		return bFlag;
	}
	
	public void Copy(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);

	    FileLock fl = ((FileOutputStream) out).getChannel().tryLock();  
	    if (fl != null) {  
		    // Transfer bytes from in to out
		    byte[] buf = new byte[1024];
		    int len;
		    while ((len = in.read(buf)) > 0) {
		        out.write(buf, 0, len);
		    }
		    fl.release();
	    }
	    
	    in.close();
	    out.close();
	}
	
	public String GetUrl() {
		return mUrl;
	}
	
	public String GetLocalPath() {
		return mLocalPath;
	}
	
	/**
	 * 获取文件当前下载进度接口
	 * @return
	 */
	public int getCurrentProgress(){
		int progress = 0;
		if(mBigFile){
			synchronized (downloadProgress) {
				progress = downloadProgress;
			}
		}else{
			progress = 100;
		}
		return progress;
	}
	
	/**
	 * A canned request for retrieving the response body at a given URL as a String.
	 */
	public class FileRequest extends Request<FileDownloader> {
	    private final Listener<FileDownloader> mListener;
	    private final FileDownloader mLoader;
	    /**
	     * Creates a new request with the given method.
	     *
	     * @param method the request {@link Method} to use
	     * @param url URL to fetch the string at
	     * @param listener Listener to receive the String response
	     * @param errorListener Error listener, or null to ignore errors
	     */
	    public FileRequest(int method, String url, FileDownloader loader, Listener<FileDownloader> listener,
	            ErrorListener errorListener) {
	        super(method, url, errorListener);
	        mLoader = loader;
	        mListener = listener;
	    }

	    /**
	     * Creates a new GET request.
	     *
	     * @param url URL to fetch the string at
	     * @param listener Listener to receive the String response
	     * @param errorListener Error listener, or null to ignore errors
	     */
	    public FileRequest(String url, FileDownloader loader, Listener<FileDownloader> listener, ErrorListener errorListener) {
	        this(Method.GET, url, loader, listener, errorListener);
	    }

	    @Override
	    protected void deliverResponse(FileDownloader response) {
	        mListener.onResponse(response);
	    }

	    @SuppressWarnings("deprecation")
		@Override
	    protected Response<FileDownloader> parseNetworkResponse(NetworkResponse response) {
			Log.d("FileDownloader", "parseNetworkResponse( " +
					"statusCode : " + response.statusCode + ", " + 
					"headers : " + response.headers +
					" )");
			notModified = response.notModified;
			
			FileOutputStream fos = null;
			File tmpFile = null;
			boolean bFlag = false;
			
			long fileLength = 0; // 取得文件长度
			int currPosition = 0; // 已下载大小记录
			
			try {
				tmpFile = File.createTempFile("tmp", "");
				fos = new FileOutputStream(tmpFile);
				
				InputStream in = null;
				if( mBigFile && response.entity != null ) {
					in = response.entity.getContent();
					fileLength = response.entity.getContentLength();
				}
				if( in != null ) {
					// big file
					// write 10k every time
					byte buffer[] = new byte[1024 * 10];
					int len = 0;
					while ( (len = in.read(buffer)) != -1 ) {
						fos.write(buffer, 0, len);
						currPosition += len;
						synchronized (downloadProgress) {
							if(fileLength != 0){
								downloadProgress = (int) (100 * currPosition / fileLength);
								Log.d("FileDownloader", "downloadProgress : " + downloadProgress);
							}
						}
		            }
				} else if( response.data != null ) {
					fos.write(response.data, 0, response.data.length);
				}
				
				File dst = new File(mLoader.GetLocalPath());
				if( dst != null && dst.getParent() != null && dst.getParent().length() > 0 ) {
					File parent = new File(dst.getParent());
					parent.mkdirs();
				}
				Copy(tmpFile, new File(mLoader.GetLocalPath()));
				
				bFlag = true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				if( tmpFile != null && tmpFile.exists() ) {
					tmpFile.delete();
				}
				
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				if( response.entity != null ) {
					try {
						response.entity.consumeContent();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			if( bFlag ) {
		    	if( mCallback != null ) {
					mCallback.onSuccess(mLoader);
				}
			} else {
				if( mCallback != null ) {
					mCallback.onFail(mLoader);
				}
			}
	    	
	        return Response.success(mLoader, HttpHeaderParser.parseCacheHeaders(response));
	    }
	    
	    @Override
	    public Map<String, String> getHeaders() throws AuthFailureError {
	    	Map<String, String> headers = new HashMap<String, String>();
	    	if (WebsiteManager.getInstance().mWebSite.isDemo) {
	    		String basicAuth = "Basic " + new String(Base64.encode("test:5179".getBytes(), Base64.NO_WRAP));
	    		headers.put("Authorization", basicAuth);
	    	}
	    	return headers;
	    } 
	}
}
