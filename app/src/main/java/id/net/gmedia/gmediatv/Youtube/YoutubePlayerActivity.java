package id.net.gmedia.gmediatv.Youtube;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.azoft.carousellayoutmanager.CenterScrollListener;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.EndlessRVSListener;
import com.maulana.custommodul.EndlessScroll;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import id.net.gmedia.gmediatv.R;
import id.net.gmedia.gmediatv.Utils.GoogleAPI;
import id.net.gmedia.gmediatv.Utils.ServerURL;
import id.net.gmedia.gmediatv.Youtube.Adapter.YoutubeListAdapter;

public class YoutubePlayerActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private static final int RECOVERY_REQUEST = 1;
    private static YouTubePlayerView ypYoutube;

    private MyPlayerStateChangeListener playerStateChangeListener;
    private MyPlaybackEventListener playbackEventListener;
    private static YouTubePlayer player;
    private static boolean fullScreen = false;
    private SearchView edtSearch;
    private TextView tvSearch;
    private static RecyclerView rvYoutube;
    private static List<CustomItem> masterList;
    private String nextPageToken = "", maxLengthList = "10", keyword = "";
    private final String TAG = "TEST";
    private ProgressBar pbLoading;
    private static boolean potraitMode = true;
    private CarouselLayoutManager layoutManager;
    private RelativeLayout rvSearchView;
    private SimpleCursorAdapter mAdapter;
    private List<String> suggestion = new ArrayList<>();
    private static boolean isOnListCusor;
    private static int lastPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.activity_youtube_player);

        isOnListCusor = true;
        lastPosition = 0;

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            potraitMode = true;
        }else{
            potraitMode = false;
        }

        initUI();
    }

    private void initUI(){

        ypYoutube = (YouTubePlayerView) findViewById(R.id.yp_youtube);
        ypYoutube.initialize(GoogleAPI.APIKey, this);
        rvSearchView = (RelativeLayout) findViewById(R.id.rv_searchview);
        edtSearch = (SearchView) findViewById(R.id.edt_search);
        tvSearch = (TextView) findViewById(R.id.tv_search);
        rvYoutube = (RecyclerView) findViewById(R.id.rv_youtube);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        masterList = new ArrayList<>();

        tvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvSearch.setVisibility(View.GONE);
                edtSearch.onActionViewExpanded();
            }
        });

        edtSearch.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvSearch.setVisibility(View.GONE);
            }
        });

        edtSearch.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                tvSearch.setVisibility(View.VISIBLE);
                return false;
            }
        });

        edtSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if(!query.equals("")){

                    keyword = query;
                    nextPageToken = "";
                    lastPosition = 0;
                    getListData();
                    isSearchCollapse(true);
                    return true;
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        edtSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                populateAdapter(s);
                if(s.length() > 2){
                    keyword = s;
                    nextPageToken = "";
                    getListData();
                    return true;
                }
                return false;
            }
        });

        setSearchEvent();
        getListData();

    }

    private void setSearchEvent(){

        String[] from = new String[] {"search"};
        int[] to = new int[] {android.R.id.text1};
        mAdapter = new SimpleCursorAdapter(YoutubePlayerActivity.this,
                android.R.layout.simple_list_item_1,
                null,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        edtSearch.setSuggestionsAdapter(mAdapter);

        edtSearch.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {

                return false;
            }

            @Override
            public boolean onSuggestionClick(int i) {

                if(suggestion.get(i).length() >0){

                    edtSearch.setQuery(suggestion.get(i), true);
                    return true;
                }
                return false;
            }
        });

        /*edtSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                if(i == 4){
                    InputMethodManager imm = (InputMethodManager) YoutubePlayerActivity.this
                            .getSystemService(Context.INPUT_METHOD_SERVICE);

                    if(imm.isAcceptingText()){
                        imm.hideSoftInputFromWindow( view.getWindowToken(), 0);

                        if(!isOnListCusor){
                            YoutubeListAdapter.position = lastPosition;
                            rvYoutube.getAdapter().notifyDataSetChanged();
                            isOnListCusor = true;
                        }

                        return true;
                    }else{
                        edtSearch.setSelected(false);
                        edtSearch.setHovered(false);
                    }
                    return true;
                }
                return false;
            }
        });*/

    }

    private void populateAdapter(String query) {
        final MatrixCursor c = new MatrixCursor(new String[]{ BaseColumns._ID, "search" });
        for (int i=0; i< suggestion.size(); i++) {
            if (suggestion.get(i).toLowerCase().contains(query.toLowerCase()))
                c.addRow(new Object[] {i, suggestion.get(i)});
        }
        mAdapter.changeCursor(c);
    }

    private void getListData(){

        masterList = new ArrayList<>();
        ApiVolley request = new ApiVolley(YoutubePlayerActivity.this, new JSONObject(), "GET", ServerURL.getListYoutubeVideoURL(nextPageToken, maxLengthList, keyword), "", "", 0, new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {

                    JSONObject apiResponse = new JSONObject(result);
                    nextPageToken = apiResponse.getString("nextPageToken");

                    JSONArray jsonArray = apiResponse.getJSONArray("items");
                    suggestion = new ArrayList<>();

                    if(jsonArray.length() > 0){

                        for(int i = 0 ; i < jsonArray.length(); i++){

                            String id = "", title = "", thumbnail = "";
                            JSONObject jo = jsonArray.getJSONObject(i);
                            id = jo.getJSONObject("id").getString("videoId");
                            title = jo.getJSONObject("snippet").getString("title");
                            thumbnail = jo.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("default").getString("url");
                            suggestion.add(jo.getJSONObject("snippet").getString("title"));
                            if(i == 0) {
                                playVideo(id, i);
                                YoutubeListAdapter.position = 0;
                                lastPosition = 0;
                            }
                            masterList.add(new CustomItem(id, title, thumbnail));
                        }
                    }

                    setAdapter(masterList);
                } catch (JSONException e) {
                    setAdapter(null);
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {
                setAdapter(null);
            }
        });
    }

    private void getMoreVideos(){

        final List<CustomItem> listToAdd = new ArrayList<>();
        ApiVolley request = new ApiVolley(YoutubePlayerActivity.this, new JSONObject(), "GET", ServerURL.getListYoutubeVideoURL(nextPageToken, maxLengthList, keyword), "", "", 0, new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {

                    JSONObject apiResponse = new JSONObject(result);
                    nextPageToken = apiResponse.getString("nextPageToken");

                    JSONArray jsonArray = apiResponse.getJSONArray("items");

                    if(jsonArray.length() > 0){

                        for(int i = 0 ; i < jsonArray.length(); i++){

                            String id = "", title = "", thumbnail = "";
                            JSONObject jo = jsonArray.getJSONObject(i);
                            id = jo.getJSONObject("id").getString("videoId");
                            title = jo.getJSONObject("snippet").getString("title");
                            thumbnail = jo.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("default").getString("url");
                            listToAdd.add(new CustomItem(id, title, thumbnail));
                            suggestion.add(jo.getJSONObject("snippet").getString("title"));
                        }

                        YoutubeListAdapter adapter = (YoutubeListAdapter) rvYoutube.getAdapter();
                        adapter.addItem(listToAdd);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onError(String result) {

                pbLoading.setVisibility(View.GONE);
            }
        });
    }

    private void setAdapter(List listItem){

        rvYoutube.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            final YoutubeListAdapter menuAdapter = new YoutubeListAdapter(YoutubePlayerActivity.this, listItem);

            if(potraitMode){
                final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(YoutubePlayerActivity.this, (potraitMode)? 2 : 1);
                rvYoutube.setLayoutManager(mLayoutManager);
//        rvListMenu.addItemDecoration(new NavMenu.GridSpacingItemDecoration(2, dpToPx(10), true));
                rvYoutube.setItemAnimator(new DefaultItemAnimator());
                rvYoutube.setAdapter(menuAdapter);

                EndlessScroll scrollListener = new EndlessScroll((GridLayoutManager) mLayoutManager) {
                    @Override
                    public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                        pbLoading.setVisibility(View.VISIBLE);
                        getMoreVideos();
                    }
                };

                rvYoutube.addOnScrollListener(scrollListener);
            }else{

                layoutManager = new CarouselLayoutManager(CarouselLayoutManager.VERTICAL);
                rvYoutube.setLayoutManager(layoutManager);
                rvYoutube.setHasFixedSize(false);
                rvYoutube.setAdapter(menuAdapter);

                rvYoutube.addOnScrollListener(new CenterScrollListener());
                layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());

                rvYoutube.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if(layoutManager.getCenterItemPosition() == rvYoutube.getAdapter().getItemCount() - 1){
                            pbLoading.setVisibility(View.VISIBLE);
                            getMoreVideos();
                        }

                        YoutubeListAdapter.position = layoutManager.getCenterItemPosition();
                        rvYoutube.getAdapter().notifyDataSetChanged();
                    }
                });
            }

        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {

        player = youTubePlayer;
        /*player.setPlayerStateChangeListener(playerStateChangeListener);
        player.setPlaybackEventListener(playbackEventListener);*/

        if (!b) {
            //player.loadVideo("fhWaJi1Hsfo"); // Plays https://www.youtube.com/watch?v=fhWaJi1Hsfo
            //youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
            player.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
                @Override
                public void onFullscreen(boolean b) {
                    fullScreen = b;
                }
            });
        }
    }

    public static void playVideo(String id, int position){

        try {

            player.loadVideo(id);
            rvYoutube.smoothScrollToPosition(position);

            player.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                @Override
                public void onLoading() {

                }

                @Override
                public void onLoaded(String s) {

                }

                @Override
                public void onAdStarted() {

                }

                @Override
                public void onVideoStarted() {

                }

                @Override
                public void onVideoEnded() {
                    if(YoutubeListAdapter.position + 1 < masterList.size()){
                        YoutubeListAdapter.position += 1;
                    }else{
                        YoutubeListAdapter.position = 0;
                    }
                    CustomItem cli = masterList.get(YoutubeListAdapter.position);
                    playVideo(cli.getItem1(), YoutubeListAdapter.position);
                }

                @Override
                public void onError(YouTubePlayer.ErrorReason errorReason) {

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

    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return ypYoutube;
    }

    // Custom Class for youtube naviagation

    private void showMessage(String message){

        Toast.makeText(YoutubePlayerActivity.this, message, Toast.LENGTH_SHORT).show();
    }
    private final class MyPlaybackEventListener implements YouTubePlayer.PlaybackEventListener {

        @Override
        public void onPlaying() {
            // Called when playback starts, either due to user action or call to play().
            showMessage("Playing");
        }

        @Override
        public void onPaused() {
            // Called when playback is paused, either due to user action or call to pause().
            showMessage("Paused");
        }

        @Override
        public void onStopped() {
            // Called when playback stops for a reason other than being paused.
            showMessage("Stopped");
        }

        @Override
        public void onBuffering(boolean b) {
            // Called when buffering starts or ends.
        }

        @Override
        public void onSeekTo(int i) {
            // Called when a jump in playback position occurs, either
            // due to user scrubbing or call to seekRelativeMillis() or seekToMillis()
        }
    }

    private final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {

        @Override
        public void onLoading() {
            // Called when the player is loading a video
            // At this point, it's not ready to accept commands affecting playback such as play() or pause()
        }

        @Override
        public void onLoaded(String s) {
            // Called when a video is done loading.
            // Playback methods such as play(), pause() or seekToMillis(int) may be called after this callback.
        }

        @Override
        public void onAdStarted() {
            // Called when playback of an advertisement starts.
        }

        @Override
        public void onVideoStarted() {
            // Called when playback of the video starts.
        }

        @Override
        public void onVideoEnded() {
            // Called when the video reaches its end.
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            // Called when an error occurs.
        }
    }

    @Override
    public void onBackPressed() {
        if (fullScreen){
            player.setFullscreen(false);
            fullScreen = false;
        } else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode){
            case 19:

                disableYoutubeSelection();
                if(isOnListCusor){

                    isSearchCollapse(true);
                    if(YoutubeListAdapter.position - 1 >= 0){

                        YoutubeListAdapter.position = YoutubeListAdapter.position - 1;
                        rvYoutube.smoothScrollToPosition(YoutubeListAdapter.position);
                        rvYoutube.getAdapter().notifyDataSetChanged();
                    }
                }else{

                    isSearchCollapse(false);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
                return true;
            case 20:

                disableYoutubeSelection();
                if(isOnListCusor){
                    isSearchCollapse(true);
                    if(YoutubeListAdapter.position + 1 < rvYoutube.getAdapter().getItemCount()){

                        YoutubeListAdapter.position = YoutubeListAdapter.position + 1;
                        rvYoutube.smoothScrollToPosition(YoutubeListAdapter.position);
                        rvYoutube.getAdapter().notifyDataSetChanged();
                    }else{
                        pbLoading.setVisibility(View.VISIBLE);
                        getMoreVideos();
                    }
                }
                return true;
            case 23:

                disableYoutubeSelection();
                isSearchCollapse(true);
                if(isOnListCusor){
                    CustomItem cli = masterList.get(YoutubeListAdapter.position);
                    playVideo(cli.getItem1(), YoutubeListAdapter.position);
                    isOnListCusor = false;
                    lastPosition = YoutubeListAdapter.position;
                    YoutubeListAdapter.position = -1;
                    rvYoutube.getAdapter().notifyDataSetChanged();
                }else{

                    if(fullScreen){

                        player.setFullscreen(false);
                        fullScreen = false;

                        if(!isOnListCusor){
                            YoutubeListAdapter.position = lastPosition;
                            rvYoutube.getAdapter().notifyDataSetChanged();
                            isOnListCusor = true;
                        }
                    }else{
                        player.setFullscreen(true);
                        fullScreen = true;
                    }
                }
                return true;
            case 21:

                disableYoutubeSelection();
                if(fullScreen){
                    player.setFullscreen(false);
                    fullScreen = false;
                }else{
                    isSearchCollapse(false);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
                return true;
            case 22:

                disableYoutubeSelection();
                if(!isOnListCusor){

                    YoutubeListAdapter.position =  lastPosition;
                    rvYoutube.getAdapter().notifyDataSetChanged();
                    isOnListCusor = true;
                }
                return true;
            case 4: //back

                disableYoutubeSelection();
                InputMethodManager imm = (InputMethodManager) YoutubePlayerActivity.this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);

                if(imm.isAcceptingText()){
                    View view = this.getCurrentFocus();
                    imm.hideSoftInputFromWindow( view.getWindowToken(), 0);

                    if(!isOnListCusor){
                        YoutubeListAdapter.position = lastPosition;
                        rvYoutube.getAdapter().notifyDataSetChanged();
                        isOnListCusor = true;
                    }

                    return true;
                }else{
                    edtSearch.setSelected(false);
                    edtSearch.setHovered(false);
                    break;
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void isSearchCollapse(boolean condition){

        if(condition){

            edtSearch.setSelected(false);
            edtSearch.setHovered(false);
            edtSearch.clearFocus();
            rvYoutube.requestFocus();
            rvYoutube.setSelected(true);
            rvYoutube.setHovered(true);
            //edtSearch.onActionViewCollapsed();
            //tvSearch.setVisibility(View.VISIBLE);
        }else{

            edtSearch.requestFocus();
            tvSearch.setVisibility(View.GONE);
            edtSearch.onActionViewExpanded();
        }
    }

    private void disableYoutubeSelection(){

        ypYoutube.clearFocus();
        ypYoutube.setHovered(false);
        ypYoutube.setSelected(false);
        //ypYoutube.setOnKeyListener(null);
        //ypYoutube.setFocusableInTouchMode(false);
    }
}
