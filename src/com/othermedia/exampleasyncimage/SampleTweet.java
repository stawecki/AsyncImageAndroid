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

import org.json.JSONException;
import org.json.JSONObject;

public class SampleTweet {

	private String username;
	private String thumbnailUrl;
	private String tweet;
	
	public SampleTweet(String username, String thumbnailUrl, String tweet) {
		super();
		this.username = username;
		this.thumbnailUrl = thumbnailUrl;
		this.tweet = tweet;
	}
	
	public SampleTweet(JSONObject obj) throws JSONException {
		super();
		this.username = obj.getString("from_user");
		this.thumbnailUrl = obj.getString("profile_image_url");
		this.tweet = obj.getString("text");
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUsername() {
		return username;
	}
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public String getTweet() {
		return tweet;
	}

	public void setTweet(String tweet) {
		this.tweet = tweet;
	}
	
	
}
