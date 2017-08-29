package id.net.gmedia.gmediatv.Main.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import id.net.gmedia.gmediatv.R;


/**
 * Created by Shin on 1/8/2017.
 */

public class ListChanelAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private View listViewItem;
    private ItemValidation iv = new ItemValidation();
    public static int selectedPosition = 0;

    public ListChanelAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.cv_list_all_chanel, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private TextView tvTitle1;
        private ImageView ivPlay;
        private RelativeLayout rlContainer;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();

        if(convertView == null){
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.cv_list_all_chanel, null);
            holder.rlContainer = (RelativeLayout) convertView.findViewById(R.id.rl_container);
            holder.tvTitle1 = (TextView) convertView.findViewById(R.id.tv_title_1);
            holder.ivPlay = (ImageView) convertView.findViewById(R.id.iv_play);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        if(position == selectedPosition){

            holder.ivPlay.setVisibility(View.VISIBLE);
            holder.tvTitle1.setTextColor(context.getResources().getColor(R.color.red_playlist));
        }else{

            /*TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            holder.rlContainer.setBackgroundResource(outValue.resourceId);*/
            holder.ivPlay.setVisibility(View.GONE);
            holder.tvTitle1.setTextColor(context.getResources().getColor(R.color.color_white));
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvTitle1.setText((position + 1)+". "+ itemSelected.getItem2());
        return convertView;

    }
}
