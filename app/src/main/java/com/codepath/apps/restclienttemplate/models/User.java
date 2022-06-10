package com.codepath.apps.restclienttemplate.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class User {

    public String name;
    public String screenName;
    public String profileImageUrl;
    public int followingCount;
    public int followersCount;
    public String description;
    public String bannerUrl;

    // empty constructor needed by parceler library
    public User() {}

    public static User fromJson(JSONObject jsonObject) throws JSONException {
        User user = new User();
        user.name = jsonObject.getString("name");
        user.screenName = jsonObject.getString("screen_name");
        user.profileImageUrl = jsonObject.getString("profile_image_url_https");
        user.bannerUrl = jsonObject.getString("profile_banner_url");
        user.followersCount = jsonObject.getInt("followers_count");
        user.followingCount = jsonObject.getInt("friends_count");
        user.description = jsonObject.getString("description");
        return user;
    }
}
