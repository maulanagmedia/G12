package id.net.gmedia.gmediatv.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import id.net.gmedia.gmediatv.SplashScreen;

/**
 * Created by Shin on 29/08/2017.
 */

public class CustomReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent myIntent = new Intent(context, SplashScreen.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myIntent);
    }
}
