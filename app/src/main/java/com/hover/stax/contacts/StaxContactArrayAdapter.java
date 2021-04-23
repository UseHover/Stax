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

import com.hover.stax.R;

import java.util.ArrayList;
import java.util.List;

public class StaxContactArrayAdapter extends ArrayAdapter<StaxContact> {

    private List<StaxContact> allContacts;
    private final List<StaxContact> filteredContacts;
    private static int resource = R.layout.stax_spinner_2line;

    public StaxContactArrayAdapter(@NonNull Context context, List<StaxContact> list) {
        super(context, resource, list);
        allContacts = new ArrayList<>(list);
        filteredContacts = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View v, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (v == null) {
            v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);

            holder = new ViewHolder(v);
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

    private static class ViewHolder {

        TextView title, subtitle;

        public ViewHolder(View view) {
            title = (TextView) view.findViewById(R.id.title);
            subtitle = (TextView) view.findViewById(R.id.subtitle);
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
        return filteredContacts.isEmpty() ? null : filteredContacts.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
}