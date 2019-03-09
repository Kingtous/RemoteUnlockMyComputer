package com.kingtous.remotefingerunlock.DataStoreTool;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.kingtous.remotefingerunlock.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    int EDIT_BUTTON=0;
    int DELETE_BUTTON=1;

    ArrayList<RecordData> recordDataArrayList;

    public RecordAdapter(ArrayList<RecordData> list){
        recordDataArrayList =list;
    }


    //========接口============
    public interface OnItemClickListener{
        void OnClick(int type, RecordData recordData);
    }

    private RecordAdapter.OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(RecordAdapter.OnItemClickListener mOnLongItemClickListener){
        this.mOnItemClickListener = mOnLongItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layout= LayoutInflater.from(parent.getContext()).inflate(R.layout.record_list_item,parent,false);
        return new RecordAdapter.recordHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            ((recordHolder)holder).type.setText(recordDataArrayList.get(position).getType());
            ((recordHolder)holder).user.setText(recordDataArrayList.get(position).getUser());
            ((recordHolder)holder).mac.setText(recordDataArrayList.get(position).getMac());
            ((recordHolder)holder).toEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.OnClick(EDIT_BUTTON, recordDataArrayList.get(position));
                }
            });

            ((recordHolder)holder).toDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.OnClick(DELETE_BUTTON, recordDataArrayList.get(position));
                }
            });

        }

    @Override
    public int getItemCount() {
        return recordDataArrayList.size();
    }


    public class recordHolder extends RecyclerView.ViewHolder{

        public TextView type;
        public TextView user;
        public TextView mac;
        public Button toEdit;
        public Button toDelete;

        public recordHolder(@NonNull View itemView) {
            super(itemView);
            user=itemView.findViewById(R.id.record_user);
            mac=itemView.findViewById(R.id.record_mac);
            type=itemView.findViewById(R.id.record_type);
            toEdit=itemView.findViewById(R.id.record_edit);
            toDelete=itemView.findViewById(R.id.record_delete);

            }

        }
}
