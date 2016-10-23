package unimelb.dix1.fuzzylocation;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterSession;

import org.lightcouch.CouchDbClientAndroid;

public class CouchDbService extends Service {
    private CouchDbClientAndroid dbClient;
    final TwitterSession session = Twitter.getSessionManager().getActiveSession();

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        new Thread(new Runnable(){
            @Override
            public void run(){

                dbClient = new CouchDbClientAndroid(session.getUserName().toLowerCase(), true, "http", DbParams.host, DbParams.port,null,null);
                String jsonStr = intent.getStringExtra("json");
                JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
                dbClient.save(json);
                dbClient.shutdown();
                stopSelf();
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Service","destroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        //return mBinder;
        return null;
    }


}
