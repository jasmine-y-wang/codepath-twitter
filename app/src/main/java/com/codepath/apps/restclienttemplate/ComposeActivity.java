package com.codepath.apps.restclienttemplate;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {

    public static final String TAG = "ComposeActivity";
    public static final int MAX_TWEET_LENGTH = 140;
    EditText etCompose;
    Button btnTweet;
    TwitterClient client;
    JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Headers headers, JSON json) {
            Log.i(TAG, "onSuccess to publish tweet");
            try {
                Tweet tweet = Tweet.fromJson(json.jsonObject);
                Log.i(TAG, "published tweet says: " + tweet.body);
                Intent intent = new Intent();
                intent.putExtra("tweet", Parcels.wrap(tweet));
                // set result code & bundle data for response
                setResult(RESULT_OK, intent);
                // close activity, pass data to parent
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
            Log.e(TAG, "on failure", throwable);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        client = TwitterApp.getRestClient(this);

        etCompose = findViewById(R.id.etCompose);
        btnTweet = findViewById(R.id.btnTweet);

        // set click listener on button
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tweetContent = etCompose.getText().toString();

                if (tweetContent.isEmpty()) {
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet cannot be empty :(", Toast.LENGTH_LONG).show();
                    return;
                }
                if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet is too long :(", Toast.LENGTH_LONG).show();
                }
                // make API call to twitter to publish the tweet
                Toast.makeText(ComposeActivity.this, "Sent!", Toast.LENGTH_LONG).show();

                if (getIntent().hasExtra("tweet_to_reply_to")) {
                    Tweet tweetToReplyTo = Parcels.unwrap(getIntent().getParcelableExtra("tweet_to_reply_to"));
                    String tweetToReplyToId = tweetToReplyTo.id;
                    String tweetToReplyToUserScreenName = tweetToReplyTo.user.screenName;
                    client.replyToTweet(tweetToReplyToId, "@" + tweetToReplyToUserScreenName + " " + tweetContent, handler);
                } else {
                    client.publishTweet(tweetContent, handler);
                }
            }
        });
    }
}