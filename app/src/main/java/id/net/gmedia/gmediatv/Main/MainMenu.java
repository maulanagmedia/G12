package id.net.gmedia.gmediatv.Main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.android.youtube.player.YouTubePlayer;

import id.net.gmedia.gmediatv.R;
import id.net.gmedia.gmediatv.Youtube.YoutubePlayerActivity;

public class MainMenu extends AppCompatActivity {

    private ImageView ivChannel;
    private ImageView ivYoutube;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        initUI();
    }

    private void initUI() {

        ivChannel = (ImageView) findViewById(R.id.iv_channel);
        ivYoutube = (ImageView) findViewById(R.id.iv_youtube);

        initEvent();
    }

    private void initEvent() {

        ivChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainMenu.this, ChanelList.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });

        ivYoutube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainMenu.this, YoutubePlayerActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });
    }
}
