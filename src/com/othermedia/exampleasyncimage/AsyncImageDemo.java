package com.othermedia.exampleasyncimage;

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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.othermedia.asyncimage.AsyncImageRegistry;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.Toast;

//http://search.twitter.com/search.json?q=android

public class AsyncImageDemo extends Activity {
	
	public static Integer AVATARS_CACHE = 1;
	
	private ArrayList<SampleTweet> userList;
	SampleAdapter sampleAdapter;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        // -- AsyncImage initialization:
        AsyncImageRegistry.initialize(getApplicationContext());
        
        
        
        /* Example of using AsyncImage with a single ImageView
         * 
        AsyncImageCache defaultCache = AsyncImageRegistry.getInstance().getDefaultCache();        
        ImageView remotePhoto = new ImageView(getApplicationContext());        
        this.setContentView(remotePhoto);
        defaultCache.loadRemoteImage(remotePhoto, // <- target ImageView
				"http://www.othermedia.com/data/images/originals/office-263.jpg", // <- bitmap URL
				 R.drawable.imageplaceholder); // <- optionally, default resource to show when downloading

         */

        // -- Register a special cache just for our Twitter Avatars with file caching "on" 
        AsyncImageRegistry.getInstance().registerCache(AsyncImageDemo.AVATARS_CACHE, true);
        
        // -- Setting up the example:
        userList = new ArrayList<SampleTweet>();
        sampleAdapter = new SampleAdapter(getApplicationContext(), userList);

        ListView list = new ListView(getApplicationContext());
        list.setAdapter(sampleAdapter);
        
        setContentView(list);

        // Get the first portion of Twitter Users talking about Android :)
        (new TwitterDownloader()).execute();
        
        // -- AsyncImage hook up ListView to the cache (for holding up new downloads - it's optional)        
        // AsyncImageRegistry.getInstance().getRegisteredCache(LOGOS_CACHE).installScrollListnerOn(list);
        

        //	Or, do it Manually, if you need a custom OnScrollListener
           list.setOnScrollListener(new OnScrollListener() {
			
        	   // This holds up the Cache's Download Queue 
			public void onScrollStateChanged(AbsListView listView, int scrollState) {
			
				if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) 
					AsyncImageRegistry.getInstance().getRegisteredCache(AVATARS_CACHE).holdQueue();
					
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) 
					AsyncImageRegistry.getInstance().getRegisteredCache(AVATARS_CACHE).resumeQueue();
			}
			
			  // This loads more items once the ListView reaches the bottom
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				
				if (firstVisibleItem+visibleItemCount == totalItemCount &&
						currentPage < 20 && !downloaderRunning) {
					currentPage++;
					(new TwitterDownloader()).execute();
				}
			}

		});
        
        Toast.makeText(getApplicationContext(), "Loading...", 1000).show();
    }
    
    private boolean downloaderRunning = false;
	private int currentPage = 1;

    
   private class TwitterDownloader extends AsyncTask<Void, Void, Void> {
	   
	   private ArrayList<SampleTweet> downloadedUsers;

	@Override
	protected Void doInBackground(Void... arg) {
        downloaderRunning = true;
		
		try {
			JSONObject twitterSearch = new JSONObject (
					AsyncImageDemo.stringFromURL("http://search.twitter.com/search.json?q=android&page="+currentPage) );
			//
			downloadedUsers = new ArrayList<SampleTweet>();
			
			JSONArray twitterResults = twitterSearch.getJSONArray("results");
			
			for (int i = 0, len = twitterResults.length(); i < len; i++) {
				downloadedUsers.add(new SampleTweet(twitterResults.getJSONObject(i)));
			}						
			
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(), e.getMessage());
		}
		
		return null;
	}
	
	protected void onPostExecute(Void arg) {
		downloaderRunning = false;
		if (downloadedUsers != null) {
		userList.addAll(downloadedUsers);
		sampleAdapter.notifyDataSetChanged();
        Toast.makeText(getApplicationContext(), "Scroll down for more", 500).show();

		}
	}
	   
   }
    
    
  public static String stringFromURL(String address) throws Exception {
    	
		StringBuilder result = new StringBuilder();
        URL url = new URL(address);
		HttpGet httpRequest = new HttpGet(url.toURI());
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);

        BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(response.getEntity());
        InputStream input = bufHttpEntity.getContent();

        BufferedReader reader = new BufferedReader(new InputStreamReader(input,"UTF-8"));

        String line;
        while ((line = reader.readLine()) != null) {
      	  result.append(line);
        }
        reader.close();

        return result.toString();	
    }
  
  
}