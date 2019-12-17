package id.net.gmedia.gmediatv.Youtube.Adapter;

/**
 * Created by Shin on 3/21/2017.
 */

import android.content.Context;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ImageUtils;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import id.net.gmedia.gmediatv.R;
import id.net.gmedia.gmediatv.Youtube.YoutubePlayerActivity;

public class YoutubeListAdapter extends RecyclerView.Adapter<YoutubeListAdapter.MyViewHolder> {

    private Context context;
    private List<CustomItem> masterList;
    public static int position = 0;
    private ItemValidation iv = new ItemValidation();

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView ivThumbnail;
        public TextView tvTitle;
        public LinearLayout llPlay, llColor;
        public CardView cvContainer;

        public MyViewHolder(View view) {
            super(view);
            cvContainer = (CardView) view.findViewById(R.id.cv_container);
            ivThumbnail = (ImageView) view.findViewById(R.id.iv_thumnail);
            tvTitle = (TextView) view.findViewById(R.id.tv_title);
            llPlay = (LinearLayout) view.findViewById(R.id.ll_play);
            llColor = (LinearLayout) view.findViewById(R.id.ll_color);
        }
    }

    public YoutubeListAdapter(Context context, List masterlist){
        this.context = context;
        this.masterList = masterlist;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cv_youtube_list, parent, false);

        return new MyViewHolder(itemView);
    }

    public void addItem(List<CustomItem> itemToAdd){

        this.masterList.addAll(itemToAdd);
        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final CustomItem cli = masterList.get(position);
        holder.tvTitle.setText(cli.getItem2());
        ImageUtils iu = new ImageUtils();

        /*if(holder.ivThumbnail.getDrawable() == null){
            iu.LoadRealImage(context, cli.getItem3(), holder.ivThumbnail, R.mipmap.ic_play);
            //holder.ivThumbnail.setImageURI(Uri.parse(cli.getItem3()));
        }*/

        int[] display = iv.getScreenResolution(context);
        int height = display[1] / 4;

        LinearLayout.LayoutParams oldLayoutParams = (LinearLayout.LayoutParams) holder.cvContainer.getLayoutParams();
        LinearLayout.LayoutParams newLayoutParams = new LinearLayout.LayoutParams(oldLayoutParams.width, height);
        //newLayoutParams.setMargins(0, iv.dpToPx(context, -16), 0, iv.dpToPx(context, -16));

        holder.cvContainer.setLayoutParams(newLayoutParams);

        iu.LoadRealImageNoCache(context, cli.getItem3(), holder.ivThumbnail);

        holder.llPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                YoutubePlayerActivity.playVideo(cli.getItem1(), position);
            }
        });

        holder.cvContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                YoutubePlayerActivity.playVideo(cli.getItem1(), position);
            }
        });

        if(this.position == position){

            holder.llColor.setBackground(context.getResources().getDrawable(R.drawable.background_radian_red));
        }else{
            holder.llColor.setBackground(context.getResources().getDrawable(R.drawable.background_radian_black));
        }
    }

    @Override
    public int getItemCount() {
        return masterList.size();
    }

}