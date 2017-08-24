package id.net.gmedia.gmediatv.DaftarVideo.Adapter;

/**
 * Created by Shin on 3/21/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import id.net.gmedia.gmediatv.MainActivity;
import id.net.gmedia.gmediatv.R;
import com.maulana.custommodul.CustomItem;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.MyViewHolder> {

    private Context context;
    private List<CustomItem> masterList;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public CardView cvContainer;
        public TextView tvTitle, tvDuration, tvFormat;
        public LinearLayout llTampil;

        public MyViewHolder(View view) {
            super(view);
            cvContainer = (CardView) view.findViewById(R.id.cv_container);
            tvTitle = (TextView) view.findViewById(R.id.tv_title);
            tvDuration = (TextView) view.findViewById(R.id.tv_duration);
            tvFormat = (TextView) view.findViewById(R.id.tv_format);
            llTampil = (LinearLayout) view.findViewById(R.id.ll_tampil);
        }
    }

    public VideoListAdapter(Context context, List masterlist){
        this.context = context;
        this.masterList = masterlist;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cv_list_video, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final CustomItem cli = masterList.get(position);
        holder.tvTitle.setText(cli.getItem2());
        holder.tvDuration.setText(cli.getItem3());
        holder.tvFormat.setText(cli.getItem4());
        holder.llTampil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("url", cli.getItem5());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return masterList.size();
    }

}