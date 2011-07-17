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

import java.util.List;

import com.othermedia.asyncimage.AsyncImageCache;
import com.othermedia.asyncimage.AsyncImageRegistry;
import com.othermedia.asyncimage.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SampleAdapter extends ArrayAdapter<SampleTweet> {
	
	private AsyncImageCache sampleCache = AsyncImageRegistry.getInstance()
									.getRegisteredCache(AsyncImageDemo.AVATARS_CACHE);
	
	public SampleAdapter(Context context, 
			List<SampleTweet> objects) {
		super(context, 0, objects);

	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		  View v = convertView;

		  if (v == null) {
		         LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		         v = vi.inflate(R.layout.tweetcell, null);
		  }
		  
		  ( (TextView) v.findViewById(R.id.username) ).setText( this.getItem(position).getUsername() );
		  ( (TextView) v.findViewById(R.id.text) ).setText( this.getItem(position).getTweet() );

		  ImageView thumb = (ImageView) v.findViewById(R.id.thumbnail);
		  
		  sampleCache.loadRemoteImage(thumb, // <- target ImageView
				  this.getItem(position).getThumbnailUrl(), // <- bitmap URL
				  R.drawable.imageplaceholder); // <- default resource to show when downloading
		
		return v;
	}

}
