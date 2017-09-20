package id.net.gmedia.gmediatv.Utils;

import android.net.Uri;

import java.net.URI;

/**
 * Created by Shin on 02/08/2017.
 */

public class ServerURL {

    public static String baseUrl = "http://gmediatv.gmedia.bz/";

    //public static String getLink = baseUrl + "api/link/get_link/";
    public static String getLink = baseUrl + "api/link/get_link_dummy/";

    public static String getLinkDummy = baseUrl + "api/link/get_link_dummy/";

    public static String getListYoutubeVideoURL(String nextPageToken, String pagePerRow, String keyword){

        return "https://www.googleapis.com/youtube/v3/search?pageToken=" + nextPageToken + "&maxResults=" + pagePerRow + "&part=snippet&chart=mostPopular&q="+ Uri.encode(keyword)+"+&type=video&key="+ GoogleAPI.YoutubeListAPIKey;
    }

    //Bloatware
    //Youtube
    public static String pnYoutube = "com.google.android.youtube.tv";
    public static String bwYoutubeForTV = baseUrl + "apk/"+pnYoutube+".apk";
    //Netflix
    public static String pnNetflix = "com.netflix.mediaclient";
    //public static String pnNetflix = "com.netflix.ninja";
    public static String bwNetflix = baseUrl + "apk/"+pnNetflix+".apk";
    //Iflix
    public static String pnIflix = "iflix.play";
    public static String bwIflix = baseUrl + "apk/"+pnIflix+".apk";
}
