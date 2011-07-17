package com.othermedia.asyncimage;

/*

  Copyright 2011 the OTHER media

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  
 */

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Environment;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.graphics.drawable.BitmapDrawable;

public class AsyncImageCache {
	
	private File cacheDir;
	private boolean fileCacheEnabled = false;
	private String cacheDirName;
	
	private MessageDigest md; // For hashing URLs in file cache
	
	
	// List of Drawables available from 2nd layer file cache
	private ArrayList<String> fileCacheList;
	
	// This is the 1st layer memory cache for Drawables
	Map<String, BitmapDrawable> cacheMap = new HashMap<String, BitmapDrawable>();
	
	// This is a Map that holds information on which ImageView requested which URL
	// (to protect against clashing when using reusable ImageViews on ListView)
	Map<ImageView,String> imageTargets = Collections.synchronizedMap( new HashMap<ImageView, String>());
	
	// This is the Download queue for ordered images
	List<AsyncImageTask> downloadQueue = Collections.synchronizedList(new LinkedList<AsyncImageTask>());
	
	// AsyncImageDownloader for this cache instance
	Thread downloaderThread = null;

	// Is the queue rejecting new tasks? (for ScrollView responsiveness)
	private boolean queueOnHold = false;


	
	public AsyncImageCache(Context context, boolean enableFileCache, String cacheDirName) {
		
		if (enableFileCache) {			
			try {
			// Initialize hashing
			md = MessageDigest.getInstance("MD5");
			
			this.cacheDirName = cacheDirName;
			
			// Open file cache directory
			String cacheDest = Environment.getDownloadCacheDirectory().toString()+"/."+this.cacheDirName;
						
			cacheDir = new File(cacheDest);
			cacheDir.mkdirs();
			if (!cacheDir.isDirectory()) {
				cacheDir = new File(context.getCacheDir().toString());
			}
			
			fileCacheList = new ArrayList<String>();
			File[] filesInCache = cacheDir.listFiles();
			for (File f : filesInCache)
				if (f.getName().endsWith(".png")) // Images are always converted to PNG when cached
					fileCacheList.add(f.getName());

			fileCacheEnabled = true;
			} catch (Exception e) {
				// If any of the above fail, this means file cache is not possible and is turned off:
				fileCacheEnabled = false;
			}
			}
	}
	
	/**
	 * Load and cache asynchronously images from the Web to your ImageView.
	 * Everything is handled inside the class (clashing, caching, downloading etc).
	 * 
	 * @param targetImageView - to which ImageView
	 * @param imageURL - image URL address
	 * @param defaultPlaceholderResource - (optional) default Resource graphic as a "placeholder"
	 */
	public synchronized void loadRemoteImage(ImageView targetImageView, String imageURL, int defaultPlaceholderResource ) {

		// Update the last URL that the target requested:
		imageTargets.put(targetImageView, imageURL);			

		if (hasCached(imageURL)) { // Image exists in cache:

			targetImageView.setImageDrawable(cacheMap.get(imageURL));
			
		} else { // Image not in cache, download:			
			
			if (defaultPlaceholderResource != -1) targetImageView.setImageResource(defaultPlaceholderResource);

			if (!queueOnHold) {
				downloadQueue.add(new AsyncImageTask(imageURL, targetImageView));
				// Fun fact: There might be multiple ImageViews requesting the same URL, so
				// I'm not checking if the URL is already in the queue, it's going to be
				// downloaded only once anyway, because there's only one downloading thread
				// so the additional tasks linked to other ImageViews, will use the cache.
				
				if (downloaderThread == null){ // No downloader running?
					// Launch downloader:
					downloaderThread = new Thread( new AsyncImageDownloader(this) );
					downloaderThread.start();
				}
				
			}
		}
	}
	
	public void downloaderFinished() {
		downloaderThread = null; 
	}
	
	/**
	 * Load and cache asynchronously images from the Web to your ImageView.
	 * Everything is handled inside the class (clashing, caching, downloading etc).
	 * See overloaded version for "placeholders".
	 * 
	 * @param targetImageView - to which ImageView
	 * @param imageURL - image URL address
	 */
	public void loadRemoteImage(ImageView targetImageView, String imageURL ) {
		this.loadRemoteImage(targetImageView, imageURL, -1);
	}
		
	public void updateTargetFromHandler(final ImageView targetImageView, final String imageURL) {
		
			// dispatch "updateTarget" using the ImageView's handler
			targetImageView.getHandler().post(new Runnable() {
				public void run() {
					updateTarget(targetImageView,imageURL);
				}
			});
	}
	
	public synchronized void updateTarget(ImageView targetImageView, String imageURL) {
		
		// Get the last requested URL from that target:
		String currentURL = imageTargets.get(targetImageView);
		
		// If the target is still interested in that URL, set it!
		if (currentURL != null && currentURL.equals(imageURL) && cacheMap.containsKey(currentURL) ) {
		targetImageView.setImageDrawable( cacheMap.get(currentURL) );					
		}		
	}

	/**
	 * Halt the cache loader - used for improving performance on ScrollViews and ListViews
	 */
	public void holdQueue() {
		 queueOnHold = true;
	}
	
	/**
	 * Resume the cache loader and auto-queue the most recent targets.
	 */
	public void resumeQueue() {
		 queueOnHold = false; 

		 // Auto-queue all recent targets!
		 ArrayList<ImageView> currentTargets = new ArrayList<ImageView>(imageTargets.keySet());
		 Collections.reverse(currentTargets);
		 for (ImageView target : currentTargets) {
			 loadRemoteImage(target, imageTargets.get(target));
		 }
		 
	}
	
	
	public boolean hasCached(String path) {
		return cacheMap.containsKey(path);
	}
	
	public void cacheImage(String path, BitmapDrawable b) {
		cacheMap.put(path, b);	
	}

	public File getCacheDir() {
		return cacheDir;
	}

	public void setCacheDir(File cacheDir) {
		this.cacheDir = cacheDir;
	}

	public String hashString( String stringToHash ) {
		byte[] resultByte = md.digest(stringToHash.getBytes());
		return byteArrayToHexString(resultByte) ;
	}

	private static String byteArrayToHexString(byte[] hash) {
	    Formatter formatter = new Formatter();
	    for (byte b : hash) {
	        formatter.format("%02x", b);
	    }
	    return formatter.toString();
	}

	public boolean isFileCacheEnabled() {
		return fileCacheEnabled;
	}

	public boolean hasInFileCache(String fname) {
		if (fileCacheList == null) return false;
		return fileCacheList.contains(fname);
	}

	public void fileCacheRetrieve(String fname, String urlpath) throws Exception {
		File inputFile = new File(getCacheDir(), fname);
		BitmapDrawable b= new BitmapDrawable(inputFile.getPath());
		cacheImage(urlpath, b);
	}

	public void setQueueOnHold(boolean queueState) {
		this.queueOnHold = queueState;
	}

	public boolean isQueueOnHold() {
		return queueOnHold;
	}
	
	public void installScrollListnerOn(ListView listView) {
		
		final AsyncImageCache localReference = this;
		
		listView.setOnScrollListener(new OnScrollListener() {
			
			public void onScrollStateChanged(AbsListView listView, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) localReference.holdQueue();
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) localReference.resumeQueue();
			}
			
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {}
		});
	}

}
