package com.hover.stax.pins;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.channels.Channel;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public  class PinEntryAdapter extends  RecyclerView.Adapter<PinEntryAdapter.PinEntryViewHolder> {
    private List<Channel> channels;

    PinEntryAdapter(List<Channel> channels) {
        this.channels = channels;
    }

    List<Channel> retrieveChannels() {return channels;}

    @NonNull
    @Override
    public PinEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.variables_items, parent, false);
        return new PinEntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final @NonNull PinEntryViewHolder holder, int position) {
        Channel channel = channels.get(position);

        holder.view.setTag(channel.id);
        holder.labelView.setText(channel.name);
        Log.d("PIN SET",  channel.pin == null ? "null" : channel.pin);
        //holder.circleImageView.setImageBitmap(logo);
//        holder.circleImageView.setVisibility(View.VISIBLE);

        if(channel.pin !=null && !channel.pin.isEmpty()) {
            holder.editView.setHint("XXXX");
        }
        holder.editView.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) { }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                channel.pin = s.toString();
            }
        });
    }

    static class PinEntryViewHolder extends  RecyclerView.ViewHolder {
        final TextView labelView;
        final CircleImageView circleImageView;
        final EditText editView;
        final View view;

        PinEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            labelView = itemView.findViewById(R.id.variable_label_id);
            editView = itemView.findViewById(R.id.variableEditId);
            circleImageView = itemView.findViewById(R.id.variable_logo);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() { return channels == null ? 0 : channels.size(); }
}

