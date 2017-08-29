package id.net.gmedia.gmediatv.Utils;

import android.net.Uri;

import java.net.URI;

/**
 * Created by Shin on 02/08/2017.
 */

public class ServerURL {

    public static String baseUrl = "http://gmediatv.gmedia.bz/";

    public static String getLink = baseUrl + "api/link/get_link/";

    public static String getListYoutubeVideoURL(String nextPageToken, String pagePerRow, String keyword){

        return "https://www.googleapis.com/youtube/v3/search?pageToken=" + nextPageToken + "&maxResults=" + pagePerRow + "&part=snippet&chart=mostPopular&q="+ Uri.encode(keyword)+"+&type=video&key="+ GoogleAPI.YoutubeListAPIKey;
    }
}
