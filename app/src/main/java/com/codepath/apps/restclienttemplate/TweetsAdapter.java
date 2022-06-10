package com.codepath.apps.restclienttemplate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    Context context;
    List<Tweet> tweets;

    // pass in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    // for each row, inflate the layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    // bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get data at position
        Tweet tweet = tweets.get(position);

        // bind the tweet with view holder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }


    // clean all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // add a list of items
    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }

    // define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        ImageView ivTweetImage;
        TextView tvRelativeDate;
        TextView tvName;
        TextView tvFavCount;
        ImageButton ibFav;
        ImageButton ibReply;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvName = itemView.findViewById(R.id.tvName);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            ivTweetImage = itemView.findViewById(R.id.ivTweetImage);
            tvRelativeDate = itemView.findViewById(R.id.tvRelativeDate);
            ibFav = itemView.findViewById(R.id.ibFav);
            tvFavCount = itemView.findViewById(R.id.tvFavCount);
            ibReply = itemView.findViewById(R.id.ibReply);
        }

        public void bind(Tweet tweet) {
            tvBody.setText(tweet.body);
            tvScreenName.setText("@" + tweet.user.screenName);
            tvName.setText(tweet.user.name);

            // set favorite count and icon
            tvFavCount.setText(String.valueOf(tweet.favCount));
            if (tweet.isFavorited) {
                Drawable filled_heart = context.getDrawable(R.drawable.ic_vector_heart);
                ibFav.setImageDrawable(filled_heart);
            } else {
                Drawable empty_heart = context.getDrawable(R.drawable.ic_vector_heart_stroke);
                ibFav.setImageDrawable(empty_heart);
            }
            Glide.with(context).load(tweet.user.profileImageUrl)
                    .transform(new RoundedCorners(400))
                    .into(ivProfileImage);
            // render tweet image
            if (!((tweet.imageUrl).equals(""))) {
                Log.i("TweetsAdapter", "image: " + tweet.imageUrl);
                Log.i("TweetsAdapter", "tweet body: " + tweet.body);
                // render tweet image and set visible
                Glide.with(context).load(tweet.imageUrl)
                        .transform(new RoundedCorners(100))
                        .into(ivTweetImage);
                ivTweetImage.setVisibility(View.VISIBLE);
            } else {
                ivTweetImage.setVisibility(View.GONE);
            }

            // set relative date
            tvRelativeDate.setText(" \u2022 " + getRelativeTimeAgo(tweet.dateCreated));

            // like button listener
            ibFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!tweet.isFavorited) {
                        // tell Twitter I want to favorite this
                        TwitterApp.getRestClient(context).favorite(tweet.id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.i("TweetsAdapter", "this should've been favorited");
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.e("TweetsAdapter", response);
                            }
                        });

                        // change the drawable to filled heart
                        Drawable filled_heart = context.getDrawable(R.drawable.ic_vector_heart);
                        ibFav.setImageDrawable(filled_heart);
                        tweet.isFavorited = true;

                        // increment the text inside tvFavCount
                        tweet.favCount++;

                    } else {
                        // tell Twitter I want to unfavorite this
                        TwitterApp.getRestClient(context).unfavorite(tweet.id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.i("TweetsAdapter", "this should've been unfavorited");
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.e("TweetsAdapter", response);
                            }
                        });

                        // change drawable to empty heart
                        Drawable empty_heart = context.getDrawable(R.drawable.ic_vector_heart_stroke);
                        ibFav.setImageDrawable(empty_heart);
                        tweet.isFavorited = false;

                        // decrement text inside tvFavCount
                        tweet.favCount--;

                    }
                    tvFavCount.setText(String.valueOf(tweet.favCount));
                }
            });

            // reply button listener
            ibReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // pop up a compose screen
                    // it's gonna be a brand new tweet, but it'll have an extra attribute
                        // extra attribute: "in_reply_to_status_id"

                    Intent i = new Intent(context, ComposeActivity.class);
                    i.putExtra("tweet_to_reply_to", Parcels.wrap(tweet));
                    ((Activity) context).startActivityForResult(i, TimelineActivity.REQUEST_CODE);
                }
            });
        }

    }

    // code from https://gist.github.com/nesquena/f786232f5ef72f6e10a7
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis, System.currentTimeMillis(),
                    DateUtils.SECOND_IN_MILLIS).toString();

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return relativeDate;
    }


}
