package com.siddhartho.phonebook.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;

import com.siddhartho.phonebook.databinding.CustomItemContactBinding;
import com.siddhartho.phonebook.dataclass.ContactNumber;
import com.siddhartho.phonebook.dataclass.ContactWithContactNumbers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContactsRecyclerViewAdapter extends RecyclerView.Adapter<ContactsRecyclerViewAdapter.MyContactsViewHolder> implements Filterable {
    private static final String TAG = "ContactsRVAdapter";

    private OnLongClickContactListener onLongClickContactListener;
    private OnContactNumbersReceiveListener onContactNumbersReceiveListener;
    private final List<ContactWithContactNumbers> contacts, fullContacts;
    private final int EXTRA_VIEWS = 2;

    public ContactsRecyclerViewAdapter() {
        contacts = new ArrayList<>();
        fullContacts = new ArrayList<>();
    }

    @Override
    public Filter getFilter() {
        Log.d(TAG, "getFilter() called");
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                Log.d(TAG, "performFiltering() called with: constraint = [" + constraint + "]");
                List<ContactWithContactNumbers> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0)
                    filteredList.addAll(fullContacts);
                else for (ContactWithContactNumbers contact
                        : fullContacts)
                    if (contact.getContact() != null
                            && Objects.requireNonNull(
                            contact.getContact().getName()).toLowerCase().contains(constraint.toString().toLowerCase().trim()))
                        filteredList.add(contact);
                    else if (contact.getContactNumbers() != null) for (ContactNumber number :
                            contact.getContactNumbers())
                        if (number.getCountryCode() != null
                                && number.getNumber() != null
                                && (number.getCountryCode().contains(constraint.toString().toLowerCase().trim())
                                || number.getNumber().contains(constraint.toString().toLowerCase().trim()))) {
                            filteredList.add(contact);
                            break;
                        }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                Log.d(TAG, "publishResults() called with: constraint = [" + constraint + "], results = [" + results + "]");
                //noinspection unchecked
                ArrayList<ContactWithContactNumbers> resultsList = (ArrayList<ContactWithContactNumbers>) results.values;
                for (ContactWithContactNumbers contact :
                        fullContacts)
                    if (!resultsList.contains(contact) && contacts.contains(contact)) {
                        notifyItemRemoved(contacts.indexOf(contact));
                        contacts.remove(contact);
                    } else if (resultsList.contains(contact) && !contacts.contains(contact)) {
                        contacts.add(resultsList.indexOf(contact), contact);
                        notifyItemInserted(contacts.indexOf(contact));
                    }
            }
        };
    }

    @NonNull
    @Override
    public MyContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder() called with: parent = [" + parent + "], viewType = [" + viewType + "]");
        return new MyContactsViewHolder(CustomItemContactBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false), onLongClickContactListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyContactsViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder() called with: holder = [" + holder + "], position = [" + position + "] listSize = [" + contacts.size() + "]");
        if (position >= contacts.size()) {
            holder.customItemContactBinding.textViewName.setText("");
            holder.customItemContactBinding.linearLayoutContactNumber.removeAllViews();
        } else {
            holder.customItemContactBinding.textViewName.setText(Objects.requireNonNull(contacts.get(position).getContact()).getName());
            onContactNumbersReceiveListener.onReceive(
                    holder.customItemContactBinding.linearLayoutContactNumber, contacts.get(position).getContactNumbers());
        }
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount() called");
        return contacts.size() + EXTRA_VIEWS;
    }

    public void setOnLongClickContactListener(OnLongClickContactListener onLongClickContactListener) {
        Log.d(TAG, "setOnLongClickContactListener() called with: onLongClickContactListener = [" + onLongClickContactListener + "]");
        this.onLongClickContactListener = onLongClickContactListener;
    }

    public void setOnContactNumbersReceiveListener(OnContactNumbersReceiveListener onContactNumbersReceiveListener) {
        Log.d(TAG, "setOnContactNumbersReceiveListener() called with: onContactNumbersReceiveListener = [" + onContactNumbersReceiveListener + "]");
        this.onContactNumbersReceiveListener = onContactNumbersReceiveListener;
    }

    public void addContactToList(ContactWithContactNumbers contactWithContactNumbers, int position) {
        Log.d(TAG, "addContactToList() called with: contactWithContactNumbers = [" + contactWithContactNumbers + "], position = [" + position + "]");
        contacts.add(position, contactWithContactNumbers);
        fullContacts.add(position, contactWithContactNumbers);
        notifyItemInserted(position);
    }

    public void addTrailingViewsAtEnd() {
        Log.d(TAG, "addTrailingViewsAtEnd() called");
        for (int i = 0; i < EXTRA_VIEWS; i++)
            notifyItemInserted(contacts.size() + i);
    }

    public void replaceContactToList(ContactWithContactNumbers contactWithContactNumbers, int position) {
        Log.d(TAG, "replaceContactToList() called with: contactWithContactNumbers = [" + contactWithContactNumbers + "], position = [" + position + "]");
        contacts.remove(position);
        fullContacts.remove(position);
        contacts.add(position, contactWithContactNumbers);
        fullContacts.add(position, contactWithContactNumbers);
        notifyItemChanged(position);
    }

    public void removeContactFromList(int position) {
        Log.d(TAG, "removeContactFromList() called with: position = [" + position + "]");
        contacts.remove(position);
        fullContacts.remove(position);
        notifyItemRemoved(position);
    }

    public interface OnLongClickContactListener {
        void onLongClickContact(ContactWithContactNumbers contactWithContactNumbers, int position);
    }

    public interface OnContactNumbersReceiveListener {
        void onReceive(LinearLayout linearLayout, List<ContactNumber> contactNumbers);
    }

    class MyContactsViewHolder extends RecyclerView.ViewHolder {

        CustomItemContactBinding customItemContactBinding;

        MyContactsViewHolder(CustomItemContactBinding customItemContactBinding, OnLongClickContactListener onLongClickContactListener) {
            super(customItemContactBinding.getRoot());
            Log.d(TAG, "MyViewHolder() called with: customItemContactBinding = [" + customItemContactBinding + "], onLongClickContactListener = [" + onLongClickContactListener + "]");
            this.customItemContactBinding = customItemContactBinding;

            customItemContactBinding.getRoot().setOnLongClickListener(v -> {
                if (onLongClickContactListener != null) {
                    onLongClickContactListener.onLongClickContact(contacts.get(getAdapterPosition()), getAdapterPosition());
                    return true;
                }
                return false;
            });
        }
    }
}
