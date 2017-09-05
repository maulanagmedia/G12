package id.net.gmedia.gmediatv.Utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import id.net.gmedia.gmediatv.RemoteUtils.ServiceUtils;

/**
 * Created by Shin on 05/09/2017.
 */

public class ServiceTimerServer extends Service{

        private static Timer timer = new Timer();
        private Context ctx;
        private int timerTtime = 1 * 60 * 1000; // 1 min

        public IBinder onBind(Intent arg0)
        {
            return null;
        }

        public void onCreate()
        {
            super.onCreate();
            ctx = this;
            startService();
        }

        private void startService()
        {
            timer.scheduleAtFixedRate(new mainTask(), 0, 1000);
        }

        private class mainTask extends TimerTask
        {
            public void run()
            {
                ServiceUtils.lockedClient = "";
            }
        }

        public void onDestroy()
        {
            super.onDestroy();
            //Toast.makeText(this, "Service Stopped ...", Toast.LENGTH_SHORT).show();
        }

}
