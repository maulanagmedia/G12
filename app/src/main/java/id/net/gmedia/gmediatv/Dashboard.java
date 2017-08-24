package id.net.gmedia.gmediatv;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import id.net.gmedia.gmediatv.Player.PlayerLibrary;
import id.net.gmedia.gmediatv.Player.PlayerVideoView;

public class Dashboard extends AppCompatActivity {

    private Button btn1, btn2, btn3;
    private final String contohRTSP = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_175k.mov";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        setContentView(R.layout.activity_dashboard);

        initUI();
    }

    private void initUI() {

        btn1 = (Button) findViewById(R.id.btn_1);
        btn2 = (Button) findViewById(R.id.btn_2);
        btn3 = (Button) findViewById(R.id.btn_3);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, PlayerVideoView.class);
                intent.putExtra("url", contohRTSP);
                startActivity(intent);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(contohRTSP)));
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, PlayerLibrary.class);
                intent.putExtra("url", contohRTSP);
                startActivity(intent);

                //JCVideoPlayerStandard.startFullscreen(Dashboard.this, JCVideoPlayerStandard.class, contohRTSP, "");
            }
        });
    }
}
