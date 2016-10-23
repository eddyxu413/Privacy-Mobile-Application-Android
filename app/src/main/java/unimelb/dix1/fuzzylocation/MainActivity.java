package unimelb.dix1.fuzzylocation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.UserTimeline;


import retrofit2.Call;

public class MainActivity extends AppCompatActivity {
    ImageButton location;
    ImageButton logout;
    ImageButton info;
    FloatingActionButton compose;
    TextView ScreenName;
    TextView userName;
    ImageView profileImage;
    ListView tweetList;
    //tweet
    private TweetTimelineListAdapter adapter;
    //private FixedTweetTimeline homeTimeline;
    final TwitterSession session = Twitter.getSessionManager().getActiveSession();
    TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient(session);

    protected static final String TAG = "Main";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_layout);

        location = (ImageButton) findViewById(R.id.location);
        logout = (ImageButton) findViewById(R.id.logout);

        info =(ImageButton) findViewById(R.id.info);
        compose = (FloatingActionButton) findViewById(R.id.compose);

        userName = (TextView) findViewById(R.id.userName);
        userName.setTypeface(null, Typeface.BOLD);
        ScreenName = (TextView) findViewById(R.id.screenName);
        profileImage = (ImageView) findViewById(R.id.profileImage);
        //compose a tweet
        compose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ComposeTweet.class);
                startActivity(intent);
            }
        });
        //show tweet location om map
        location.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                startActivity(intent);

            }
        });

        //info
        info.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, About.class);
                startActivity(intent);
            }
        });
        //logout
        logout.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {

                  Log.d("Logout", "try");
                  logout();
              }
        });

        //timeline
        final UserTimeline userTimeline = new UserTimeline.Builder().screenName(session.getUserName()).build();
        tweetList = (ListView)findViewById(R.id.timeLine);
        adapter = new TweetTimelineListAdapter.Builder(this)
                .setTimeline(userTimeline)
                .build();
        tweetList.setAdapter(adapter);


        //profile image
        final RequestQueue mQueue = Volley.newRequestQueue(this);
        Call<User> call = twitterApiClient.getAccountService().verifyCredentials(true,false);
        call.enqueue(new Callback<User>(){
            @Override
            public void failure(TwitterException e) {
                Log.d("profileImage",e.toString());
                e.printStackTrace();
            }

            @Override
            public void success(Result<User> userResult) {
                //If it succeeds creating a User object from userResult.data
                User user = userResult.data;
                String screenName = user.name;

                ScreenName.setText(screenName);
                userName.setText("@"+session.getUserName());
                //Getting the profile image url
                //String profileImage = user.profileImageUrl.replace("_normal", "");
                String imageUrl = user.profileImageUrl;
                Log.d("image",imageUrl);
                ImageRequest imageRequest = new ImageRequest(
                        imageUrl,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap response) {
                                profileImage.setImageBitmap(response);
                            }
                        }, 0, 0, null, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        profileImage.setImageResource(R.drawable.default_profile);
                    }
                });
                int socketTimeout = 30000;//30 seconds
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                mQueue.add(imageRequest);
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        adapter.refresh(new Callback<TimelineResult<Tweet>>() {
            @Override
            public void success(Result<TimelineResult<Tweet>> result) {
                //swipeLayout.setRefreshing(false);
                Log.d("SendRefresh","success");
            }

            @Override
            public void failure(TwitterException exception) {
                // Toast or some other action
                Log.d("SendRefresh","fail");
            }
        });
    }


    public void logout(){
        AlertDialog.Builder dialog = new AlertDialog.Builder
                (MainActivity.this);

        dialog.setMessage("You will be logout.");

        dialog.setCancelable(true);
        dialog.setPositiveButton("OK", new DialogInterface.
                OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (session != null) {
                    Twitter.getSessionManager().clearActiveSession();
                    Twitter.logOut();
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.
                OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();

    }

}
