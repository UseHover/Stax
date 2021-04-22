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
        v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);

        StaxContact c = filteredContacts.get(position);

        ((TextView) v.findViewById(R.id.title)).setText(c.shortName());
        ((TextView) v.findViewById(R.id.subtitle)).setText(c.getPhoneNumber());
        v.findViewById(R.id.subtitle).setVisibility(c.hasName() ? View.VISIBLE : View.GONE);

        return v;
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