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

import java.util.HashMap;

import android.content.Context;

public class AsyncImageRegistry {
	
	private HashMap<Integer, AsyncImageCache> cacheRegistry;
	
	private static AsyncImageRegistry instance = null;
	
	public static Integer DEFAULT_CACHE = 0;
	public static String DEFAULT_CACHE_DIR = "defaultimagecache";
	
	public boolean initialized;
	
	private Context context;
	
	public static AsyncImageRegistry getInstance() {
		if (instance == null) 
			instance = new AsyncImageRegistry();
		
		return instance;
	}
	
	public AsyncImageRegistry() {
		initialized = false;
		this.cacheRegistry = new HashMap<Integer, AsyncImageCache>();
	}
	
	/**
	 * Get the default AsyncImageCache with file caching off.
	 * 
	 * @return If initialized, returns a working default AsyncImageCache.
	 */
	public AsyncImageCache getDefaultCache() {
		return cacheRegistry.get(DEFAULT_CACHE);
	}

	/**
	 * Get the custom AsyncImageCache registered by registerCache()
	 * 
	 * @param cacheId - Cache ID which was used during registration.
	 * @return If exists, returns the registered AsyncImageCache.
	 */
	public AsyncImageCache getRegisteredCache(Integer cacheId) {
		return cacheRegistry.get(cacheId);
	}
	
	/**
	 * AsyncImageRegistry should be initialized before use!
	 * 
	 * @param appContext - Context required for file caching.
	 */
	public static void initialize(Context appContext) {
		AsyncImageRegistry asyncImage = AsyncImageRegistry.getInstance();
		if (!asyncImage.initialized) {
		asyncImage.context = appContext;
		asyncImage.registerCache(DEFAULT_CACHE, false, DEFAULT_CACHE_DIR);
		asyncImage.initialized = true;
		}
	}
	
	/**
	 * Register a new asynchronous image loading and caching service.
	 * 
	 * @param cacheId - an Integer ID of your cache. Must be different than 0.
	 * Usually specified as a static variable in your main class.
	 * @param fileCacheEnabled - enable file caching? true/false
	 * @param cacheDirName - specify a custom directory name for file cache.
	 */
	public void registerCache(Integer cacheId, boolean fileCacheEnabled, String cacheDirName) {
		if ( ! this.cacheRegistry.containsKey(cacheId) ) // If not registered:
		this.cacheRegistry.put(cacheId, new AsyncImageCache(this.context, fileCacheEnabled, cacheDirName));
	}

	/**
	 * Register a new asynchronous image loading and caching service.
	 * 
	 * @param cacheId - an Integer ID of your cache. Must be different than 0.
	 * Usually specified as a static variable in your main class.
	 * @param fileCacheEnabled - enable file caching? true/false
	 */
	public void registerCache(Integer cacheId, boolean fileCacheEnabled) {
		this.registerCache(cacheId, fileCacheEnabled, "asyncimagecacheid"+cacheId );
	}

}
