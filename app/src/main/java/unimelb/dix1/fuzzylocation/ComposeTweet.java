package unimelb.dix1.fuzzylocation;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonObject;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.TimelineResult;

import retrofit2.Call;

public class ComposeTweet extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private SeekBar seekbar;
    private TextView privacyLevel;
    private EditText tweetText;
    private Button sendTweet;
    private ImageButton help;
    private int privacyLv;
    protected GoogleApiClient mGoogleApiClient;
    protected android.location.Location mLastLocation;
    protected Double mLatitude; //actual Latitude
    protected Double mLongitude;
    protected Double FuzLatitude; //Fuzzy Latitude
    protected Double FuzLongitude;

    protected static final String TAG = "ComposeTweet";

    final TwitterSession session = Twitter.getSessionManager().getActiveSession();
    TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient(session);
    final StatusesService statusesService = twitterApiClient.getStatusesService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.compose_tweet);

        help = (ImageButton) findViewById(R.id.help);
        tweetText = (EditText) findViewById(R.id.tweetText);
        sendTweet = (Button) findViewById(R.id.sendTweet);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setProgress(0);

        privacyLevel = (TextView) findViewById(R.id.privacyLV);
        privacyLevel.setText(String.valueOf(seekbar.getProgress()));
        getLocation();


        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                privacyLevel.setText(String.valueOf(seekbar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekbar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekbar) {
                privacyLv = seekbar.getProgress();
                privacyLevel.setText(String.valueOf(privacyLv));
            }
        });

        sendTweet.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                getLocation();
                sendTweet();
            }
        });

        help.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                getHelp();
            }
        });
    }

    public void getHelp(){
        FragmentTransaction mFragTransaction = getFragmentManager().beginTransaction();
        Fragment fragment =  getFragmentManager().findFragmentByTag("fragment_help");
        if(fragment!=null){
            mFragTransaction.remove(fragment);
        }
        HelpFragment help = new HelpFragment();
        help.show(getFragmentManager(), "fragment_help");
    }

    public void sendTweet(){
        FuzzyLocation fuzzyLocation = new FuzzyLocation();
        if (privacyLv == 0){
            FuzLatitude = mLatitude;
            FuzLongitude = mLongitude;
        }else{
//            String txt = "La:"+String.valueOf(mLatitude)+"; Lo:"+String.valueOf(mLongitude);
//            Log.d("privacyLV",txt);
//            Log.d("privacyLV",String.valueOf(privacyLv));
            //fuzzyLocation.doFuzzy(mLatitude,mLongitude,privacyLv);
            fuzzyLocation.doFuzzy2(mLatitude,mLongitude,privacyLv);
            FuzLatitude = fuzzyLocation.getFuzzyLatitude();
            FuzLongitude = fuzzyLocation.getFuzzyLongitude();
        }

        final String text = tweetText.getText().toString();
        //send tweet to Twitter Server
        Call<Tweet> call = statusesService.update(text, null, false,FuzLatitude, FuzLongitude, null, true, false, null);
        call.enqueue(new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                //Do something with result
                Tweet tweet = result.data;
                tweetText.setText("");
                InputMethodManager mInputMethodManager =(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                if (mInputMethodManager.isActive()) {
                    mInputMethodManager.hideSoftInputFromWindow(tweetText.getWindowToken(), 0);
                };

                Toast.makeText(getApplicationContext(), "tweet sent", Toast.LENGTH_SHORT).show();

                // update to CounchDB
                JsonObject json = new JsonObject();
                Long id = tweet.getId();
                Log.d("tweet id",id.toString());
                json.addProperty("_id",id.toString());
                json.addProperty("user",session.getUserName());
                json.addProperty("tweet",text);
                json.addProperty("mLatitude",mLatitude);
                json.addProperty("mLongitude",mLongitude);
                json.addProperty("FuzLatitude",FuzLatitude);
                json.addProperty("FuzLongitude",FuzLongitude);
                json.addProperty("PrivacyLv",privacyLv);
                String jsonStr = json.toString();
                Log.d("json",jsonStr);
                Intent intent = new Intent(ComposeTweet.this, CouchDbService.class);
                intent.putExtra("json",jsonStr);
                startService(intent);
                finish();
            }

            public void failure(TwitterException exception) {
                //Do something on failure
                Toast.makeText(getApplicationContext(), "error in tweeting", Toast.LENGTH_SHORT).show();
                Log.d("postTweet","error in tweeting",exception);
            }
        });
    }

    public void getLocation(){
        //location
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(ComposeTweet.this)
                    .addConnectionCallbacks(ComposeTweet.this)
                    .addOnConnectionFailedListener(ComposeTweet.this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if (mGoogleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(ComposeTweet.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ComposeTweet.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(ComposeTweet.this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
                return;
            }
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                mLatitude = mLastLocation.getLatitude();
                mLongitude = mLastLocation.getLongitude();
//                String latitude =  String.valueOf(mLatitude);
//                String longitude = String.valueOf(mLongitude);
//                String txt = "La:"+latitude+"; Lo:"+longitude;
//                Log.d("getLoca",txt);
                //Toast.makeText(ComposeTweet.this, txt, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ComposeTweet.this, "No Location Detected", Toast.LENGTH_LONG).show();
            }
        }
        else{
            mGoogleApiClient.connect();
        }
    }

    @Override
    //for location
    public void onConnected(Bundle connectionHint) {
        Log.d("location","onConnected");
        getLocation();
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }
}
