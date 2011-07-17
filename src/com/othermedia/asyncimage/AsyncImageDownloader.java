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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

public class AsyncImageDownloader implements Runnable {

	
	private AsyncImageCache parentImageCache;
	
	private static String DebugTag = "AsyncImageDownloader";
	
	@Override
	public void run() {
		if (parentImageCache == null) return;

		// The downloader runs until the queue is empty or the cache is disconnected
		while (parentImageCache != null && parentImageCache.downloadQueue.size() > 0) {
			
			AsyncImageTask currentTask = parentImageCache.downloadQueue.remove(0); // Pop first task in (FIFO)
			
			try {

				// -- Check and retrieve file cache
				String hashedPath = null;
				try {
					if (parentImageCache.isFileCacheEnabled()) {
						hashedPath = parentImageCache.hashString(currentTask.getImageURL())+".png";
						if (parentImageCache.hasInFileCache(hashedPath)) {
							parentImageCache.fileCacheRetrieve(hashedPath, currentTask.getImageURL());
						}
					}
				} catch (Exception e) {
					Log.e(DebugTag, "Exception on retrieving from file cache: "+e+": "+e.getMessage());
				}

				
				// - Check cache, if the image got retrieved by an earlier request or from file cache
				if (parentImageCache.hasCached(currentTask.getImageURL())) {
					parentImageCache.updateTargetFromHandler(currentTask.getTargetImageView(), currentTask.getImageURL());
				} else { 
					// ...Else download image:
					
					int tries = 3; // Allowed download Tries
					
					while (tries >  0) {  
						tries--; // In case of a short network drop or other random connection error.
						
						try {
							
							// -- Create HTTP Request
							URL requestUrl = new URL(currentTask.getImageURL());
							HttpGet httpRequest = new HttpGet(requestUrl.toURI());
							HttpClient httpClient = new DefaultHttpClient();
							HttpResponse httpResponse = (HttpResponse) httpClient.execute(httpRequest);
							
							// -- Process Bitmap Download
							BufferedHttpEntity bufferedEntity = new BufferedHttpEntity(httpResponse.getEntity());
							InputStream inputStream = bufferedEntity.getContent();
							Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
							BitmapDrawable drawable = new BitmapDrawable(bitmap);
							parentImageCache.cacheImage(currentTask.getImageURL(), drawable); // - cache downloaded bitmap, then update ImageView:
							parentImageCache.updateTargetFromHandler(currentTask.getTargetImageView(), currentTask.getImageURL());
							
							tries = 0; // Release tries.
							inputStream.close();
							
							 // -- Cache Drawable as file, if enabled
							if (parentImageCache.isFileCacheEnabled()) {
								try {
									if (hashedPath != null) {
									File outputFile = new File(parentImageCache.getCacheDir(), hashedPath);
									FileOutputStream outputStream = new FileOutputStream(outputFile);
									bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
									}
								} catch (Exception e) {
									Log.e(DebugTag, "Exception on file caching process: "+e+": "+e.getMessage());
								}
							} // -- End file caching.
							
						} catch (Exception e) {
							Log.e(DebugTag, "Exception on download process: "+e+": "+e.getMessage());
							try { Thread.sleep(250); } catch (InterruptedException ei) {}
						}
					}												
				}
				
			
			} catch (Exception e) {
				Log.e(DebugTag, "Exception on current AsyncImageTask: "+e+": "+e.getMessage());
			}
			
		}
		
		parentImageCache.downloaderFinished(); // Release thread reference from cache

	}

	public AsyncImageDownloader(AsyncImageCache parentImageCache) {
		super();
		this.parentImageCache = parentImageCache;
	}
	
	
	

}
