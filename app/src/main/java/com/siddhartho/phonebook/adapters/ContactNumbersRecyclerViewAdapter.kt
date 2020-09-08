package com.siddhartho.phonebook.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.siddhartho.phonebook.databinding.CustomItemContactNumberBinding
import com.siddhartho.phonebook.dataclass.ContactNumber
import com.siddhartho.phonebook.utils.Constants

class ContactNumbersRecyclerViewAdapter(
    private val onCountryCodeClicked: (editText: EditText, holder: MyContactNumbersViewHolder) -> Unit,
    private val onNumberClicked: (editText: EditText, holder: MyContactNumbersViewHolder) -> Unit,
    private val onDeleteClicked: (holder: MyContactNumbersViewHolder) -> Unit
) :
    RecyclerView.Adapter<ContactNumbersRecyclerViewAdapter.MyContactNumbersViewHolder>() {

    private var contactNumbers: ArrayList<ContactNumber>? = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyContactNumbersViewHolder {
        Log.d(TAG, "onCreateViewHolder() called with: parent = $parent, viewType = $viewType")
        return MyContactNumbersViewHolder(
            CustomItemContactNumberBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount() called")
        return contactNumbers!!.size
    }

    override fun onBindViewHolder(holder: MyContactNumbersViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder() called with: holder = $holder, position = $position")
        if (position == contactNumbers?.size) {
            holder.customItemContactNumberBinding.editTextCountryCode.setText(Constants.DEFAULT_CC)
            holder.customItemContactNumberBinding.editTextNumber.setText("")
        } else {
            holder.customItemContactNumberBinding.editTextCountryCode.setText(
                contactNumbers?.get(
                    position
                )?.countryCode
            )
            holder.customItemContactNumberBinding.editTextNumber.setText(
                contactNumbers?.get(
                    position
                )?.number
            )
        }
        holder.customItemContactNumberBinding.editTextCountryCode.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus)
                onCountryCodeClicked(view as EditText, holder)
        }
        holder.customItemContactNumberBinding.editTextNumber.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus)
                onNumberClicked(view as EditText, holder)
        }
        holder.customItemContactNumberBinding.btnDeleteNumber.setOnClickListener {
            onDeleteClicked(holder)
        }
    }

    fun resetContactNumberList() {
        Log.d(TAG, "resetContactNumberList() called")
        contactNumbers?.clear()
    }

    fun addContactNumber(contactNumber: ContactNumber, position: Int?) {
        Log.d(
            TAG,
            "addContactNumber() called with: contactNumber = $contactNumber, position = $position"
        )
        contactNumbers?.add(contactNumber)
        position?.let { notifyItemInserted(it) }
    }

    fun removeContactNumber(position: Int?) {
        Log.d(TAG, "removeContactNumber() called with: position = $position")
        position?.let {
            if (it < contactNumbers?.size!!) {
                contactNumbers?.removeAt(it)
                notifyItemRemoved(position)
            }
        }
    }

    class MyContactNumbersViewHolder(val customItemContactNumberBinding: CustomItemContactNumberBinding) :
        RecyclerView.ViewHolder(customItemContactNumberBinding.root)

    companion object {
        private const val TAG = "ContactNumbersRVAdapter"
    }
}