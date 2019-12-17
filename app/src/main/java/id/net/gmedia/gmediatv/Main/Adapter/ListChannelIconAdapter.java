package id.net.gmedia.gmediatv.Main.Adapter;

/**
 * Created by Shin on 3/21/2017.
 */

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ImageUtils;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import id.net.gmedia.gmediatv.Main.ChannelViewScreen;
import id.net.gmedia.gmediatv.R;

public class ListChannelIconAdapter extends RecyclerView.Adapter<ListChannelIconAdapter.MyViewHolder> {

    private Context context;
    private List<CustomItem> masterList;
    public static int position = 0;
    private ItemValidation iv = new ItemValidation();
    public static int selectedPosition = 0;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout rlMater, rlContainer;
        public TextView tvItem1, tvItem2;
        public ImageView ivIcon;

        public MyViewHolder(View view) {

            super(view);
            rlContainer = (RelativeLayout) view.findViewById(R.id.rl_container);
            rlMater = (RelativeLayout) view.findViewById(R.id.rv_master);
            tvItem1 = (TextView) view.findViewById(R.id.tv_item1);
            tvItem2 = (TextView) view.findViewById(R.id.tv_item2);
            ivIcon = (ImageView) view.findViewById(R.id.iv_icon);
        }
    }

    public ListChannelIconAdapter(Context context, List masterlist){
        this.context = context;
        this.masterList = masterlist;
    }

    @Override
    public long getItemId(int position) {
        setHasStableIds(true);
        return super.getItemId(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_chanel_icon, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final CustomItem cli = masterList.get(position);

        int[] display = iv.getScreenResolution(context);

        RelativeLayout.LayoutParams paramsMaster = new RelativeLayout.LayoutParams((display[1]/5), (display[1]/4) + iv.dpToPx(context,20));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((display[1]/6), (display[1]/6));
        RelativeLayout.LayoutParams paramText = (RelativeLayout.LayoutParams) holder.tvItem2.getLayoutParams();
        paramsMaster.setMargins(0, 0, 0, 0);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ABOVE, holder.tvItem2.getId());
        //paramText.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        //paramText.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        holder.rlMater.setLayoutParams(paramsMaster);
        //holder.rlMater.setBackground(context.getResources().getDrawable(R.drawable.background_radian_black));
        //Log.d("holder", "onBindViewHolder: "+ holder.rlMater.getMeasuredHeight());
        //holder.rlContainer.setLayoutParams(params);

        //paramText.width = (display[1]/7);
        //holder.tvItem2.setLayoutParams(paramText);

        if(position == selectedPosition){

            holder.rlContainer.setBackground(context.getResources().getDrawable(R.drawable.background_radian_red2));
            holder.tvItem2.setVisibility(View.VISIBLE);
            holder.tvItem2.setText(cli.getItem2());
        }else{

            holder.rlContainer.setBackground(context.getResources().getDrawable(R.drawable.background_normal));
            holder.tvItem2.setVisibility(View.INVISIBLE);
        }

        if(cli.getItem4().length() > 0){

            holder.ivIcon.setVisibility(View.VISIBLE);
            holder.tvItem1.setVisibility(View.GONE);
            ImageUtils iu = new ImageUtils();
            //iu.LoadGIFImage(context, cli.getItem4(), holder.ivIcon);
            iu.LoadRealImageNoCacheTV(context, cli.getItem4(), holder.ivIcon);
        }else{

            holder.ivIcon.setVisibility(View.GONE);
            holder.tvItem1.setVisibility(View.VISIBLE);
            holder.tvItem1.setText(cli.getItem2().substring(0,1));
        }

        holder.rlContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selectedPosition = position;
                notifyDataSetChanged();
                ChannelViewScreen.playVideo(context, cli.getItem2(), cli.getItem3());
            }
        });
    }

    @Override
    public int getItemCount() {
        return masterList.size();
    }

}