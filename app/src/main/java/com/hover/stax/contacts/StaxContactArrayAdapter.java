package com.hover.stax.contacts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hover.stax.databinding.StaxSpinner2lineBinding;

import java.util.ArrayList;
import java.util.List;

public class StaxContactArrayAdapter extends ArrayAdapter<StaxContact> {

    private final List<StaxContact> allContacts;
    private final List<StaxContact> filteredContacts;

    public StaxContactArrayAdapter(@NonNull Context context, List<StaxContact> list) {
        super(context, 0, list);
        allContacts = new ArrayList<>(list);
        filteredContacts = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View v, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (v == null) {
            StaxSpinner2lineBinding binding = StaxSpinner2lineBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            v = binding.getRoot();

            holder = new ViewHolder(binding);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        StaxContact c = filteredContacts.get(position);
        holder.title.setText(c.shortName());
        holder.subtitle.setText(c.getPhoneNumber());
        holder.subtitle.setVisibility(c.hasName() ? View.VISIBLE : View.GONE);

        return v;
    }

    static class ViewHolder {
        TextView title;
        TextView subtitle;

        public ViewHolder(StaxSpinner2lineBinding binding) {
            title = binding.title;
            subtitle = binding.subtitle;
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                List<StaxContact> filteredContacts = new ArrayList<>();
                if (constraint != null) {
                    for (StaxContact contact : allContacts) {
                        if (contact.toString().replaceAll(" ", "").toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filteredContacts.add(contact);
                        }
                    }
                    filterResults.values = filteredContacts;
                    filterResults.count = filteredContacts.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredContacts.clear();
                if (results != null && results.count > 0) {
                    // avoids unchecked cast warning when using filteredContacts.addAll((ArrayList<StaxContact>) results.values);
                    for (Object object : (List<?>) results.values) {
                        if (object instanceof StaxContact) {
                            filteredContacts.add((StaxContact) object);
                        }
                    }
                    notifyDataSetChanged();
                } else if (constraint == null) {
                    // no filter, add entire original list back in
                    filteredContacts.addAll(allContacts);
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    public int getCount() {
        return filteredContacts.size();
    }

    public StaxContact getItem(int position) {
        return filteredContacts.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
}