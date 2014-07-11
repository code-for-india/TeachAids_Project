package com.project.TeachAids;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;

public class VideoDownloadManager {
	private DownloadManager mDownloadManager;
	private Context mContext;
	private Map<String, Long> mCurrentDownloads;
	private Map<String, Integer> mDownloadPercentages;
	private DownloadManagerListener mListener;
	private Handler mHandler;
	
	private static String TAG = "VideoDownloadManager";
	
	public interface DownloadManagerListener {
		public void onDownloadFinished(String uri);
		public void onDownloadError(String uri);
	}
	
	public VideoDownloadManager(Context context, DownloadManagerListener listener) {
		mContext = context;
		mListener = listener;
		mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		mCurrentDownloads = new HashMap<>();
		mDownloadPercentages = new HashMap<>();
		mHandler = new Handler();
		BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
            	String action = intent.getAction();
            	if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            		mHandler.post(new Runnable() {
						@Override
						public void run() {
            				sendCompleteNotification(intent);
						}
            		});
            	}
            }
		};
		mContext.registerReceiver(downloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}
	
	// return true if download is active. false otherwise (i.e. download has already completed)
	public boolean triggerDownload(String uri, String destination) {
		boolean needTrigger = true;
		
		Pair<Integer, Integer> existingDownloadPair = getDownloadStatus(uri);
		if (existingDownloadPair != null) {
			int downloadId = existingDownloadPair.first.intValue();
			int downloadStatus = existingDownloadPair.second.intValue();
			if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
				if (destination != null) {
					needTrigger = !(new File(destination).exists());
				} else {
					needTrigger = false;
				}
				if (!needTrigger) {
					// existing download already occurred and file exists. bail immediately.
					Log.d(TAG, "Not Triggering new download. Existing successful (completed) download found for uri: " + uri + ". Download Id: " + downloadId);
					return false;
				}
			} else if (downloadStatus == DownloadManager.STATUS_PAUSED || 
					   downloadStatus == DownloadManager.STATUS_PENDING ||
					   downloadStatus == DownloadManager.STATUS_RUNNING) {
				// download in progress. track it
				Log.d(TAG, "Not Triggering new download. Existing download found for uri: " + uri + ". Download Id: " + downloadId);
				needTrigger = false;
				mCurrentDownloads.put(uri, (long) downloadId);
				mDownloadPercentages.put(uri, 0);
			} else {
				// status == DownloadManager.STATUS_FAILED. Trigger new
				Log.d(TAG, "Triggering new download. Existing failed download found for uri: " + uri);				
			}
		} else {
			Log.d(TAG, "Triggering new download. No existing download found for uri: " + uri);
		}

		if (needTrigger) {
			if (!mCurrentDownloads.containsKey(uri)) {
				Request request = new Request(Uri.parse(uri));
				if (destination != null) {
					request.setDestinationUri(Uri.parse("file://" + destination));
				}
				request.setDescription("TeachAids download: " + DateFormat.getDateTimeInstance().format(new Date()));
				request.setTitle("TeachAids download: " + DateFormat.getDateTimeInstance().format(new Date()));
		        long enqueue = mDownloadManager.enqueue(request);
				mCurrentDownloads.put(uri, enqueue);
				mDownloadPercentages.put(uri, 0);
			}			
		}
		
		return true;
	}
	
	public void showDownloads() {
        Intent i = new Intent();
        i.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
        mContext.startActivity(i);
    }

	public final Map<String, Integer> getProgress() {
		// update download percentages before returning
		final DownloadManager.Query query = new DownloadManager.Query();
	    final Cursor cursor = mDownloadManager.query(query);
	    while (cursor.moveToNext()) {
	    	final int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
	    	final String triggeredUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI));
	    	if (mCurrentDownloads.containsKey(triggeredUri)) {
	    		if (status == DownloadManager.STATUS_FAILED) {
	    			int code = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
	    			Log.e(TAG, "Download failed for uri: " + triggeredUri + ". Reason: " + code);
	    			mCurrentDownloads.remove(triggeredUri);
	    			mDownloadPercentages.remove(triggeredUri);
	    			mListener.onDownloadError(triggeredUri);
	    		} else { 
	    		    if (status != DownloadManager.STATUS_SUCCESSFUL) {
	    		        int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
	    		        int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
	    		        mDownloadPercentages.put(triggeredUri, (int) (100.0 * (double)bytes_downloaded / (double)bytes_total));
	    		    }
	    		}
	    	}
	    }
	    
		return mDownloadPercentages;
	}
	
	// returns pair of download id => status of a download that was already triggered.
	// null if download was not previously triggered
	private Pair<Integer, Integer> getDownloadStatus(String uri) {
	    final DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(DownloadManager.STATUS_FAILED |
        						DownloadManager.STATUS_PAUSED | 
        						DownloadManager.STATUS_SUCCESSFUL |
        						DownloadManager.STATUS_RUNNING | 
        						DownloadManager.STATUS_PENDING);
	    final Cursor cursor = mDownloadManager.query(query);
	    while (cursor.moveToNext()) {
	    	final int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
	    	final String triggeredUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI));
	    	if (triggeredUri != null && triggeredUri.equals(uri)) {
	    		return new Pair<>(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID)), status);
	    	}
	    }
	    
	    // download not triggered
	    return null;
	}	
	
	private void sendCompleteNotification(Intent intent) {
		if (mCurrentDownloads.size() > 0) {
			long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
	        Query query = new Query();
	        query.setFilterById(downloadId);
	        Cursor cursor = mDownloadManager.query(query);
	        while (cursor.moveToNext()) {
	        	final int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
		    	final String triggeredUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI));
		    	if (mCurrentDownloads.containsKey(triggeredUri)) {
		    		mCurrentDownloads.remove(triggeredUri);
	    			mDownloadPercentages.remove(triggeredUri);
		    		if (status == DownloadManager.STATUS_SUCCESSFUL) {
		    			mListener.onDownloadFinished(triggeredUri);
		    		} else {
		    			mListener.onDownloadError(triggeredUri);
		    		}
		    	}
	        }
		}
	}
}
