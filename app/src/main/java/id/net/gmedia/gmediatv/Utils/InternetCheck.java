package id.net.gmedia.gmediatv.Utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Shinmaul on 9/28/2017.
 */

public class InternetCheck extends AsyncTask<Void, Void, Void> {


    private Activity activity;
    private InternetCheckListener listener;

    public InternetCheck(Activity x){

        activity= x;

    }

    @Override
    protected Void doInBackground(Void... params) {


        boolean b = hasInternetAccess();
        listener.onComplete(b);

        return null;
    }


    public void isInternetConnectionAvailable(InternetCheckListener x){
        listener=x;
        execute();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    private boolean hasInternetAccess() {
        if (isNetworkAvailable()) {
            /*try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL(ServerURL.testConnection).openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setRequestMethod("GET");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                Log.d("Connection test: ", "hasInternetAccess: "+ urlc.getResponseMessage()+" "+urlc.getResponseCode());
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL(ServerURL.testConnection).openConnection());
                try {
                    InputStream in = new BufferedInputStream(urlc.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        return true;
                    }
                } finally {
                    urlc.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Log.d("TAG", "No network available!");
        }
        return false;
    }

    public interface InternetCheckListener{
        void onComplete(boolean connected);
    }

}