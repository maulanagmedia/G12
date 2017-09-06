package id.net.gmedia.gmediatv.Main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.maulana.custommodul.ApkInstaller;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.RuntimePermissionsActivity;

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
import java.util.Collections;
import java.util.List;

import id.net.gmedia.gmediatv.R;
import id.net.gmedia.gmediatv.RemoteUtils.ServiceUtils;
import id.net.gmedia.gmediatv.Utils.ServerURL;
import id.net.gmedia.gmediatv.Utils.ServiceTimerServer;

public class MainMenu extends RuntimePermissionsActivity {

    private static boolean doubleBackToExitPressedOnce;
    private boolean exitState = false;
    private LinearLayout llTV, llYoutube;
    private ImageView ivTV, ivYoutube;
    private static int selectedChoise = 0;
    private static final int REQUEST_PERMISSIONS = 20;
    private LinearLayout llNetflix, llIflix;
    private ImageView ivNetFlix, ivIflix;
    private final String TAG = "MainMenu";

    //For remote
    private NsdManager mNsdManager;
    private ServerSocket serverSocket;
    private ItemValidation iv = new ItemValidation();
    private SocketServerThread socketServerThread;
    private LinearLayout llInfoContainer;
    private ImageView ivInfo;
    private TextView tvMac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        startService(new Intent(MainMenu.this, ServiceTimerServer.class));
        // for android > M
        if (ContextCompat.checkSelfPermission(
                MainMenu.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainMenu.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainMenu.this, android.Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                MainMenu.this, android.Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {

            MainMenu.super.requestAppPermissions(new
                            String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WAKE_LOCK, android.Manifest.permission.RECEIVE_BOOT_COMPLETED}, R.string
                            .runtime_permissions_txt
                    , REQUEST_PERMISSIONS);
        }

        doubleBackToExitPressedOnce = false;
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            if(bundle.getBoolean("exit", false)){
                exitState = true;
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
            }
        }
        initUI();

        // For Remote access
        //ServiceUtils.DEFAULT_PORT = ConnectionUtil.getPort(ServerActivity.this);
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        registerService(ServiceUtils.DEFAULT_PORT);
        initializeReceiver();
    }

    @Override
    public void onPermissionsGranted(int requestCode) {

    }

    private void initUI() {

        llTV = (LinearLayout) findViewById(R.id.ll_tv);
        llYoutube = (LinearLayout) findViewById(R.id.ll_yt);
        llNetflix = (LinearLayout) findViewById(R.id.ll_netflix);
        llIflix = (LinearLayout) findViewById(R.id.ll_iflix);
        ivTV = (ImageView) findViewById(R.id.iv_tv);
        ivYoutube = (ImageView) findViewById(R.id.iv_yt);
        ivNetFlix = (ImageView) findViewById(R.id.iv_netflix);
        ivIflix = (ImageView) findViewById(R.id.iv_iflix);
        llInfoContainer = (LinearLayout) findViewById(R.id.ll_info_container);
        ivInfo = (ImageView) findViewById(R.id.iv_info);
        tvMac = (TextView) findViewById(R.id.tv_mac);

        selectedChoise = 0;
        setHovered(selectedChoise);

        initEvent();
    }

    private void initEvent() {

        ivTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainMenu.this, ChanelList.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        ivYoutube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    Intent i = getPackageManager().getLaunchIntentForPackage(ServerURL.pnYoutube);
                    startActivity(i);
                } catch (Exception e) {

                    ApkInstaller atualizaApp = new ApkInstaller();
                    atualizaApp.setContext(MainMenu.this);
                    atualizaApp.execute(ServerURL.bwYoutubeForTV);
                }

                /*Intent intent = new Intent(MainMenu.this, YoutubePlayerActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);*/
            }
        });

        ivNetFlix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    Intent i = getPackageManager().getLaunchIntentForPackage(ServerURL.pnNetflix);
                    startActivity(i);
                } catch (Exception e) {

                    ApkInstaller atualizaApp = new ApkInstaller();
                    atualizaApp.setContext(MainMenu.this);
                    atualizaApp.execute(ServerURL.bwNetflix);
                }
            }
        });

        ivIflix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    Intent i = getPackageManager().getLaunchIntentForPackage(ServerURL.pnIflix);
                    startActivity(i);
                } catch (Exception e) {

                    ApkInstaller atualizaApp = new ApkInstaller();
                    atualizaApp.setContext(MainMenu.this);
                    atualizaApp.execute(ServerURL.bwIflix);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            Intent intent = new Intent(MainMenu.this, MainMenu.class);
            intent.putExtra("exit", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            //System.exit(0);
        }

        if(!exitState && !doubleBackToExitPressedOnce){
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getResources().getString(R.string.app_exit), Toast.LENGTH_SHORT).show();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int maxMenu = 4;
        switch (keyCode){
            /*case 19:
                if(selectedChoise - 2 >= 0){
                    selectedChoise = selectedChoise - 2;
                    setHovered(selectedChoise);
                }
                break;
            case 20:
                if(selectedChoise + 2 < maxMenu){
                    selectedChoise = selectedChoise + 2;
                    setHovered(selectedChoise);
                }
                break;*/
            case 21:
                if(selectedChoise - 1 >= 0){
                    selectedChoise = selectedChoise - 1;
                    setHovered(selectedChoise);
                }
                break;
            case 22:
                if(selectedChoise + 1 < maxMenu){
                    selectedChoise = selectedChoise + 1;
                    setHovered(selectedChoise);
                }
                break;
            case 23:
                goToSelectedChoise();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setHovered(int number){

        switch (number){
            case 0:
                ivTV.setHovered(true);
                ivYoutube.setHovered(false);
                ivNetFlix.setHovered(false);
                ivIflix.setHovered(false);
                break;
            case 1:
                ivTV.setHovered(false);
                ivYoutube.setHovered(true);
                ivNetFlix.setHovered(false);
                ivIflix.setHovered(false);
                break;
            case 2:
                ivTV.setHovered(false);
                ivYoutube.setHovered(false);
                ivNetFlix.setHovered(true);
                ivIflix.setHovered(false);
                break;
            case 3:
                ivTV.setHovered(false);
                ivYoutube.setHovered(false);
                ivNetFlix.setHovered(false);
                ivIflix.setHovered(true);
                break;
        }
    }

    private void goToSelectedChoise(){

        switch (selectedChoise){
            case 0:
                ivTV.performClick();
                break;
            case 1:
                ivYoutube.performClick();
                break;
            case 2:
                ivNetFlix.performClick();
                break;
            case 3:
                ivIflix.performClick();
                break;
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
                                            Toast.makeText(MainMenu.this, "Connected device " + ServiceUtils.lockedClient, Toast.LENGTH_LONG).show();
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
