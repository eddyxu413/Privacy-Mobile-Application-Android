package unimelb.dix1.fuzzylocation;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;
import com.google.maps.android.SphericalUtil;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    final TwitterSession session = Twitter.getSessionManager().getActiveSession();
    private GoogleMap mMap;
    private Button showTweet;
    private EditText tweetCounts;
    private ImageButton next;
    private ImageButton previous;
    List<Marker> markers = new ArrayList<Marker>();
    JSONArray allDocs;
    Integer count;
    int index;
    int length;
    JSONObject jsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.maps);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        next = (ImageButton)findViewById(R.id.next);
        previous = (ImageButton)findViewById(R.id.previous);
        showTweet =  (Button) findViewById(R.id.showTweet);
        tweetCounts = (EditText)findViewById(R.id.tweetCounts);
        showTweet.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                index = -1;
                length=0;
                getTweet();
            }
        });
        tweetCounts.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tweetCounts.setText("");
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                nextTweet();
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                previousTweet();
            }
        });
    }

    public void nextTweet(){
        try {
            mMap.clear();
            index = (index + 1) % count;
            JSONObject currentDoc = allDocs.getJSONObject(index).getJSONObject("doc");
            Log.d("next",currentDoc.toString());

            addMarker(currentDoc);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void previousTweet(){
        try {
            mMap.clear();
            JSONObject currentDoc;
            if (index == -1){

                index = 0;
                currentDoc = allDocs.getJSONObject(index).getJSONObject("doc");
                addMarker(currentDoc);
            }else{

                index = (index-1+count) % count;
                currentDoc = allDocs.getJSONObject(index).getJSONObject("doc");
                addMarker(currentDoc);
            }

            Log.d("previous",currentDoc.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void addMarker(JSONObject currentDoc){
        try {

            String text = currentDoc.getString("tweet");
            Integer lv = currentDoc.getInt("PrivacyLv");

            Double mLatitude = currentDoc.getDouble("mLatitude");
            Double mLongitude = currentDoc.getDouble("mLongitude");

            LatLng mLoc = new LatLng(mLatitude,mLongitude);
            Marker accMark =mMap.addMarker(new MarkerOptions().position(mLoc).title("#" + String.valueOf(index + 1)  +" (Privacy Lv."+lv.toString()
                    +")"+ " - "+text));
            accMark.showInfoWindow();

            mMap.moveCamera(CameraUpdateFactory.newLatLng(mLoc));

            if (lv!=0){
                Double FuzLatitude = currentDoc.getDouble("FuzLatitude");
                Double FuzLongitude = currentDoc.getDouble("FuzLongitude");
                LatLng fuzzLoc  = new LatLng(FuzLatitude,FuzLongitude);

                mMap.addMarker(new MarkerOptions().position(fuzzLoc).title("#"+String.valueOf(index+1)+" Lv."+lv.toString()+" Obfuscated Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));


            }

            CameraUpdate cu;
            if (lv==0){
                cu = CameraUpdateFactory.newLatLngZoom(accMark.getPosition(), 14.5F);
            }else {
                double radius = lv*1000.0;
                LatLngBounds bounds = toBounds(mLoc,radius);
                int padding = 50; // offset from edges of the map in pixels
                cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            }
            mMap.animateCamera(cu);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addSimpleMarker(JSONObject currentDoc, int i){
        try {
            //String text = currentDoc.getString("tweet");
            Integer lv = currentDoc.getInt("PrivacyLv");

            Double mLatitude = currentDoc.getDouble("mLatitude");
            Double mLongitude = currentDoc.getDouble("mLongitude");

            LatLng mLoc = new LatLng(mLatitude,mLongitude);

            Marker accMark,fuzzMark;

            if (lv==0){
                accMark=mMap.addMarker(new MarkerOptions().position(mLoc).title("#" + String.valueOf(i + 1)  +" (Privacy Lv."+lv.toString()
                        +")"+ " - Actual Loc"));
            }else {
                accMark=mMap.addMarker(new MarkerOptions().position(mLoc).title("#" + String.valueOf(i + 1) + " - Actual Loc"));
            }
            markers.add(accMark);
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(mLoc));
            if (lv!=0){
                Double FuzLatitude = currentDoc.getDouble("FuzLatitude");
                Double FuzLongitude = currentDoc.getDouble("FuzLongitude");
                LatLng fuzzLoc  = new LatLng(FuzLatitude,FuzLongitude);
                fuzzMark=mMap.addMarker(new MarkerOptions().position(fuzzLoc).title("#"+String.valueOf(i+1)+" Lv."+lv.toString()+" Obfuscated Loc")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE )));
                markers.add(fuzzMark);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public void getTweet(){
        final String input = tweetCounts.getText().toString();
        //Log.d("tweetCounts",input);

        final RequestQueue mQueue = Volley.newRequestQueue(this);
        String dbName = session.getUserName().toLowerCase();
        String url ="http://118.138.246.223:5984/"+dbName+"/_all_docs?include_docs=true&descending=true";
        JsonObjectRequest req = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            allDocs = response.getJSONArray("rows");
                            length = allDocs.length();
                            try{
                                count = Integer.valueOf(input);
                            }catch (NumberFormatException e){
                                count = length;
                            }

                            InputMethodManager mInputMethodManager =(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                            if (mInputMethodManager.isActive()) {
                                mInputMethodManager.hideSoftInputFromWindow(tweetCounts.getWindowToken(), 0);
                            };

                            //Log.d("tweetCounts",count.toString());
                            mMap.clear();
                            markers.clear();
                            for(int i =0; i<count;i++){
                                JSONObject currentDoc = allDocs.getJSONObject(i).getJSONObject("doc");
                                jsonObject= currentDoc;

                                addSimpleMarker(currentDoc,i);


                            }
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            CameraUpdate cu;
                            if (markers.size()==1){
                                cu = CameraUpdateFactory.newLatLngZoom(markers.get(0).getPosition(), 13F);
                            }else {
                                for (Marker marker : markers) {
                                    builder.include(marker.getPosition());
                                }
                                LatLngBounds bounds = builder.build();
                                int padding = 200; // offset from edges of the map in pixels
                                cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                            }
                            mMap.moveCamera(cu);
                            //Log.d("doc",lv.toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });
        mQueue.add(req);
    }

    public LatLngBounds toBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Melbourne and move the camera
        LatLng Melbourne = new LatLng(-37.81319, 144.96298);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Melbourne));
    }
}
