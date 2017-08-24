package id.net.gmedia.gmediatv.Player;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import id.net.gmedia.gmediatv.R;

public class PlayerLibrary extends AppCompatActivity {

    //private JCVideoPlayerStandard jcvPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_library);
        getSupportActionBar().hide();

        initUI();
    }

    private void initUI() {

        //jcvPlayer = (JCVideoPlayerStandard) findViewById(R.id.jcv_player);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            String videoUrl = bundle.getString("url");
            /*jcvPlayer.setUp(videoUrl
                    , JCVideoPlayerStandard.SCREEN_LAYOUT_LIST, "");*/
            //jcvPlayer.startWindowFullscreen();
            //jcvPlayer.thumbImageView.setImage("http://p.qpic.cn/videoyun/0/2449_43b6f696980311e59ed467f22794e792_1/640");
        }
    }

    /*@Override
    public void onBackPressed() {
        if (JCVideoPlayer.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        JCVideoPlayer.releaseAllVideos();
    }*/
}
