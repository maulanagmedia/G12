package id.net.gmedia.gmediatv;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import id.net.gmedia.gmediatv.Main.ChannelViewScreen;
import id.net.gmedia.gmediatv.Main.MainMenu;

public class SplashScreen extends AppCompatActivity {

    private static boolean splashLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (!splashLoaded) {
            int secondsDelayed = 2;
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    //startActivity(new Intent(SplashScreen.this, DaftarVideo.class));
                    Intent intent = new Intent(SplashScreen.this, MainMenu.class);
                    intent.putExtra("splashed", true);
                    startActivity(intent);
                    finish();
                }
            }, secondsDelayed * 1000);

            //splashLoaded = true;
        }
        else {
            //Intent goToMainActivity = new Intent(SplashScreen.this, DaftarVideo.class);
            Intent goToMainActivity = new Intent(SplashScreen.this, MainMenu.class);
            goToMainActivity.putExtra("splashed", true);
            goToMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(goToMainActivity);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
