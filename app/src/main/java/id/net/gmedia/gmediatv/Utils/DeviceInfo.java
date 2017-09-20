package id.net.gmedia.gmediatv.Utils;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * Created by Shin on 06/09/2017.
 */

public class DeviceInfo {

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    //res1.append(Integer.toHexString(b & 0xFF) + ":");
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "00:00:00:00:00:00";
    }
}
