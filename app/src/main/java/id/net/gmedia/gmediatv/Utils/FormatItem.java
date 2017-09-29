package id.net.gmedia.gmediatv.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Shin on 08/09/2017.
 */

public class FormatItem {

    public static final String formatDate = "yyyy-MM-dd";
    public static final String formatTimestamp = "yyyy-MM-dd HH:mm:ss";
    public static final String formatTime = "HH:mm:ss";
    public static final String formatDateDisplay = "dd/MM/yyyy";

    public static String getMacAddress(){
        try {
            return loadFileAsString("/sys/class/net/eth0/address")
                    .toUpperCase().substring(0, 17);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String loadFileAsString(String filePath) throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }
}
