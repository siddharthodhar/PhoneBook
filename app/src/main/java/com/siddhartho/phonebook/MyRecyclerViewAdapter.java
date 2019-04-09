package com.siddhartho.phonebook;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {

    ArrayList<String> namearr;
    ArrayList<String> numarr;
    View.OnClickListener call_listener;
    View.OnClickListener sms_listener;

    OnLongClickContactListener onLongClickContactListener;

    public MyRecyclerViewAdapter(ArrayList<String> namearr, ArrayList<String> numarr, View.OnClickListener call_listener, View.OnClickListener sms_listener, OnLongClickContactListener onLongClickContactListener) {
        this.namearr = namearr;
        this.numarr = numarr;
        this.call_listener = call_listener;
        this.sms_listener = sms_listener;
        this.onLongClickContactListener = onLongClickContactListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.activity_custom_item, parent, false);
        return new MyViewHolder(view, onLongClickContactListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.name.setText(namearr.get(position));
        holder.number.setText(numarr.get(position));

        holder.call.setTag(numarr.get(position));
        holder.call.setOnClickListener(call_listener);

        holder.sendsms.setTag(numarr.get(position));
        holder.sendsms.setOnClickListener(sms_listener);
    }

    @Override
    public int getItemCount() {
        return numarr.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView number;
        Button call;
        Button sendsms;

        public MyViewHolder(View view, final OnLongClickContactListener onLongClickContactListener) {
            super(view);

            name = (TextView) view.findViewById(R.id.textView_name);
            number = (TextView) view.findViewById(R.id.textView_number);
            call = (Button) view.findViewById(R.id.btn_call);
            sendsms = (Button) view.findViewById(R.id.btn_sms);

            name.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onLongClickContactListener.onlongClickContact(getAdapterPosition());
                    return true;
                }
            });

            number.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onLongClickContactListener.onlongClickContact(getAdapterPosition());
                    return true;
                }
            });
        }
    }

    public interface OnLongClickContactListener {
        void onlongClickContact(int position);
    }
}
