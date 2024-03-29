package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Tweet {

    public String body;
    public String createdAt;
    public User user;
    public String imageUrl;
    public String dateCreated;
    public String id;
    public boolean isFavorited;
    public boolean isRetweeted;
    public int favCount;
    public int retweetedCount;

    // empty constructor needed by parceler library
    public Tweet() {}

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        if (jsonObject.has("retweeted_status")) {
            return null;
        }

        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        JSONObject entities = jsonObject.getJSONObject("entities");
        tweet.dateCreated = jsonObject.getString("created_at");
        tweet.id = jsonObject.getString("id_str");
        tweet.isFavorited = jsonObject.getBoolean("favorited");
        tweet.isRetweeted = jsonObject.getBoolean("retweeted");
        tweet.favCount = jsonObject.getInt("favorite_count");
        tweet.retweetedCount = jsonObject.getInt("retweet_count");

        // get image url if one exists
        tweet.imageUrl = "";
        if (entities.has("media")) {
            JSONArray mediaArray = entities.getJSONArray("media");
            if (mediaArray.length() != 0) {
                JSONObject mediaObj = mediaArray.getJSONObject(0);
                Log.i("Tweet", mediaObj.toString());
                if (mediaObj.getString("type").equals("photo")) {
                    tweet.imageUrl = mediaObj.getString("media_url_https");
                    Log.i("Tweet", tweet.imageUrl);
                }
            }
        }
        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Tweet tweet = fromJson(jsonArray.getJSONObject(i));
            if (tweet != null) { // skip retweets
                tweets.add(tweet);
            }
        }
        return tweets;
    }
}
