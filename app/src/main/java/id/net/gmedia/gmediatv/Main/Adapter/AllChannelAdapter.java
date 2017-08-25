package id.net.gmedia.gmediatv.Main.Adapter;

/**
 * Created by Shin on 3/21/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ImageUtils;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import java.util.List;

import id.net.gmedia.gmediatv.Main.ChannelViewScreen;
import id.net.gmedia.gmediatv.R;
import id.net.gmedia.gmediatv.Youtube.YoutubePlayerActivity;

public class AllChannelAdapter extends RecyclerView.Adapter<AllChannelAdapter.MyViewHolder> {

    private Context context;
    private List<CustomItem> masterList;
    public static int position = 0;
    private ItemValidation iv = new ItemValidation();
    public static int selectedPosition = 0;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout rlContainer;
        public TextView tvTitle1;
        public ImageView ivPlay;

        public MyViewHolder(View view) {

            super(view);
            rlContainer = (RelativeLayout) view.findViewById(R.id.rl_container);
            tvTitle1 = (TextView) view.findViewById(R.id.tv_title_1);
            ivPlay = (ImageView) view.findViewById(R.id.iv_play);
        }
    }

    public AllChannelAdapter(Context context, List masterlist){
        this.context = context;
        this.masterList = masterlist;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cv_list_all_chanel, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final CustomItem cli = masterList.get(position);

        if(position == selectedPosition){

            holder.ivPlay.setVisibility(View.VISIBLE);
            holder.tvTitle1.setTextColor(context.getResources().getColor(R.color.red_playlist));
        }else{

            holder.ivPlay.setVisibility(View.GONE);
            holder.tvTitle1.setTextColor(context.getResources().getColor(R.color.color_white));
        }

        holder.tvTitle1.setText(cli.getItem2());

        holder.rlContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selectedPosition = position;
                notifyDataSetChanged();

                Intent intent = new Intent(context, ChannelViewScreen.class);
                intent.putExtra("nama", cli.getItem2());
                intent.putExtra("link", cli.getItem3());
                context.startActivity(intent);
                ((Activity)context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    @Override
    public int getItemCount() {
        return masterList.size();
    }

}