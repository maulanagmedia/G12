package id.net.gmedia.gmediatv.Main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.ApkInstaller;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ImageUtils;
import com.maulana.custommodul.ItemValidation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import id.net.gmedia.gmediatv.Main.Adapter.ListChanelAdapter;
import id.net.gmedia.gmediatv.R;
import id.net.gmedia.gmediatv.RemoteUtils.ServiceUtils;
import id.net.gmedia.gmediatv.Utils.CustomVideoView;
import id.net.gmedia.gmediatv.Utils.DeviceInfo;
import id.net.gmedia.gmediatv.Utils.FormatItem;
import id.net.gmedia.gmediatv.Utils.InternetCheck;
import id.net.gmedia.gmediatv.Utils.SavedChanelManager;
import id.net.gmedia.gmediatv.Utils.ServerURL;

public class ChannelViewScreen extends AppCompatActivity {

    private static CustomVideoView vvPlayVideo;
    private ItemValidation iv = new ItemValidation();
    private boolean showNavigator = false;
    private int timerTime = 10; // in second
    private final int timerAdsChecker = 5; // in minutes
    private int animationDuration = 800; // in milisecond
    private TextView tvVolume;
    private SeekBar sbVolume;
    private AudioManager audioManager;
    private SearchView edtSearch;
    private TextView tvSearch;
    private RelativeLayout rvListVideoContainer;
    private ListView lvChanel;
    private List<CustomItem> masterList;
    private SavedChanelManager savedChanel;
    private boolean itemOnSelect = false;
    private int delayTime = 5000; // Delay before hide the view
    private int channelTime = 1600; // Delay before hide the view
    private ImageView ivUp, ivDown;
    private int invervalHolding = 500;
    private int intervalMove = 200;
    private Handler handlerUp = new Handler(), handlerDown = new Handler();
    private Runnable handlerRunnableUp = new Runnable() {
        @Override
        public void run() {
            handlerUp.postDelayed(this, intervalMove);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    scrollListViewToUp();
                }
            });
        }
    };
    private boolean tapped = false;

    private Runnable handlerRunnableDown = new Runnable() {
        @Override
        public void run() {
            handlerDown.postDelayed(this, intervalMove);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    scrollListViewToDown();
                }
            });
        }
    };
    private ProgressBar pbLoading;
    private LinearLayout llYoutubeContainer;
    private static boolean isTypeChannel;
    private LinearLayout llChannelSelector;
    private TextView tvChannelSelector;
    private Timer timer;
    private boolean buttonOnYoutube = false;
    private static int lastPositionChannel = 0;

    //For remote
    private NsdManager mNsdManager;
    private ServerSocket serverSocket;
    private SocketServerThread socketServerThread;
    private final String TAG = "Chanel";
    private RelativeLayout rvScreenContainer;
    private WebView wvAds;
    private List<CustomItem> adsList;
    private boolean isLoad = true;
    private WifiManager wifi;
    private double scaleVideo = 1;
    private boolean isFullScreen = true;
    private boolean isTimerPaused = false;
    private ImageView ivLogoTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view_screen);

        isTypeChannel = false;
        isTimerPaused = false;
        scaleVideo = 1;
        isFullScreen = true;
        savedChanel = new SavedChanelManager(ChannelViewScreen.this);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            String nama = bundle.getString("nama");
            String link = bundle.getString("link");
            if(nama != null && link != null) savedChanel.saveLastChanel(nama,link);
        }

        initUI();

        // For Remote access
        //ServiceUtils.DEFAULT_PORT = ConnectionUtil.getPort(ServerActivity.this);
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        registerService(ServiceUtils.DEFAULT_PORT);
        initializeReceiver();
    }

    private void initUI() {

        tvVolume = (TextView) findViewById(R.id.tv_volume);
        sbVolume = (SeekBar) findViewById(R.id.sb_volume);
        vvPlayVideo = (CustomVideoView) findViewById(R.id.vv_stream);
        llYoutubeContainer = (LinearLayout) findViewById(R.id.ll_youtube_container);
        lvChanel = (ListView) findViewById(R.id.lv_chanel);
        rvListVideoContainer = (RelativeLayout) findViewById(R.id.rl_list_chanel);
        ivUp = (ImageView) findViewById(R.id.iv_up);
        ivLogoTV = (ImageView) findViewById(R.id.iv_tv_logo);
        ivDown = (ImageView) findViewById(R.id.iv_down);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        llChannelSelector = (LinearLayout) findViewById(R.id.ll_channel_selector);
        tvChannelSelector = (TextView) findViewById(R.id.tv_channel_selector);
        rvScreenContainer = (RelativeLayout) findViewById(R.id.rv_screen_container);
        wvAds = (WebView) findViewById(R.id.wv_ads);
        wvAds.setWebViewClient(new WebViewClient());

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        sbVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        tapped = true;
        showNavigationItem();
        if(savedChanel.isSaved()) playVideo(savedChanel.getNama(), savedChanel.getLink());
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //getDataWithConnection();

        vvPlayVideo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                itemOnSelect = false;
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    tapped = true;
                    showNavigationItem();
                }

                return true;
            }
        });

        sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                tvVolume.setText(String.valueOf(i));
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        llYoutubeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                redirrectToYoutube();
                /*Intent intent = new Intent(ChannelViewScreen.this, YoutubePlayerActivity.class);
                startActivity(intent);*/
            }
        });

        lvChanel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                itemOnSelect = true;
                tapped = true;
                showNavigationItem();
                return false;
            }
        });

        ivUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                itemOnSelect = true;
                tapped = true;
                showNavigationItem();

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        handlerUp.removeCallbacks(handlerRunnableUp);
                        handlerUp.postDelayed(handlerRunnableUp, invervalHolding);
                        scrollListViewToUp();
                        return true;
                    case MotionEvent.ACTION_UP:
                        handlerUp.removeCallbacks(handlerRunnableUp);
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        handlerUp.removeCallbacks(handlerRunnableUp);
                        return true;
                }

                return false;
            }
        });

        ivDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                itemOnSelect = true;
                tapped = true;
                showNavigationItem();

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        handlerDown.removeCallbacks(handlerRunnableDown);
                        handlerDown.postDelayed(handlerRunnableDown, invervalHolding);
                        scrollListViewToDown();
                        return true;
                    case MotionEvent.ACTION_UP:
                        handlerDown.removeCallbacks(handlerRunnableDown);
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        handlerDown.removeCallbacks(handlerRunnableDown);
                        return true;
                }

                return false;
            }
        });

        //getAds();
        setTimerAds();
    }

    private void getDataWithConnection() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        //return activeNetworkInfo != null && activeNetworkInfo.isConnected();

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            new InternetCheck(ChannelViewScreen.this).isInternetConnectionAvailable(new InternetCheck.InternetCheckListener() {

                @Override
                public void onComplete(boolean connected) {
                    if(connected){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getLogo();
                                getLinkRTSP();
                                getAds();
                            }
                        });
                    }else{
                        Snackbar.make(findViewById(android.R.id.content), R.string.wifi_not_connected,
                                Snackbar.LENGTH_INDEFINITE).setAction("OK",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (wifi.isWifiEnabled() == false)
                                        {
                                            wifi.setWifiEnabled(true);
                                        }
                                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                    }
                                }).show();
                    }
                }
            });
        } else {
            Log.d("conect error", "No network available!");
        }
    }

    private void redirrectToYoutube(){

        try {

            Intent i = getPackageManager().getLaunchIntentForPackage(ServerURL.pnYoutube);
            startActivity(i);
        } catch (Exception e) {

            ApkInstaller atualizaApp = new ApkInstaller();
            atualizaApp.setContext(ChannelViewScreen.this);
            atualizaApp.execute(ServerURL.bwYoutubeForTV);
        }
    }

    private void scrollListViewToUp() {
        if(lvChanel.getAdapter() != null){

            final int firstPosition =  lvChanel.getFirstVisiblePosition();
            lvChanel.post(new Runnable() {
                @Override
                public void run() {
                    // Select the last row so it will scroll into view...
                    if(firstPosition != 0) lvChanel.setSelection(firstPosition - 1);
                }
            });
        }
    }

    private void scrollListViewToDown() {
        if(lvChanel.getAdapter() != null){

            final int firstPosition =  lvChanel.getFirstVisiblePosition();
            final int lastPosition =  lvChanel.getLastVisiblePosition();
            lvChanel.post(new Runnable() {
                @Override
                public void run() {
                    // Select the last row so it will scroll into view...
                    if(lastPosition != lvChanel.getAdapter().getCount() - 1) lvChanel.setSelection(firstPosition + 1);
                }
            });
        }
    }

    private void showNavigationItem(){

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if(rvListVideoContainer.getVisibility() == View.GONE){
                    rvListVideoContainer.setVisibility(View.VISIBLE);
                    rvListVideoContainer.animate()
                            .translationY(0)
                            .alpha(1.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                }
                            });
                }else if(rvListVideoContainer.getVisibility() == View.VISIBLE && !itemOnSelect){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            rvListVideoContainer.clearAnimation();
                            rvListVideoContainer.animate()
                                    .translationY(0)
                                    .alpha(0.0f)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            rvListVideoContainer.setVisibility(View.GONE);
                                        }
                                    });
                        }
                    });
                }
            }
        });
    }

    private void getAds() {

        JSONObject jbody = new JSONObject();
        adsList = new ArrayList<>();

        try {
            jbody.put("mac", FormatItem.getMacAddress());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley apiVolley = new ApiVolley(ChannelViewScreen.this, jbody, "POST", ServerURL.getAds, "", "", 0, new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200) {

                        JSONArray jsonArray = response.getJSONArray("response");
                        for(int i = 0; i < jsonArray.length();i++){
                            JSONObject jo = jsonArray.getJSONObject(i);
                            adsList.add(new CustomItem(jo.getString("id"), jo.getString("link"), jo.getString("showing_duration"), jo.getString("scale_screen")));
                        }

                        setTimerAds();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {

            }
        });
    }

    private void getLogo() {

        JSONObject jbody = new JSONObject();
        adsList = new ArrayList<>();

        try {
            jbody.put("mac", FormatItem.getMacAddress());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley apiVolley = new ApiVolley(ChannelViewScreen.this, jbody, "POST", ServerURL.getLogo, "", "", 0, new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200) {

                        String linkLogo = response.getJSONObject("response").getString("link");
                        if(linkLogo.length() > 0){
                            ImageUtils iu = new ImageUtils();
                            iu.LoadGIFImage(ChannelViewScreen.this, linkLogo, ivLogoTV, R.mipmap.ic_launcher);
                            //iu.LoadRealImage(ChannelViewScreen.this, linkLogo,ivLogoTV);
                            //Glide.with(ChannelViewScreen.this).load(Uri.parse(linkLogo)).error(R.mipmap.ic_launcher).placeholder(R.mipmap.ic_launcher).into(ivLogoTV);
                        }

                        setTimerAds();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {

            }
        });
    }

    private void setTimerAds(){

        if(isLoad){
            isLoad = false;
            Timer timerAds = new Timer();
            timerAds.schedule(new TimerTask() {
                @Override
                public void run() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if(adsList != null && adsList.size() > 0 && !isTimerPaused){
                                isTimerPaused = true;
                                CustomItem item =  adsList.get(0);
                                int duration = iv.parseNullInteger(item.getItem3());
                                double scale = iv.parseNullFloat(item.getItem4());
                                setVideoCOntainerScale1(false, scale);
                                playAdsTimer(duration,item.getItem2(), scale);
                            }else if(!isTimerPaused){
                                getAds();
                            }
                        }
                    });
                }
            },(timerAdsChecker * 60 * 1000) , (timerAdsChecker * 60 * 1000));
        }
    }

    private void playAdsTimer(int Seconds, String url, final double scale){

        wvAds.setWebViewClient(new WebViewClient());
        wvAds.loadUrl(url);
        new CountDownTimer(Seconds* 1000+1000, 1000) {

            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
            }

            public void onFinish() {

                if(adsList != null){
                    adsList.remove(0);

                    if(adsList.size() > 0){
                        CustomItem nowAds = adsList.get(0);
                        int duration = iv.parseNullInteger(nowAds.getItem3());
                        String adsURL = nowAds.getItem2();
                        playAdsTimer(duration, adsURL, scale);
                    }else{
                        backNormalScale1(1, scale);
                        isTimerPaused = false;
                    }
                }else{
                    isTimerPaused = false;
                }
            }
        }.start();
    }

    private void getLinkRTSP() {

        JSONObject jbody = new JSONObject();

        try {
            jbody.put("mac", FormatItem.getMacAddress());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley apiVolley = new ApiVolley(ChannelViewScreen.this, jbody, "POST", ServerURL.getLink, "", "", 0, new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                masterList = new ArrayList<>();
                int x = 0;
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200) {

                        JSONArray jsonArray = response.getJSONArray("response");
                        for(int i = 0; i < jsonArray.length();i++){
                            JSONObject jo = jsonArray.getJSONObject(i);
                            masterList.add(new CustomItem(jo.getString("id"), jo.getString("nama"), jo.getString("link")));

                            if(i == 0 && !savedChanel.isSaved()){
                                playVideo(jo.getString("nama"), jo.getString("link"));
                            }else if(savedChanel.isSaved() && jo.getString("link").trim().equals(savedChanel.getLink().trim())&& jo.getString("nama").trim().equals(savedChanel.getNama().trim())){
                                x = i;
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                setListChanel(masterList, x);
            }

            @Override
            public void onError(String result) {

                setListChanel(null, 0);
            }
        });
    }

    private void setListChanel(List<CustomItem> listItem, int saved){

        lvChanel.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            final ListChanelAdapter adapter = new ListChanelAdapter(ChannelViewScreen.this, listItem);
            lvChanel.setAdapter(adapter);
            ListChanelAdapter.selectedPosition = saved;
            lvChanel.setSelection(saved);
            adapter.notifyDataSetChanged();
            lvChanel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem item = (CustomItem) adapterView.getItemAtPosition(i);
                    ListChanelAdapter.selectedPosition = i;
                    adapter.notifyDataSetChanged();
                    playVideo(item.getItem2(),item.getItem3());
                }
            });
        }
    }

    private void playVideo(String nama, String url){

        vvPlayVideo.stopPlayback();
        vvPlayVideo.clearAnimation();
        vvPlayVideo.suspend();
        vvPlayVideo.setVideoURI(null);

        pbLoading.setVisibility(View.VISIBLE);
        /*MediaController mediaController = new MediaController(ChannelViewScreen.this);
        mediaController.setAnchorView(vvPlayVideo);*/

        Uri uri = Uri.parse(url);
        savedChanel.saveLastChanel(nama, url);
        vvPlayVideo.setVideoURI(uri);
        //vvPlayVideo.setMediaController(mediaController);
        vvPlayVideo.requestFocus();
        //vvPlayVideo.seekTo(100);

        /*if(masterList != null && masterList.size() > 0){

            int x = 0;
            for(CustomItem item : masterList){

                if(item.getItem3().trim().equals(url.trim())) ListChanelAdapter.selectedPosition = x;
                x++;
            }
        }*/

        vvPlayVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {

                pbLoading.setVisibility(View.GONE);
                mp.start();

                fullScreenVideo(scaleVideo);
                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {

                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

                        mp.start();
                        fullScreenVideo(scaleVideo);
                    }
                });
            }
        });

        vvPlayVideo.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {

                pbLoading.setVisibility(View.GONE);
                /*Snackbar.make(findViewById(android.R.id.content), "Channel sudah tidak tersedia", Snackbar.LENGTH_LONG)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        }).show();*/
                Toast.makeText(ChannelViewScreen.this, "Channel sudah tidak tersedia", Toast.LENGTH_LONG).show();
                return true;
            }
        });
    }

    private void setVideoCOntainerScale1(final boolean reverse, final double scale)
    {

        scaleVideo = scale;
        float floatScale = (float) scale;
        isFullScreen = reverse;
        fullScreenVideo(scale);
        if(reverse){
            //scaleView(rvScreenContainer, 1, 1+(1-floatScale));
            scaleView(rvScreenContainer, floatScale, 1);
        }else{
            scaleView(rvScreenContainer, 1, floatScale);
        }
    }

    public void scaleView(View v, float startScale, final float endScale) {

        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rvScreenContainer.getLayoutParams();
        params.leftMargin = 0;

        if(isFullScreen){
            params.width = (int) (metrics.widthPixels * 1);
            params.height = (int) (metrics.heightPixels * 1);
            rvScreenContainer.setScaleX(endScale);
            rvScreenContainer.setScaleY(endScale);
            rvScreenContainer.setPivotX(0);
            rvScreenContainer.setPivotY(0);
        }

        Animation anim = new ScaleAnimation(
                startScale, endScale, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(1000);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!isFullScreen){
                            rvScreenContainer.clearAnimation();
                            rvScreenContainer.setScaleX(1);
                            rvScreenContainer.setScaleY(1);
                            rvScreenContainer.setPivotX(0);
                            rvScreenContainer.setPivotY(0);
                            params.width = (int) (metrics.widthPixels * endScale);
                            params.height = (int) (metrics.heightPixels * endScale);
                        }
                        fullScreenVideo(endScale);
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(anim);
    }

    private void backNormalScale1(int Seconds, final double scale){

        new CountDownTimer(Seconds* 1000+1000, 1000) {

            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
            }

            public void onFinish() {
                wvAds.setWebViewClient(new WebViewClient());
                wvAds.loadUrl("about:blank");
                setVideoCOntainerScale1(true,scale);
            }
        }.start();
    }

    private void fullScreenVideo(double scale)
    {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) vvPlayVideo.getLayoutParams();
        if(isFullScreen){
           scale = 1;
        }
        double doubleWidth = metrics.widthPixels * scale;
        double doubleHeight = metrics.heightPixels * scale;
        RelativeLayout.LayoutParams newparams = new RelativeLayout.LayoutParams((int) doubleWidth,(int) doubleHeight);
        newparams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        newparams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        newparams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        newparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

        /*params.width =  rvScreenContainer.getMeasuredWidth();
        params.height = rvScreenContainer.getMeasuredHeight();*/

        vvPlayVideo.setLayoutParams(newparams);
    }

    @Override
    public void onBackPressed() {

        if(rvListVideoContainer.getVisibility() == View.VISIBLE){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    rvListVideoContainer.clearAnimation();
                    rvListVideoContainer.animate()
                            .translationY(0)
                            .alpha(0.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    rvListVideoContainer.setVisibility(View.GONE);
                                }
                            });
                }
            });
        }else{
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        int maxLength = (masterList == null) ? 0 : masterList.size();
        switch (keyCode){
            case 19:
                itemOnSelect = true;
                tapped = true;
                showNavigationItem();

                if(!buttonOnYoutube){

                    if(ListChanelAdapter.selectedPosition - 1 >= 0){

                        ListChanelAdapter.selectedPosition = ListChanelAdapter.selectedPosition - 1;
                        lastPositionChannel = ListChanelAdapter.selectedPosition;
                        ListChanelAdapter adapter = (ListChanelAdapter) lvChanel.getAdapter();
                        adapter.notifyDataSetChanged();
                        //lvChanel.setSelection(ListChanelAdapter.selectedPosition);
                        ensureVisible(lvChanel, ListChanelAdapter.selectedPosition);
                        /*CustomItem item = masterList.get(ListChanelAdapter.selectedPosition);
                        playVideo(item.getItem2(),item.getItem3());*/
                        llYoutubeContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_black));
                        buttonOnYoutube = false;
                    }else{

                        ListChanelAdapter.selectedPosition  = -1;
                        ListChanelAdapter adapter = (ListChanelAdapter) lvChanel.getAdapter();
                        adapter.notifyDataSetChanged();

                        llYoutubeContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_red));
                        buttonOnYoutube = true;
                    }

                }else{

                    if(masterList != null){

                        llYoutubeContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_black));
                        buttonOnYoutube = false;

                        lastPositionChannel = masterList.size() - 1;
                        // Play Last Video
                        ListChanelAdapter.selectedPosition  = lastPositionChannel;
                        ListChanelAdapter adapter = (ListChanelAdapter) lvChanel.getAdapter();
                        adapter.notifyDataSetChanged();
                        ensureVisible(lvChanel, ListChanelAdapter.selectedPosition);
                        /*CustomItem item = masterList.get(ListChanelAdapter.selectedPosition);
                        playVideo(item.getItem2(),item.getItem3());*/
                    }
                }

                break;
            case 20:

                itemOnSelect = true;
                tapped = true;
                showNavigationItem();

                if(ListChanelAdapter.selectedPosition + 1 < maxLength){
                    ListChanelAdapter.selectedPosition  = ListChanelAdapter.selectedPosition + 1;
                    ListChanelAdapter adapter = (ListChanelAdapter) lvChanel.getAdapter();
                    adapter.notifyDataSetChanged();
                    //lvChanel.setSelection(ListChanelAdapter.selectedPosition);
                    ensureVisible(lvChanel, ListChanelAdapter.selectedPosition);
                    /*CustomItem item = masterList.get(ListChanelAdapter.selectedPosition);
                    playVideo(item.getItem2(),item.getItem3());*/
                    buttonOnYoutube = false;
                    lastPositionChannel = ListChanelAdapter.selectedPosition;

                    llYoutubeContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_black));
                    buttonOnYoutube = false;
                }else{

                    ListChanelAdapter.selectedPosition  = -1;
                    ListChanelAdapter adapter = (ListChanelAdapter) lvChanel.getAdapter();
                    adapter.notifyDataSetChanged();

                    llYoutubeContainer.setBackground(getResources().getDrawable(R.drawable.background_radian_red));
                    buttonOnYoutube = true;
                }
                break;
            case 23: // OK

                if(buttonOnYoutube){

                    redirrectToYoutube();
                }else{

                    if(rvListVideoContainer.getVisibility() == View.GONE){
                    /*CustomItem item = masterList.get(ListChanelAdapter.selectedPosition);
                    playVideo(item.getItem2(),item.getItem3());*/
                        itemOnSelect = true;
                        showNavigationItem();
                    }else{

                        if(masterList != null){
                            CustomItem item = masterList.get(ListChanelAdapter.selectedPosition);
                            playVideo(item.getItem2(),item.getItem3());
                            itemOnSelect = false;
                            showNavigationItem();
                        }
                    }
                }

                break;
            case 7:
                selectChannel("0");
                break;
            case 8:
                selectChannel("1");
                break;
            case 9:
                selectChannel("2");
                break;
            case 10:
                selectChannel("3");
                break;
            case 11:
                selectChannel("4");
                break;
            case 12:
                selectChannel("5");
                break;
            case 13:
                selectChannel("6");
                break;
            case 14:
                selectChannel("7");
                break;
            case 15:
                selectChannel("8");
                break;
            case 16:
                selectChannel("9");
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void selectChannel(final String number){

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if(tvChannelSelector.getText().length() < 4){
                    isTypeChannel = true;
                    itemOnSelect = true;
                    showNavigationItem();
                    tvChannelSelector.setText(tvChannelSelector.getText().toString()+number);
                    if(llChannelSelector.getVisibility() == View.GONE){
                        llChannelSelector.setVisibility(View.VISIBLE);
                        llChannelSelector.animate()
                                .translationY(0)
                                .alpha(1.0f)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                    }
                                });
                    }
                    if(number.equals("") && tvChannelSelector.getText().length() == 1) isTypeChannel = false;
                }else{
                    isTypeChannel = false;
                }
            }
        });

        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {

                if(isTypeChannel){

                    isTypeChannel = false;
                    if(tvChannelSelector.getText().length() == 1){
                        selectChannel("");
                    }
                }else {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if(iv.parseNullInteger(tvChannelSelector.getText().toString()) > 0 && iv.parseNullInteger(tvChannelSelector.getText().toString())< masterList.size()){
                                ListChanelAdapter.selectedPosition = iv.parseNullInteger(tvChannelSelector.getText().toString()) - 1;
                                ListChanelAdapter adapter = (ListChanelAdapter) lvChanel.getAdapter();
                                adapter.notifyDataSetChanged();
                                lvChanel.smoothScrollToPosition(ListChanelAdapter.selectedPosition);
                                CustomItem item = masterList.get(ListChanelAdapter.selectedPosition);
                                playVideo(item.getItem2(),item.getItem3());

                            }else{

                                Toast.makeText(ChannelViewScreen.this, "Channel tidak tersedia", Toast.LENGTH_SHORT).show();
                            }

                            llChannelSelector.clearAnimation();
                            llChannelSelector.animate()
                                    .translationY(0)
                                    .alpha(0.0f)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            llChannelSelector.setVisibility(View.GONE);
                                            tvChannelSelector.setText("");
                                        }
                                    });
                            onBackPressed();
                        }
                    });

                }
            }

        }, channelTime);
    }

    private void ensureVisible(ListView listView, int pos)
    {
        if (listView == null)
        {
            return;
        }

        if(pos < 0 || pos >= listView.getCount())
        {
            return;
        }

        int first = listView.getFirstVisiblePosition();
        int last = listView.getLastVisiblePosition();

        if (pos < first)
        {
            listView.setSelection(pos);
            return;
        }

        if (pos >= last)
        {
            listView.setSelection(1 + pos - (last - first));
            return;
        }
    }

    // ===================================== Remote ================================

    NsdManager.RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {

        @Override
        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
            String mServiceName = NsdServiceInfo.getServiceName();
            ServiceUtils.SERVICE_NAME = mServiceName;
            Log.d(TAG, "Registered name : " + mServiceName);
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo,
                                         int errorCode) {
            // Registration failed! Put debugging code here to determine
            // why.
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
            // Service has been unregistered. This only happens when you
            // call
            // NsdManager.unregisterService() and pass in this listener.
            Log.d(TAG,
                    "Service Unregistered : " + serviceInfo.getServiceName());
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo,
                                           int errorCode) {
            // Unregistration failed. Put debugging code here to determine
            // why.
        }
    };

    @Override
    protected void onPause() {
        if (mNsdManager != null) {
            try{
                mNsdManager.unregisterService(mRegistrationListener);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getDataWithConnection();
        if (mNsdManager != null) {
            registerService(ServiceUtils.DEFAULT_PORT);
        }

        initializeReceiver();
    }

    @Override
    protected void onDestroy() {

        if (mNsdManager != null) {
            try{
                mNsdManager.unregisterService(mRegistrationListener);
            }catch (Exception e){

                e.printStackTrace();
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(ServiceUtils.SERVICE_NAME);
        serviceInfo.setServiceType(ServiceUtils.SERVICE_TYPE);

        try {
            serviceInfo.setHost(InetAddress.getByName(getMyIPAddress(true)));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        serviceInfo.setPort(port);

        try {
            mNsdManager.registerService(serviceInfo,
                    NsdManager.PROTOCOL_DNS_SD,
                    mRegistrationListener);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static String getMyIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;
                        //Log.d(TAG, "getIPAddress: "+ addr.getHostName());
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

    private void initializeReceiver() {
        socketServerThread = new SocketServerThread();
        socketServerThread.start();
    }

    private class SocketServerThread extends Thread {

        @Override
        public void run() {

            Socket socket = null;
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;

            try {
                Log.i(TAG, "Creating server socket");
                serverSocket = new ServerSocket(ServiceUtils.DEFAULT_PORT);

                while (true) {
                    socket = serverSocket.accept();
                    dataInputStream = new DataInputStream(
                            socket.getInputStream());
                    dataOutputStream = new DataOutputStream(
                            socket.getOutputStream());

                    String messageFromClient, messageToClient, request;

                    //If no message sent from client, this code will block the program
                    messageFromClient = dataInputStream.readUTF();

                    final JSONObject jsondata;

                    try {
                        jsondata = new JSONObject(messageFromClient);
                        request = jsondata.getString("request");

                        if (request.equals(ServiceUtils.REQUEST_CODE)) {

                            String clientIPAddress = jsondata.getString("ipAddress");
                            String typeCommand = jsondata.getString("type");

                            if(typeCommand.equals("request_connection")){

                                if(ServiceUtils.lockedClient.equals("") || ServiceUtils.lockedClient.equals(clientIPAddress)){

                                    ServiceUtils.lockedClient = clientIPAddress;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ChannelViewScreen.this, "Connected device " + ServiceUtils.lockedClient, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    messageToClient = "1";
                                }else{
                                    messageToClient = "0";
                                }
                                dataOutputStream.writeUTF(messageToClient);

                            } else if(typeCommand.equals("clear_connection")){

                                if(clientIPAddress.equals(ServiceUtils.lockedClient)){
                                    ServiceUtils.lockedClient = "";
                                    messageToClient = "1";
                                }else{
                                    messageToClient = "0";
                                }
                                dataOutputStream.writeUTF(messageToClient);
                            }else{

                                if(ServiceUtils.lockedClient.equals(clientIPAddress) || ServiceUtils.lockedClient.equals("")){
                                    ServiceUtils.lockedClient = clientIPAddress;
                                    String keyCode = jsondata.getString("keyCode");
                                    Log.d(TAG, "ip Client: " +clientIPAddress);
                                    // Add client IP to a list
                                    getAction(iv.parseNullInteger(keyCode));
                                    messageToClient = "Connection Accepted";
                                    dataOutputStream.writeUTF(messageToClient);
                                }else{
                                    messageToClient = "Connection Rejected ip not registered";
                                    dataOutputStream.writeUTF(messageToClient);
                                }
                            }
                        } else {
                            // There might be other queries, but as of now nothing.
                            dataOutputStream.flush();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Unable to get request");
                        dataOutputStream.flush();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void getAction(final int keyCode){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
            }
        });
    }
}
