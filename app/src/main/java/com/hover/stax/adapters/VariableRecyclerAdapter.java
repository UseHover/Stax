package com.hover.stax.adapters;

import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.interfaces.VariableEditinterface;
import com.hover.stax.models.ChoosePinModel;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public  class VariableRecyclerAdapter extends  RecyclerView.Adapter<VariableRecyclerAdapter.VariableItemListView> {
    private ArrayList<ChoosePinModel> choosePinModelList;
    private VariableEditinterface editinterface;
    private boolean withLogo;

    public VariableRecyclerAdapter(ArrayList<ChoosePinModel> choosePinModelList, VariableEditinterface editinterface, boolean withLogo) {
        this.choosePinModelList = choosePinModelList;
        this.editinterface = editinterface;
        this.withLogo = withLogo;
    }

    @NonNull
    @Override
    public VariableItemListView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.variables_items, parent, false);
        return new VariableItemListView(view);
    }

    @Override
    public void onBindViewHolder(final @NonNull VariableItemListView holder, int position) {
        String label = choosePinModelList.get(position).getLabel();
        String value = choosePinModelList.get(position).getCurrentValue();
        String serviceId = choosePinModelList.get(position).getServiceId();
        boolean hasData = choosePinModelList.get(position).isHasValue();
        Bitmap logo = choosePinModelList.get(position).getServiceLogo();

        holder.view.setTag(serviceId);
        holder.labelText.setText(label);
        holder.editText.setHint(value);
        if(withLogo) {
            //holder.circleImageView.setImageBitmap(logo);
            holder.circleImageView.setVisibility(View.VISIBLE);
        }


        if(hasData){
            holder.editText.setText(value);
        }

        holder.editText.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) { }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                editinterface.onEditStringChanged(label, s.toString());
            }
        });
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
    public int getItemCount() {
        if(choosePinModelList == null) return 0;
        return choosePinModelList.size();
    }

    static class VariableItemListView extends  RecyclerView.ViewHolder {
        final TextView labelText;
        final CircleImageView circleImageView;
        final EditText editText;
        final View view;
        VariableItemListView(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            labelText = itemView.findViewById(R.id.variable_label_id);
            editText = itemView.findViewById(R.id.variableEditId);
            circleImageView = itemView.findViewById(R.id.variable_logo);

        }
    }

}

