package com.siddhartho.phonebook.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.siddhartho.phonebook.R
import com.siddhartho.phonebook.adapters.ContactNumbersRecyclerViewAdapter
import com.siddhartho.phonebook.databasecomponent.ContactsViewModelFactory
import com.siddhartho.phonebook.databinding.ActivityAddContactsBinding
import com.siddhartho.phonebook.dataclass.Contact
import com.siddhartho.phonebook.dataclass.ContactNumber
import com.siddhartho.phonebook.dataclass.ContactWithContactNumbers
import com.siddhartho.phonebook.repository.ContactsRepository
import com.siddhartho.phonebook.utils.Constants
import com.siddhartho.phonebook.utils.showToast
import com.siddhartho.phonebook.viewmodel.ContactsViewModel
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class AddContactsActivity : AppCompatActivity() {
    private var activityAddContactsBinding: ActivityAddContactsBinding? =
        null
    private var contactsViewModel: ContactsViewModel? = null
    private var contactWithContactNumbers: ContactWithContactNumbers? =
        ContactWithContactNumbers(
            Contact(""),
            arrayListOf(ContactNumber(null, Constants.DEFAULT_CC, ""))
        )
    private val contactNumbersToDelete: ArrayList<ContactNumber>? = ArrayList()
    private val contactNumbersRecyclerViewAdapter = ContactNumbersRecyclerViewAdapter(
        onCountryCodeClicked = ::onCountryCodeClicked,
        onNumberClicked = ::onNumberClicked,
        onDeleteClicked = ::onDeleteClicked
    )
    private val disposables =
        CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(
            TAG,
            "onCreate() called with: savedInstanceState = [$savedInstanceState]"
        )
        activityAddContactsBinding =
            ActivityAddContactsBinding.inflate(
                layoutInflater
            )
        setContentView(activityAddContactsBinding?.root)
        setSupportActionBar(activityAddContactsBinding?.toolbarAddContact)
        contactsViewModel = ViewModelProvider(
            this,
            ContactsViewModelFactory(
                ContactsRepository(
                    this
                )
            )
        ).get(
            ContactsViewModel::class.java
        )

        setObserversAndListeners()

        val savedContactWithContactNumbers =
            savedInstanceState?.getParcelable<ContactWithContactNumbers>(CONTACT_BUNDLE_KEY)
        if (savedContactWithContactNumbers != null)
            populateContactDetailsToUI(savedContactWithContactNumbers)
        else populateContactDetailsToUI(getContactFromIntent())
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
        if (ACTIVITY_STOPPED)
            contactWithContactNumbers?.contactNumbers?.add(getNewContactNumber())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState() called with: outState = $outState")
        contactWithContactNumbers?.contactNumbers?.remove(getNewContactNumber())
        outState.putParcelable(CONTACT_BUNDLE_KEY, contactWithContactNumbers)
        super.onSaveInstanceState(outState)
    }

    private fun setObserversAndListeners() {
        Log.d(TAG, "setObserversAndListeners() called")
        activityAddContactsBinding?.toolbarAddContact?.setNavigationOnClickListener {
            Log.d(TAG, "setObserversAndListeners() called navigation icon clicked")
            onBackPressed()
        }

        activityAddContactsBinding?.autoCompleteTextViewName?.addTextChangedListener(object :
            TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                Log.d(TAG, "autoCompleteTextViewName: afterTextChanged() called with: p0 = $p0")
                Flowable.just(p0.toString())
                    .debounce(3, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap {
                        Log.d(TAG, "setObserversAndListeners: editTextName flatMap $it")
                        contactWithContactNumbers?.contact?.name = it
                        contactsViewModel?.getContactNames(it)
                            ?.subscribeOn(Schedulers.io())
                            ?.observeOn(AndroidSchedulers.mainThread())
                    }
                    .subscribe({
                        Log.d(TAG, "setObserversAndListeners: getContactNames -> onNext() $it")
                        activityAddContactsBinding?.autoCompleteTextViewName?.setAdapter(
                            ArrayAdapter(
                                this@AddContactsActivity,
                                android.R.layout.simple_dropdown_item_1line,
                                it
                            )
                        )
                    }, {
                        Log.e(
                            TAG,
                            "setObserversAndListeners: getContactNames -> onError() ${it.message}",
                            it
                        )
                        showToast(resources.getString(R.string.error_try_again))
                    })?.let { disposables.add(it) }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(
                    TAG,
                    "autoCompleteTextViewName: beforeTextChanged() called with: p0 = $p0, p1 = $p1, p2 = $p2, p3 = $p3"
                )
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(
                    TAG,
                    "autoCompleteTextViewName: onTextChanged() called with: p0 = $p0, p1 = $p1, p2 = $p2, p3 = $p3"
                )
            }
        })

        activityAddContactsBinding?.autoCompleteTextViewName?.setOnItemClickListener { _, view, _, _ ->
            Log.d(
                TAG,
                "setObserversAndListeners: editTextName setOnItemClickListener() called with: _, view = $view, _, _"
            )
            activityAddContactsBinding?.autoCompleteTextViewName?.setText(
                (view as TextView).text.toString()
            )
            activityAddContactsBinding?.autoCompleteTextViewName?.setSelection(
                activityAddContactsBinding?.autoCompleteTextViewName?.text.toString().length
            )
            contactWithContactNumbers?.contact?.name =
                activityAddContactsBinding?.autoCompleteTextViewName?.text.toString()
        }


        activityAddContactsBinding?.btnSubmit?.setOnClickListener { _ ->
            Log.d(TAG, "setObserversAndListeners: btnSubmit setOnClickListener() called with: _")
            contactWithContactNumbers?.let {
                if (it.isValid()) {
                    if (it.contactNumbers.isNullOrEmpty()) {
                        showToast(resources.getString(R.string.mandatory_field))
                        it.contactNumbers?.add(getNewContactNumber())
                    } else
                        insertOrUpdateContactAfterDeletingNumbersIfExist(it)
                } else {
                    DELETE_CLICKED = true
                    contactNumbersRecyclerViewAdapter.removeContactNumber(
                        it.contactNumbers?.size
                    )
                    DELETE_CLICKED = false
                    showToast(
                        resources.getString(R.string.invalid_field),
                        Toast.LENGTH_LONG
                    )
                }
            }
        }


        activityAddContactsBinding?.btnCancel?.setOnClickListener {
            Log.d(TAG, "setObserversAndListeners: btnCancel setOnClickListener() called")
            onBackPressed()
        }
    }

    private fun populateContactDetailsToUI(contactWithContactNumbers: ContactWithContactNumbers?) {
        Log.d(
            TAG,
            "populateContactDetailsToUI() called with: contactWithContactNumbers = $contactWithContactNumbers"
        )
        contactWithContactNumbers?.let {
            Log.d(TAG, "populateContactDetailsToUI() called contact found")
            this.contactWithContactNumbers = it
            this.contactWithContactNumbers?.contactNumbers?.add(getNewContactNumber())
        }
        activityAddContactsBinding?.autoCompleteTextViewName?.setText(this.contactWithContactNumbers?.contact?.name)
        activityAddContactsBinding?.autoCompleteTextViewName?.setSelection(
            activityAddContactsBinding?.autoCompleteTextViewName?.text.toString().length
        )
        activityAddContactsBinding?.recyclerViewNumber?.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        activityAddContactsBinding?.recyclerViewNumber?.adapter =
            contactNumbersRecyclerViewAdapter
        contactNumbersRecyclerViewAdapter.resetContactNumberList()
        Observable.fromIterable(this.contactWithContactNumbers?.contactNumbers)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d(TAG, "populateContactDetailsToUI() called adding $it to RV")
                contactNumbersRecyclerViewAdapter.addContactNumber(
                    it,
                    this.contactWithContactNumbers?.contactNumbers?.indexOf(it)
                )
            }, {
                Log.e(
                    TAG,
                    "populateContactDetailsToUI: error while adding contactNumber ${it.message}",
                    it
                )
                showToast(resources.getString(R.string.error_loading_number))
            })?.let { disposables.add(it) }
    }

    private fun insertOrUpdateContactAfterDeletingNumbersIfExist(contactWithContactNumbers: ContactWithContactNumbers) {
        Log.d(
            TAG,
            "insertOrUpdateContactAfterDeletingNumbersIfExist() called with: contactWithContactNumbers = $contactWithContactNumbers"
        )
        disposables.add(
            Completable.fromAction {
                Observable.fromIterable(contactNumbersToDelete)
                    .flatMapCompletable {
                        Log.d(
                            TAG,
                            "insertOrUpdateContactAfterDeletingNumbersIfExist() called with contactNumber = ${it.countryCode} - ${it.number}"
                        )
                        contactsViewModel?.deleteNumber(it)
                            ?.subscribeOn(Schedulers.io())
                            ?.observeOn(AndroidSchedulers.mainThread())
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.d(
                            TAG,
                            "insertOrUpdateContactAfterDeletingNumbersIfExist() called with deleteNumber: onComplete()"
                        )
                    }, {
                        Log.e(
                            TAG,
                            "insertOrUpdateContactAfterDeletingNumbersIfExist: deleteNumber() onError: ${it.message}",
                            it
                        )
                        showToast(
                            resources.getString(R.string.error_while_deleting_number),
                            Toast.LENGTH_LONG
                        )
                    })
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .andThen(
                    contactsViewModel?.insertOrUpdateContact(contactWithContactNumbers)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                )
                .subscribe({
                    Log.d(
                        TAG,
                        "insertOrUpdateContactAfterDeletingNumbersIfExist: onComplete() called"
                    )
                    showToast(resources.getString(R.string.save_successful))
                    onBackPressed()
                }, {
                    Log.e(
                        TAG,
                        "insertOrUpdateContactAfterDeletingNumbersIfExist: onError() ${it.message}",
                        it
                    )
                    showToast(resources.getString(R.string.error_try_again))
                })
        )
    }

    private fun addNewContactNumber(position: Int?) {
        Log.d(TAG, "addNewContactNumber() called with: position = $position")
        val newContactNumber = getNewContactNumber()
        if (position == contactWithContactNumbers?.contactNumbers?.size?.minus(1) && !DELETE_CLICKED) {
            contactWithContactNumbers?.contactNumbers?.add(
                newContactNumber
            )
            contactNumbersRecyclerViewAdapter.addContactNumber(
                newContactNumber,
                contactWithContactNumbers?.contactNumbers?.size?.minus(1)
            )
        }
    }

    private fun getNewContactNumber() = ContactNumber(
        contactWithContactNumbers?.contact?.contactId,
        Constants.DEFAULT_CC,
        ""
    )

    private fun getContactFromIntent(): ContactWithContactNumbers? =
        intent.getParcelableExtra(Constants.CONTACT_WITH_NUMBER_KEY)

    private fun onCountryCodeClicked(
        editText: EditText,
        holder: ContactNumbersRecyclerViewAdapter.MyContactNumbersViewHolder
    ) {
        Log.d(TAG, "onCountryCodeClicked() called with: editText = $editText, holder = $holder")
        editText.addTextChangedListener(getCountryCodeTextChangedListener(holder))
    }

    private fun onNumberClicked(
        editText: EditText,
        holder: ContactNumbersRecyclerViewAdapter.MyContactNumbersViewHolder
    ) {
        Log.d(TAG, "onNumberClicked() called with: editText = $editText, position = $holder")
        editText.addTextChangedListener(getContactNumberTextChangedListener(holder))
    }

    private fun onDeleteClicked(holder: ContactNumbersRecyclerViewAdapter.MyContactNumbersViewHolder) {
        Log.d(TAG, "onDeleteClicked() called with: holder = $holder")
        if (holder.adapterPosition == contactWithContactNumbers?.contactNumbers?.size?.minus(1))
            showToast(resources.getString(R.string.empty_field))
        else {
            DELETE_CLICKED = true
            contactWithContactNumbers?.contactNumbers?.get(holder.adapterPosition)?.let {
                contactNumbersToDelete?.add(it)
            }
            contactWithContactNumbers?.contactNumbers?.removeAt(holder.adapterPosition)
            contactNumbersRecyclerViewAdapter.removeContactNumber(
                holder.adapterPosition
            )
            DELETE_CLICKED = false
        }
    }

    private fun getCountryCodeTextChangedListener(holder: ContactNumbersRecyclerViewAdapter.MyContactNumbersViewHolder) =
        object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                Log.d(TAG, "afterTextChanged() called with: p0 = $p0")
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(
                    TAG,
                    "beforeTextChanged() called with: p0 = $p0, p1 = $p1, p2 = $p2, p3 = $p3"
                )
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "onTextChanged() called with: p0 = $p0, p1 = $p1, p2 = $p2, p3 = $p3")
                if (activityAddContactsBinding?.recyclerViewNumber?.scrollState == RecyclerView.SCROLL_STATE_IDLE)
                    Observable.just(p0.toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Log.d(TAG, "onCountryCodeClicked onNext() called $it")
                            if (it.isNotEmpty())
                                addNewContactNumber(holder.adapterPosition)
                            contactWithContactNumbers?.contactNumbers?.get(holder.adapterPosition)?.countryCode =
                                it
                        }, {
                            Log.e(TAG, "onCountryCodeClicked onError(): ${it.message}", it)
                            showToast(resources.getString(R.string.error_while_entering_number))
                        })?.let { d -> disposables.add(d) }
            }
        }

    private fun getContactNumberTextChangedListener(holder: ContactNumbersRecyclerViewAdapter.MyContactNumbersViewHolder) =
        object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                Log.d(TAG, "afterTextChanged() called with: p0 = $p0")
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(
                    TAG,
                    "beforeTextChanged() called with: p0 = $p0, p1 = $p1, p2 = $p2, p3 = $p3"
                )
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "onTextChanged() called with: p0 = $p0, p1 = $p1, p2 = $p2, p3 = $p3")
                if (activityAddContactsBinding?.recyclerViewNumber?.scrollState == RecyclerView.SCROLL_STATE_IDLE)
                    Observable.just(p0.toString())
                        .debounce(3, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMapMaybe {
                            Log.d(TAG, "onNumberClicked() flatMap: $it")
                            if (it.isNotEmpty())
                                addNewContactNumber(holder.adapterPosition)
                            contactWithContactNumbers?.contactNumbers?.get(holder.adapterPosition)?.number =
                                it
                            contactsViewModel?.getContact(it)
                                ?.subscribeOn(Schedulers.io())
                                ?.observeOn(AndroidSchedulers.mainThread())
                        }
                        .subscribe({
                            Log.d(
                                TAG,
                                "onNumberClicked getContact() -> onNext() called ${it.contact?.name}"
                            )
                            it?.let {
                                populateContactDetailsToUI(it)
                            }
                        }, {
                            Log.e(
                                TAG,
                                "onNumberClicked getContact() -> onError(): ${it.message}",
                                it
                            )
                            showToast(resources.getString(R.string.error_while_entering_number))
                        })?.let { d -> disposables.add(d) }
            }
        }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
        ACTIVITY_STOPPED = true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
        ACTIVITY_STOPPED = false
        disposables.dispose()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    companion object {
        private const val TAG = "AddContactsActivity"
        private const val CONTACT_BUNDLE_KEY = "CONTACT_BUNDLE_KEY"
        private var DELETE_CLICKED = false
        private var ACTIVITY_STOPPED = false
    }

}