package id.net.gmedia.gmediatv.Main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
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
import android.widget.VideoView;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import id.net.gmedia.gmediatv.Main.Adapter.AllChannelAdapter;
import id.net.gmedia.gmediatv.Main.Adapter.ListChanelAdapter;
import id.net.gmedia.gmediatv.R;
import id.net.gmedia.gmediatv.Utils.CustomVideoView;
import id.net.gmedia.gmediatv.Utils.SavedChanelManager;
import id.net.gmedia.gmediatv.Utils.ServerURL;
import id.net.gmedia.gmediatv.Youtube.YoutubePlayerActivity;

public class ChannelViewScreen extends AppCompatActivity {

    private static CustomVideoView vvPlayVideo;
    private ItemValidation iv = new ItemValidation();
    private boolean showNavigator = false;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view_screen);

        isTypeChannel = false;
        savedChanel = new SavedChanelManager(ChannelViewScreen.this);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            String nama = bundle.getString("nama");
            String link = bundle.getString("link");
            if(nama != null && link != null) savedChanel.saveLastChanel(nama,link);
        }

        initUI();
    }

    private void initUI() {

        tvVolume = (TextView) findViewById(R.id.tv_volume);
        sbVolume = (SeekBar) findViewById(R.id.sb_volume);
        vvPlayVideo = (CustomVideoView) findViewById(R.id.vv_stream);
        llYoutubeContainer = (LinearLayout) findViewById(R.id.ll_youtube_container);
        lvChanel = (ListView) findViewById(R.id.lv_chanel);
        rvListVideoContainer = (RelativeLayout) findViewById(R.id.rl_list_chanel);
        ivUp = (ImageView) findViewById(R.id.iv_up);
        ivDown = (ImageView) findViewById(R.id.iv_down);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        llChannelSelector = (LinearLayout) findViewById(R.id.ll_channel_selector);
        tvChannelSelector = (TextView) findViewById(R.id.tv_channel_selector);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        sbVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        tapped = true;
        showNavigationItem();
        if(savedChanel.isSaved()) playVideo(savedChanel.getNama(), savedChanel.getLink());
        getLinkRTSP();

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
                Intent intent = new Intent(ChannelViewScreen.this, YoutubePlayerActivity.class);
                startActivity(intent);
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

        /*Thread thread = new Thread() {
            @Override
            public void run() {

                try { // this code using timer and this not a good practice for automatically hide

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            if(rvListVideoContainer.getVisibility() == View.GONE && tapped){
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
                            }else if(rvListVideoContainer.getVisibility() == View.VISIBLE && !itemOnSelect && tapped){

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
                            tapped = false;
                        }
                    });
                    Thread.sleep(delayTime);
                } catch (InterruptedException e) {
                }

                if(!itemOnSelect && rvListVideoContainer.getVisibility() == View.VISIBLE && !tapped) {
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
                    tapped = true;
                }
            }
        };
        thread.start();*/

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

    private void getLinkRTSP() {

        ApiVolley apiVolley = new ApiVolley(ChannelViewScreen.this, new JSONObject(), "GET", ServerURL.getLink, "", "", 0, new ApiVolley.VolleyCallback() {
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

                fullScreenVideo();
                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {

                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

                        mp.start();
                        fullScreenVideo();
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

    private void fullScreenVideo()
    {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) vvPlayVideo.getLayoutParams();
        params.width =  metrics.widthPixels;
        params.height = metrics.heightPixels;
        params.leftMargin = 0;
        vvPlayVideo.setLayoutParams(params);
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

        int maxLength = masterList.size();
        switch (keyCode){
            case 19:
                itemOnSelect = true;
                tapped = true;
                showNavigationItem();
                if(ListChanelAdapter.selectedPosition - 1 >= 0){
                    ListChanelAdapter.selectedPosition = ListChanelAdapter.selectedPosition - 1;
                    ListChanelAdapter adapter = (ListChanelAdapter) lvChanel.getAdapter();
                    adapter.notifyDataSetChanged();
                    lvChanel.setSelection(ListChanelAdapter.selectedPosition);
                    CustomItem item = masterList.get(ListChanelAdapter.selectedPosition);
                    playVideo(item.getItem2(),item.getItem3());
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
                    lvChanel.setSelection(ListChanelAdapter.selectedPosition);
                    CustomItem item = masterList.get(ListChanelAdapter.selectedPosition);
                    playVideo(item.getItem2(),item.getItem3());
                }
                break;
            case 23: // OK
                if(rvListVideoContainer.getVisibility() == View.GONE){
                    /*CustomItem item = masterList.get(ListChanelAdapter.selectedPosition);
                    playVideo(item.getItem2(),item.getItem3());*/
                    itemOnSelect = true;
                    showNavigationItem();
                }else{
                    itemOnSelect = false;
                    showNavigationItem();
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

                            if(iv.parseNullInteger(tvChannelSelector.getText().toString()) >= 0 && iv.parseNullInteger(tvChannelSelector.getText().toString())< masterList.size()){
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
}
