package com.kingtous.remotefingerunlock.BluetoothConnectTool;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kingtous.remotefingerunlock.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;



public class BluetoothRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    int footer_postion=0;
    //原来加了footer的，现在不需要了
    int TYPE_FOOTER=0;
    int TYPE_DEVICES=1;

    ArrayList<BluetoothDeviceData> devices;
    View footer;

    BluetoothRecyclerAdapter(ArrayList<BluetoothDeviceData> list){
       devices=list;
    }


    //========接口============
    public interface OnItemClickListener{
        void OnClick(View view,int Position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener){
        this.mOnItemClickListener = mOnItemClickListener;
    }
    //=========暴露接口=========

    public View getFooter() {
        return footer;
    }

    public void setFooter(View footer) {
        this.footer = footer;
        notifyItemInserted(getItemCount()-1);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==footer_postion && footer!=null)
        {
            return new deviceHolder(footer);
        }
        View layout= LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_device_item,parent,false);
        return new deviceHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position)==TYPE_DEVICES){
            ((deviceHolder)holder).name.setText(devices.get(position).getName());
            ((deviceHolder)holder).mac.setText(devices.get(position).getMac());
            //=======通过接口回调===========
            ((deviceHolder) holder).cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.OnClick(((deviceHolder) holder).cardView,position);
                }
            });
        }
        else if (getItemViewType(position)==TYPE_FOOTER)
        {
            return;
        }
        else {
            return;
        }



    }

    @Override
    public int getItemCount() {
        if (footer!=null)
        {
            return devices.size()+1;
        }
        else
            return devices.size();
    }

    @Override
    public int getItemViewType(int position) {

//        if (position==getItemCount()-1){
//            //最后是footer
//            return TYPE_FOOTER;
//        }
        return TYPE_DEVICES;
    }

    public class deviceHolder extends RecyclerView.ViewHolder{

        public CardView cardView;
        public TextView name;
        public TextView mac;

        public deviceHolder(@NonNull View itemView) {
            super(itemView);
            if (itemView==footer)
                return;
            else {
                name=itemView.findViewById(R.id.name_bluetooth_device_name);
                mac=itemView.findViewById(R.id.name_bluetooth_device_mac);
                cardView=itemView.findViewById(R.id.card_BLUETOOTH);
            }
        }
    }

}
