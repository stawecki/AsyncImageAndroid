AsyncImage
==========

Asynchronous image loading for Android

Copyright (c) 2011 the OTHER media Limited, written by Mateusz Stawecki

AsyncImage library was re-written based on the previous version of "ImageCache" which I wrote for ECB Cricket Android by the OTHER media. It is now published with the Apache License v2.0 thanks to the OTHER media! More information on the license at: http://www.apache.org/licenses/LICENSE-2.0

Features
--------

* Easy to use Asynchronous and Cached bitmap loading for ImageViews
* Works very well with ListViews and recycled ImageViews !!!
* You can keep using standard ImageViews and there's no extra work on the Adapter
* Optional file caching on device
* Cache instances available from singleton

How to use?
-----------

#### Initialize the cache somewhere at the start e.g. onCreate

     AsyncImageRegistry.initialize(getApplicationContext());

Pass on the context, if you'd like to have file cache support, which is optional.

#### It's probably best if you keep a reference to the cache you want to use, instead of calling every time:

     AsyncImageCache defaultCache = AsyncImageRegistry.getInstance().getDefaultCache();

#### For one lonely ImageView

     ImageView remotePhoto = new ImageView(getApplicationContext());        
     // [...] remotePhoto is added to the current content view...
     defaultCache.loadRemoteImage(remotePhoto, // <- target ImageView
                                  "http://www.othermedia.com/data/images/originals/office-263.jpg", // <- bitmap URL
                                  R.drawable.imageplaceholder); // <- optionally, default resource to show when downloading

#### Multiple cache instances

If you want a separate cache for something:

     // Register it, preferably right after AsyncImageRegistry.initialize(...)
     AsyncImageRegistry.getInstance().registerCache(AsyncImageDemo.AVATARS_CACHE, true);
     											// ^- cache ID number             ^- file cache on?
     
     // Use it anywhere you want
     AsyncImageCache sampleCache = AsyncImageRegistry.getInstance().getRegisteredCache(AsyncImageDemo.AVATARS_CACHE);


#### Example use with ListViews

Two extra steps you need to take when writing your Adapter:

     public class SampleAdapter extends ArrayAdapter<SampleUser> {
     	
     	// ----- 1. Keep a reference to your locally used AsyncImageCache
     	private AsyncImageCache sampleCache = AsyncImageRegistry.getInstance()
     									.getRegisteredCache(AsyncImageDemo.AVATARS_CACHE);
     
     	public View getView(int position, View convertView, ViewGroup parent) {
     		[...]
     		  ImageView thumb = (ImageView) v.findViewById(R.id.thumbnail);
     		  
     	// ----- 2. Actually use it
     		  sampleCache.loadRemoteImage(thumb, // <- target ImageView
     				  this.getItem(position).getThumbnailUrl(), // <- bitmap URL
     				  R.drawable.imageplaceholder); // <- default resource to show when downloading
     		[...]
     	}
     	[...]
     }

Wait, but aren't there downloads clashing and this and that!?
Hopefully not! AsyncImage manages this for you! If not, we need a new item in TODO.

Example app included in `com.othermedia.exampleasyncimage` !

Download
--------

Clone this repo or get the latest JAR from: http://stawecki.com/demos/android/AsyncImage.jar

To Do
-----

There are some improvements coming very soon:

* Better memory management - it's a bit sloppy at the moment
* AsyncImageListner [1]
* File cache cleanup/expiry
* Official unit tests
* File cache default bundle

[1] - for each targeted ImageView receive events based on the status e.g. If you want to use progress bars on top of default placeholders and hide them when the image is being displayed, or do some fancy animations when the image is loaded.

