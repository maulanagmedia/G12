package id.net.gmedia.gmediatv.Player;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import id.net.gmedia.gmediatv.R;

public class PlayerVideoView extends AppCompatActivity {

    private VideoView vvPlayVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_video_view);
        getSupportActionBar().hide();
        initUI();
    }

    private void initUI() {

        vvPlayVideo = (VideoView) findViewById(R.id.vv_stream);
        MediaController mediaController = new MediaController(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            String videoUrl = bundle.getString("url");
            mediaController.setAnchorView(vvPlayVideo);
            Uri uri = Uri.parse(videoUrl);
            vvPlayVideo.setVideoURI(uri);
            vvPlayVideo.setMediaController(mediaController);
            vvPlayVideo.requestFocus();

            vvPlayVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {

                    mp.start();
                    mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {

                        @Override
                        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

                            mp.start();
                        }
                    });
                }
            });
        }
    }
}
