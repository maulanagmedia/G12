package id.net.gmedia.gmediatv.Youtube;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import id.net.gmedia.gmediatv.R;
import id.net.gmedia.gmediatv.Utils.GoogleAPI;

public class YoutubeTester extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    //Youtube
    private static YouTubePlayerView ypYoutube;
    private static boolean isYoutube = false;
    private static YouTubePlayer youtubePlayer;
    private static final String masterYoutubeURL = "https://youtu.be/";
    private static final int RECOVERY_REQUEST = 1;
    private static YouTubePlayer.OnInitializedListener youtubeListener;
    private static boolean fullScreen = false;
    private String youtTubeID = "17a87wPqD7Y";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_tester);

        ypYoutube = (YouTubePlayerView) findViewById(R.id.yp_youtube);
        ypYoutube.initialize(GoogleAPI.APIKey, this);
        playVideo(youtTubeID);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        youtubePlayer = youTubePlayer;
        /*player.setPlayerStateChangeListener(playerStateChangeListener);
        player.setPlaybackEventListener(playbackEventListener);*/

        if(isYoutube){

            youTubePlayer.loadVideo(youtTubeID);
            youtubePlayer.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);

            youtubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                @Override
                public void onLoading() {

                    Log.d("Youtube", "onLoading: ");
                }

                @Override
                public void onLoaded(String s) {

                    Log.d("Youtube", "onLoaded: ");
                }

                @Override
                public void onAdStarted() {

                    Log.d("Youtube", "onAdStarted: ");
                }

                @Override
                public void onVideoStarted() {

                }

                @Override
                public void onVideoEnded() {

                    Log.d("Youtube", "onVideoEnded: ");
                }

                @Override
                public void onError(YouTubePlayer.ErrorReason errorReason) {

                    Toast.makeText(YoutubeTester.this, "Channel sudah tidak tersedia", Toast.LENGTH_LONG).show();
                }
            });
        }

        if (!b) {
            //player.loadVideo("fhWaJi1Hsfo"); // Plays https://www.youtube.com/watch?v=fhWaJi1Hsfo
            //youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
            youtubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
                @Override
                public void onFullscreen(boolean b) {
                    fullScreen = b;
                }
            });
        }
    }

    public void playVideo(final String id){

        try {

            youtubePlayer.loadVideo(id);

            youtubePlayer.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);

            youtubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                @Override
                public void onLoading() {

                    Log.d("Youtube", "onLoading: ");
                }

                @Override
                public void onLoaded(String s) {

                    Log.d("Youtube", "onLoaded: ");
                }

                @Override
                public void onAdStarted() {

                    Log.d("Youtube", "onAdStarted: ");
                }

                @Override
                public void onVideoStarted() {

                }

                @Override
                public void onVideoEnded() {

                    Log.d("Youtube", "onVideoEnded: ");
                }

                @Override
                public void onError(YouTubePlayer.ErrorReason errorReason) {

                    Toast.makeText(YoutubeTester.this, "Channel sudah tidak tersedia", Toast.LENGTH_LONG).show();
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

        if (youTubeInitializationResult.isUserRecoverableError()) {
            youTubeInitializationResult.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            String error = String.format(getString(R.string.player_error), youTubeInitializationResult.toString());
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(GoogleAPI.APIKey, this);
        }
    }

    protected static YouTubePlayer.Provider getYouTubePlayerProvider() {
        return ypYoutube;
    }
}
