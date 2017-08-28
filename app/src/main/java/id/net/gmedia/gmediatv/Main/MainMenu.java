package id.net.gmedia.gmediatv.Main;

import android.content.Intent;
import android.media.Image;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubePlayer;

import id.net.gmedia.gmediatv.R;
import id.net.gmedia.gmediatv.Youtube.YoutubePlayerActivity;

public class MainMenu extends AppCompatActivity {

    private boolean doubleBackToExitPressedOnce = false, exitState = false;
    private LinearLayout llTV, llYoutube;
    private ImageView ivTV, ivYoutube;
    private static int selectedChoise = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            if(bundle.getBoolean("exit", false)){
                exitState = true;
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
            }
        }
        initUI();
    }

    private void initUI() {

        llTV = (LinearLayout) findViewById(R.id.ll_tv);
        llYoutube = (LinearLayout) findViewById(R.id.ll_yt);
        ivTV = (ImageView) findViewById(R.id.iv_tv);
        ivYoutube = (ImageView) findViewById(R.id.iv_yt);

        selectedChoise = 0;
        ivYoutube.setHovered(true);
        ivYoutube.setHovered(false);

        initEvent();
    }

    private void initEvent() {

        ivTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainMenu.this, ChanelList.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        ivYoutube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainMenu.this, YoutubePlayerActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {

            Intent intent = new Intent(MainMenu.this, MainMenu.class);
            intent.putExtra("exit", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            //System.exit(0);
        }

        if(!exitState){
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getResources().getString(R.string.app_exit), Toast.LENGTH_SHORT).show();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case 21:
                selectedChoise = 0;
                ivTV.setHovered(true);
                ivYoutube.setHovered(false);
                break;
            case 22:
                selectedChoise = 1;
                ivYoutube.setHovered(true);
                ivTV.setHovered(false);
                break;
            case 23:
                goToSelectedChoise();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void goToSelectedChoise(){

        if(selectedChoise == 0){

            ivTV.performClick();
        }else{
            ivYoutube.performClick();
        }
    }
}
