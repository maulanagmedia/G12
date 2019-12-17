package id.net.gmedia.gmediatv.DaftarVideo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import id.net.gmedia.gmediatv.DaftarVideo.Adapter.VideoListAdapter;
import id.net.gmedia.gmediatv.R;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

public class DaftarVideo extends AppCompatActivity {

    private TextView tvIP, tvISP, tvLatLong;
    private RecyclerView rvistVideo;
    private List<CustomItem> masterList;
    private static final String TAG = "DAFTAR_VIDEO";
    private ItemValidation iv = new ItemValidation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_video);

        initUI();
    }

    private void initUI() {

        tvIP = (TextView) findViewById(R.id.tv_ip);
        tvISP = (TextView) findViewById(R.id.tv_isp);
        tvLatLong = (TextView) findViewById(R.id.tv_lat_long);
        rvistVideo = (RecyclerView) findViewById(R.id.rv_list_video);

        tvIP.setText(getIPAddress(true));
        getIPInfo();

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            for (Network network : connectivityManager.getAllNetworks()) {
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                if (networkInfo.isConnected()) {
                    LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
                    Log.d("DnsInfo", "iface = " + linkProperties.getInterfaceName());
                    Log.d("DnsInfo", "dns = " + linkProperties.getDnsServers());

                    tvISP.setText(linkProperties.getInterfaceName());
                }
            }
        }*/

        getVideoList();
    }

    private void getIPInfo(){

        ApiVolley request = new ApiVolley(DaftarVideo.this, new JSONObject(), "GET", "http://ip-api.com/json", "", "", 0, new ApiVolley.VolleyCallback() {

            @Override
            public void onSuccess(String result) {

                JSONObject responseAPI;
                try {
                    responseAPI = new JSONObject(result);
                    String isp = responseAPI.getString("isp");
                    String latLong = responseAPI.getString("lat")+" ,"+responseAPI.getString("lon");

                    tvISP.setText(isp);
                    tvLatLong.setText(latLong);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {
                Log.d(TAG, "onError: "+result);
            }
        });
    }

    private void getVideoList() {

        masterList = new ArrayList<>();
        masterList.add(new CustomItem("1","Toy Story 2", "01:23","http://www.html5videoplayer.net/videos/toystory.mp4".substring("http://www.html5videoplayer.net/videos/toystory.mp4".lastIndexOf(".")), "http://www.html5videoplayer.net/videos/toystory.mp4"));
        masterList.add(new CustomItem("2","Toy Story 3", "01:23","http://www.html5videoplayer.net/videos/toystory.mp4".substring("http://www.html5videoplayer.net/videos/toystory.mp4".lastIndexOf(".")), "http://www.html5videoplayer.net/videos/toystory.mp4"));
        masterList.add(new CustomItem("3","Toy Story 4", "01:23","http://www.html5videoplayer.net/videos/toystory.mp4".substring("http://www.html5videoplayer.net/videos/toystory.mp4".lastIndexOf(".")), "http://www.html5videoplayer.net/videos/toystory.mp4"));

        setAdapter(masterList);
    }

    private void setAdapter(List listItem){

        rvistVideo.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            VideoListAdapter menuAdapter = new VideoListAdapter(DaftarVideo.this, listItem);

            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(DaftarVideo.this, 1);
            rvistVideo.setLayoutManager(mLayoutManager);
//        rvListMenu.addItemDecoration(new NavMenu.GridSpacingItemDecoration(2, dpToPx(10), true));
            rvistVideo.setItemAnimator(new DefaultItemAnimator());
            rvistVideo.setAdapter(menuAdapter);
        }
    }

    public static String getIPAddress(boolean useIPv4) {
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
}
