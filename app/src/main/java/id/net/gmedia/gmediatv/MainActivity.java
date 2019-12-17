package id.net.gmedia.gmediatv;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;

import id.net.gmedia.gmediatv.RemoteUtils.ServiceUtils;

public class MainActivity extends AppCompatActivity {

    private VideoView vvLoad;
    private LinearLayout llPlay, llDownload;
    private MediaController mediaController;
    private ProgressDialog progressDialog;
    private List<String> videoList;
    private ProgressBar pbLoading;
    private String videoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
    }

    private void initUI() {

        vvLoad = (VideoView) findViewById(R.id.vv_load);
        llPlay = (LinearLayout) findViewById(R.id.ll_play);
        llDownload = (LinearLayout) findViewById(R.id.ll_download);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        pbLoading.setVisibility(View.GONE);


        ServiceUtils.lockedClient = "";
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            videoPath = bundle.getString("url");
        }

        Uri uri= Uri.parse(videoPath);
        vvLoad.setVideoURI(uri);
        vvLoad.seekTo(300);
        mediaController = new MediaController(this);
        vvLoad.setMediaController(mediaController);
        initEvent();

        getVideoData();
    }

    private void getVideoData() {

    }

    private void initEvent() {

        llPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbLoading.setVisibility(View.VISIBLE);
                playVideo();
            }
        });

        llDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DownloadFileFromURL().execute(videoPath);
            }
        });
    }

    private void playVideo(){
        vvLoad.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                pbLoading.setVisibility(View.GONE);
                mp.setLooping(true);
                vvLoad.start();
                mediaController.show();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        pbLoading.setVisibility(View.VISIBLE);
        playVideo();
    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        String extension;
        boolean success;
        String videoName;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog();
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                // this will be useful so that you can show a tipical 0-100% progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                extension = f_url[0].substring(f_url[0].lastIndexOf("."));
                File vidDir = new File(android.os.Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_MOVIES) + File.separator + "GmediaTV");
                vidDir.mkdirs();

                // create unique identifier
                Date date = new Date();
                // create file name
                videoName = "GmediaTV_" + date.getYear()+date.getMonth()+date.getDate()+date.getHours()+date.getMinutes()+date.getSeconds()+extension;
                File fileVideo = new File(vidDir.getAbsolutePath(), videoName);
                success = false;
                InputStream input = new BufferedInputStream(url.openStream());
                // Output stream
                OutputStream output = new FileOutputStream(fileVideo);
                byte data[] = new byte[1024];
                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress(""+(int)((total*100)/lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                    success = true;
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            progressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task
         * Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {

            dismissDialog();

            //String downloadedPath = "file://" + Environment.getExternalStorageDirectory().toString() + "/downloadedfile."+extension;

            showFinishDialog(success);
        }
        private void showDialog(){
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Downloading file. Please wait...");
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();
        }

        private void dismissDialog(){
            progressDialog.dismiss();
        }

        private void showFinishDialog(boolean downloadSuccess){

            String title, message;
            if(downloadSuccess){
                title = "Download finished";
                message = "Video "+ videoName+ " berhasil di download";
            }else {
                title = "Download failed";
                message = "Tidak dapat mengunduh video ini, silahkan coba kembali";
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                    .setTitle(title)
                    .setMessage(message)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.show();

        }
    }
}
