package id.net.gmedia.gmediatv.Main;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import id.net.gmedia.gmediatv.Main.Adapter.AllChannelAdapter;
import id.net.gmedia.gmediatv.Main.Adapter.ListChanelAdapter;
import id.net.gmedia.gmediatv.R;
import id.net.gmedia.gmediatv.Utils.SavedChanelManager;
import id.net.gmedia.gmediatv.Utils.ServerURL;

public class ChanelList extends AppCompatActivity {

    private RecyclerView rvChanggelList;
    private ItemValidation iv = new ItemValidation();
    private List<CustomItem> masterList;
    private SavedChanelManager chanelManager;
    private ProgressBar pbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chanel_list);

        initUI();
    }

    private void initUI() {

        rvChanggelList = (RecyclerView) findViewById(R.id.rv_list_channel);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        chanelManager = new SavedChanelManager(ChanelList.this);

        getListChannel();
    }

    private void getListChannel() {

        pbLoading.setVisibility(View.VISIBLE);
        ApiVolley apiVolley = new ApiVolley(ChanelList.this, new JSONObject(), "GET", ServerURL.getLink, "", "", 0, new ApiVolley.VolleyCallback() {
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
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                setListChanel(masterList, x);
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onError(String result) {

                setListChanel(null, 0);
                pbLoading.setVisibility(View.GONE);
            }
        });
    }

    private void setListChanel(List<CustomItem> listItem, int saved){

        rvChanggelList.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            final AllChannelAdapter menuAdapter = new AllChannelAdapter(ChanelList.this, listItem);

            final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(ChanelList.this, 4);
            rvChanggelList.setLayoutManager(mLayoutManager);
//        rvListMenu.addItemDecoration(new NavMenu.GridSpacingItemDecoration(2, dpToPx(10), true));
            rvChanggelList.setItemAnimator(new DefaultItemAnimator());
            rvChanggelList.setAdapter(menuAdapter);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        int maxleng = masterList.size();
        switch (keyCode){
            case 19:
                if(AllChannelAdapter.selectedPosition - 4 >= 0){
                    AllChannelAdapter.selectedPosition = AllChannelAdapter.selectedPosition - 4;
                    AllChannelAdapter adapter = (AllChannelAdapter) rvChanggelList.getAdapter();
                    adapter.notifyDataSetChanged();
                    rvChanggelList.smoothScrollToPosition(AllChannelAdapter.selectedPosition);
                }
                break;
            case 20:
                if(AllChannelAdapter.selectedPosition + 4 < maxleng){
                    AllChannelAdapter.selectedPosition = AllChannelAdapter.selectedPosition + 4;
                    AllChannelAdapter adapter = (AllChannelAdapter) rvChanggelList.getAdapter();
                    adapter.notifyDataSetChanged();
                    rvChanggelList.smoothScrollToPosition(AllChannelAdapter.selectedPosition);
                }
                break;
            case 22:
                if(AllChannelAdapter.selectedPosition + 1 < maxleng){
                    AllChannelAdapter.selectedPosition = AllChannelAdapter.selectedPosition + 1;
                    AllChannelAdapter adapter = (AllChannelAdapter) rvChanggelList.getAdapter();
                    adapter.notifyDataSetChanged();
                    rvChanggelList.smoothScrollToPosition(AllChannelAdapter.selectedPosition);
                }
                break;
            case 21:
                if(AllChannelAdapter.selectedPosition - 1 >= 0){
                    AllChannelAdapter.selectedPosition = AllChannelAdapter.selectedPosition - 1;
                    AllChannelAdapter adapter = (AllChannelAdapter) rvChanggelList.getAdapter();
                    adapter.notifyDataSetChanged();
                    rvChanggelList.smoothScrollToPosition(AllChannelAdapter.selectedPosition);
                }
                break;
            case 23:

                CustomItem item = masterList.get(AllChannelAdapter.selectedPosition);
                Intent intent = new Intent(ChanelList.this, ChannelViewScreen.class);
                intent.putExtra("nama", item.getItem2());
                intent.putExtra("link", item.getItem3());
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
