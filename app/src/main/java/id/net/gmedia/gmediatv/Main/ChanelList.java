package id.net.gmedia.gmediatv.Main;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
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

import id.net.gmedia.gmediatv.Main.Adapter.AllChannelAdapter;
import id.net.gmedia.gmediatv.R;
import id.net.gmedia.gmediatv.RemoteUtils.ServiceUtils;
import id.net.gmedia.gmediatv.Utils.FormatItem;
import id.net.gmedia.gmediatv.Utils.InternetCheck;
import id.net.gmedia.gmediatv.Utils.SavedChanelManager;
import id.net.gmedia.gmediatv.Utils.ServerURL;

public class ChanelList extends AppCompatActivity {

    private RecyclerView rvChanggelList;
    private ItemValidation iv = new ItemValidation();
    private List<CustomItem> masterList;
    private SavedChanelManager chanelManager;
    private ProgressBar pbLoading;
    private final String TAG = "ChannelList";
    private SavedChanelManager savedChanel;
    private int savedC = 0;

    //For remote
    private NsdManager mNsdManager;
    private ServerSocket serverSocket;
    private SocketServerThread socketServerThread;

    private WifiManager wifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chanel_list);

        initUI();

        // For Remote access
        //ServiceUtils.DEFAULT_PORT = ConnectionUtil.getPort(ServerActivity.this);
        if (Build.VERSION.SDK_INT >= 21) {
            mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
            registerService(ServiceUtils.DEFAULT_PORT);
            initializeReceiver();
        }
    }

    private void initUI() {

        rvChanggelList = (RecyclerView) findViewById(R.id.rv_list_channel);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        chanelManager = new SavedChanelManager(ChanelList.this);

        savedChanel = new SavedChanelManager(ChanelList.this);
        savedC = 0;
        ServiceUtils.lockedClient = "";
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        //getDataWithConnection();
    }

    private void getDataWithConnection() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        //return activeNetworkInfo != null && activeNetworkInfo.isConnected();

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            new InternetCheck(ChanelList.this).isInternetConnectionAvailable(new InternetCheck.InternetCheckListener() {

                @Override
                public void onComplete(boolean connected) {
                    if(connected){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getListChannel();
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

    private void getListChannel() {

        pbLoading.setVisibility(View.VISIBLE);

        JSONObject jbody = new JSONObject();

        try {
            jbody.put("mac", FormatItem.getMacAddress());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley apiVolley = new ApiVolley(ChanelList.this, jbody, "POST", ServerURL.getLink, "", "", 0, new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                masterList = new ArrayList<>();
                int x = 0;
                try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200) {

                        savedC = 0;
                        int s = 0;
                        JSONArray jsonArray = response.getJSONArray("response");
                        for(int i = 0; i < jsonArray.length();i++){
                            JSONObject jo = jsonArray.getJSONObject(i);
                            masterList.add(new CustomItem(jo.getString("id"), jo.getString("nama"), jo.getString("link")));
                            if(jo.getString("nama").equals(savedChanel.getNama()) && jo.getString("link").equals(savedChanel.getLink())){
                                savedC = s;
                            }
                            s++;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                setListChanel(masterList, savedC);
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onError(String result) {

                setListChanel(null, 0);
                pbLoading.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChanelList.this, MainMenu.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void setListChanel(List<CustomItem> listItem, int saved){

        rvChanggelList.setAdapter(null);

        if(listItem != null && listItem.size() > 0){

            Bundle bundle = getIntent().getExtras();
            if(bundle != null){

                if(bundle.getBoolean("splashed", false)){
                    getIntent().putExtra("splashed",false);
                    CustomItem item = listItem.get(saved);
                    Intent intent = new Intent(ChanelList.this, ChannelViewScreen.class);
                    intent.putExtra("nama", item.getItem2());
                    intent.putExtra("link", item.getItem3());
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }

            final AllChannelAdapter menuAdapter = new AllChannelAdapter(ChanelList.this, listItem);

            final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(ChanelList.this, 4);
            rvChanggelList.setLayoutManager(mLayoutManager);
//        rvListMenu.addItemDecoration(new NavMenu.GridSpacingItemDecoration(2, dpToPx(10), true));
            rvChanggelList.setItemAnimator(new DefaultItemAnimator());
            rvChanggelList.setAdapter(menuAdapter);

            menuAdapter.selectedPosition = savedC;
            menuAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        int maxleng = (masterList != null) ? masterList.size() : 0;
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

                if(masterList != null){
                    CustomItem item = masterList.get(AllChannelAdapter.selectedPosition);
                    Intent intent = new Intent(ChanelList.this, ChannelViewScreen.class);
                    intent.putExtra("nama", item.getItem2());
                    intent.putExtra("link", item.getItem3());
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
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
            if (Build.VERSION.SDK_INT >= 21) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onPause();
    }

    @Override
    protected void onStop() {

        if (mNsdManager != null) {
            try{
                mNsdManager.unregisterService(mRegistrationListener);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= 21) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getDataWithConnection();

        if (Build.VERSION.SDK_INT >= 21) {
            if (mNsdManager != null) {
                registerService(ServiceUtils.DEFAULT_PORT);
            }

            initializeReceiver();
        }
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
            if (Build.VERSION.SDK_INT >= 21) {
                serverSocket.close();
            }
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
                                            Toast.makeText(ChanelList.this, "Connected device " + ServiceUtils.lockedClient, Toast.LENGTH_LONG).show();
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
