package id.net.gmedia.gmediatv.Main;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.maulana.custommodul.ApkInstaller;
import com.maulana.custommodul.RuntimePermissionsActivity;

import id.net.gmedia.gmediatv.R;
import id.net.gmedia.gmediatv.Utils.ServerURL;

public class MainMenu extends RuntimePermissionsActivity {

    private static boolean doubleBackToExitPressedOnce;
    private boolean exitState = false;
    private LinearLayout llTV, llYoutube;
    private ImageView ivTV, ivYoutube;
    private static int selectedChoise = 0;
    private static final int REQUEST_PERMISSIONS = 20;
    private LinearLayout llNetflix, llIflix;
    private ImageView ivNetFlix, ivIflix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // for android > M
        if (ContextCompat.checkSelfPermission(
                MainMenu.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainMenu.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainMenu.this, android.Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainMenu.this, android.Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {

            MainMenu.super.requestAppPermissions(new
                            String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WAKE_LOCK, android.Manifest.permission.RECEIVE_BOOT_COMPLETED}, R.string
                            .runtime_permissions_txt
                    , REQUEST_PERMISSIONS);
        }

        doubleBackToExitPressedOnce = false;
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

    @Override
    public void onPermissionsGranted(int requestCode) {

    }

    private void initUI() {

        llTV = (LinearLayout) findViewById(R.id.ll_tv);
        llYoutube = (LinearLayout) findViewById(R.id.ll_yt);
        llNetflix = (LinearLayout) findViewById(R.id.ll_netflix);
        llIflix = (LinearLayout) findViewById(R.id.ll_iflix);
        ivTV = (ImageView) findViewById(R.id.iv_tv);
        ivYoutube = (ImageView) findViewById(R.id.iv_yt);
        ivNetFlix = (ImageView) findViewById(R.id.iv_netflix);
        ivIflix = (ImageView) findViewById(R.id.iv_iflix);


        selectedChoise = 0;
        setHovered(selectedChoise);

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

                try {

                    Intent i = getPackageManager().getLaunchIntentForPackage(ServerURL.pnYoutube);
                    startActivity(i);
                } catch (Exception e) {

                    ApkInstaller atualizaApp = new ApkInstaller();
                    atualizaApp.setContext(MainMenu.this);
                    atualizaApp.execute(ServerURL.bwYoutubeForTV);
                }

                /*Intent intent = new Intent(MainMenu.this, YoutubePlayerActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);*/
            }
        });

        ivNetFlix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    Intent i = getPackageManager().getLaunchIntentForPackage(ServerURL.pnNetflix);
                    startActivity(i);
                } catch (Exception e) {

                    ApkInstaller atualizaApp = new ApkInstaller();
                    atualizaApp.setContext(MainMenu.this);
                    atualizaApp.execute(ServerURL.bwNetflix);
                }
            }
        });

        ivIflix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    Intent i = getPackageManager().getLaunchIntentForPackage(ServerURL.pnIflix);
                    startActivity(i);
                } catch (Exception e) {

                    ApkInstaller atualizaApp = new ApkInstaller();
                    atualizaApp.setContext(MainMenu.this);
                    atualizaApp.execute(ServerURL.bwIflix);
                }
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

        if(!exitState && !doubleBackToExitPressedOnce){
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
        int maxMenu = 4;
        switch (keyCode){
            case 19:
                if(selectedChoise - 2 >= 0){
                    selectedChoise = selectedChoise - 2;
                    setHovered(selectedChoise);
                }
                break;
            case 20:
                if(selectedChoise + 2 < maxMenu){
                    selectedChoise = selectedChoise + 2;
                    setHovered(selectedChoise);
                }
                break;
            case 21:
                if(selectedChoise - 1 >= 0){
                    selectedChoise = selectedChoise - 1;
                    setHovered(selectedChoise);
                }
                break;
            case 22:
                if(selectedChoise + 1 < maxMenu){
                    selectedChoise = selectedChoise + 1;
                    setHovered(selectedChoise);
                }
                break;
            case 23:
                goToSelectedChoise();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setHovered(int number){

        switch (number){
            case 0:
                ivTV.setHovered(true);
                ivYoutube.setHovered(false);
                ivNetFlix.setHovered(false);
                ivIflix.setHovered(false);
                break;
            case 1:
                ivTV.setHovered(false);
                ivYoutube.setHovered(true);
                ivNetFlix.setHovered(false);
                ivIflix.setHovered(false);
                break;
            case 2:
                ivTV.setHovered(false);
                ivYoutube.setHovered(false);
                ivNetFlix.setHovered(true);
                ivIflix.setHovered(false);
                break;
            case 3:
                ivTV.setHovered(false);
                ivYoutube.setHovered(false);
                ivNetFlix.setHovered(false);
                ivIflix.setHovered(true);
                break;
        }
    }

    private void goToSelectedChoise(){

        switch (selectedChoise){
            case 0:
                ivTV.performClick();
                break;
            case 1:
                ivYoutube.performClick();
                break;
            case 2:
                ivNetFlix.performClick();
                break;
            case 3:
                ivIflix.performClick();
                break;
        }
    }
}
