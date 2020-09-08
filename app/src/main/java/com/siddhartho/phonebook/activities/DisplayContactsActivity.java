package com.siddhartho.phonebook.activities;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.siddhartho.phonebook.dataclass.CallLogsCount;
import com.siddhartho.phonebook.utils.Constants;
import com.siddhartho.phonebook.dataclass.ContactWithContactNumbers;
import com.siddhartho.phonebook.utils.ContactsBroadcastReceiver;
import com.siddhartho.phonebook.adapters.ContactsRecyclerViewAdapter;
import com.siddhartho.phonebook.repository.ContactsRepository;
import com.siddhartho.phonebook.viewmodel.ContactsViewModel;
import com.siddhartho.phonebook.databasecomponent.ContactsViewModelFactory;
import com.siddhartho.phonebook.utils.ContentResolverForCallLog;
import com.siddhartho.phonebook.R;
import com.siddhartho.phonebook.databinding.ActivityDisplayContactsBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static android.view.Gravity.END;
import static com.siddhartho.phonebook.utils.ContactsUtilsKt.callThroughIntent;
import static com.siddhartho.phonebook.utils.ContactsUtilsKt.showToast;

public class DisplayContactsActivity extends AppCompatActivity {
    private static final String TAG = "DisplayContactsActivity";

    private ActivityDisplayContactsBinding activityDisplayContactsBinding;
    private ContactsViewModel contactsViewModel;
    private static final int CALL_REQUEST_CODE = 101, SMS_REQUEST_CODE = 102;
    private static final String SMS_MESSAGE = "sms_message", SMS_CONTACT = "sms_contact",
            SEARCH_QUERY = "search_query", RECYCLER_VIEW_STATE = "recycler_view_state";
    private String numberClicked;
    private ArrayList<ContactWithContactNumbers> contactList;
    private ContactsRecyclerViewAdapter contactsRecyclerViewAdapter;
    private static boolean isBackPressed = false;
    private EditText editTextSms;

    private final CompositeDisposable disposables = new CompositeDisposable();
    private ContactsBroadcastReceiver contactsBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
        activityDisplayContactsBinding = ActivityDisplayContactsBinding.inflate(getLayoutInflater());
        setContentView(activityDisplayContactsBinding.getRoot());
        setSupportActionBar(activityDisplayContactsBinding.toolbarDisplayContact);

        registerReceiverNow();

        setListeners();

        contactsViewModel = new ViewModelProvider(this,
                new ContactsViewModelFactory(new ContactsRepository(this))).get(ContactsViewModel.class);

        setRecyclerViewContacts();

        goToActionOverlayPermission();
    }

    private void registerReceiverNow() {
        Log.d(TAG, "registerReceiverNow() called");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PHONE_STATE");
        contactsBroadcastReceiver = new ContactsBroadcastReceiver();
        registerReceiver(contactsBroadcastReceiver, filter);
    }

    private void setListeners() {
        activityDisplayContactsBinding.fabDisplayContact.setOnClickListener(view -> goToAddContacts(null));

        activityDisplayContactsBinding.searchDisplayContact.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit() called with: query = [" + query + "]");
                if (query != null && contactsRecyclerViewAdapter != null) {
                    contactsRecyclerViewAdapter.getFilter().filter(query);
                    ((InputMethodManager) Objects.requireNonNull(getSystemService(INPUT_METHOD_SERVICE)))
                            .hideSoftInputFromWindow(activityDisplayContactsBinding.searchDisplayContact.getWindowToken(), 0);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange() called with: newText = [" + newText + "]");
                if (contactList != null && newText != null && !contactList.isEmpty())
                    if (contactsRecyclerViewAdapter != null)
                        contactsRecyclerViewAdapter.getFilter().filter(newText);
                    else {
                        showToast(DisplayContactsActivity.this,
                                getResources().getString(R.string.error_try_again), Toast.LENGTH_SHORT);
                        return false;
                    }
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
        disposables.dispose();
        unregisterReceiver(contactsBroadcastReceiver);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState() called with: outState = [" + outState + "]");
        outState.putInt(RECYCLER_VIEW_STATE,
                ((LinearLayoutManager) Objects.requireNonNull(
                        activityDisplayContactsBinding.viewScreenDisplayContact.recyclerViewContacts.getLayoutManager()))
                        .findFirstCompletelyVisibleItemPosition()
        );
        if (activityDisplayContactsBinding.searchDisplayContact.getQuery().length() != 0)
            outState.putString(SEARCH_QUERY, activityDisplayContactsBinding.searchDisplayContact.getQuery().toString());
        if (editTextSms != null) {
            outState.putString(SMS_CONTACT, numberClicked);
            outState.putString(SMS_MESSAGE, editTextSms.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState() called with: savedInstanceState = [" + savedInstanceState + "]");
        activityDisplayContactsBinding.viewScreenDisplayContact.recyclerViewContacts.smoothScrollToPosition(
                savedInstanceState.getInt(RECYCLER_VIEW_STATE)
        );
        if (savedInstanceState.containsKey(SEARCH_QUERY))
            activityDisplayContactsBinding.searchDisplayContact.postDelayed(() ->
                    activityDisplayContactsBinding.searchDisplayContact
                            .setQuery(savedInstanceState.getString(SEARCH_QUERY), true), 1000);
        numberClicked = savedInstanceState.getString(SMS_CONTACT);
        if (numberClicked != null)
            showSmsDialog(numberClicked, savedInstanceState.getString(SMS_MESSAGE));
    }

    private void setRecyclerViewContacts() {
        Log.d(TAG, "setRecyclerViewContacts() called");
        contactsRecyclerViewAdapter = new ContactsRecyclerViewAdapter();
        contactList = new ArrayList<>();
        AtomicReference<ArrayList<ContactWithContactNumbers>> contactListReference = new AtomicReference<>(new ArrayList<>());

        activityDisplayContactsBinding.viewScreenDisplayContact.recyclerViewContacts.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        activityDisplayContactsBinding.viewScreenDisplayContact.recyclerViewContacts.setAdapter(contactsRecyclerViewAdapter);

        disposables.add(contactsViewModel.getAllContacts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap((Function<List<ContactWithContactNumbers>, Flowable<ContactWithContactNumbers>>) contactWithContactNumbersList -> {
                    Log.d(TAG, "flatMap() getAllContacts() called with contactWithContactNumbersList = [" + contactWithContactNumbersList + "]");
                    contactListReference.set((ArrayList<ContactWithContactNumbers>) contactWithContactNumbersList);
                    return Flowable.fromIterable(contactWithContactNumbersList);
                })
                .subscribe(contactWithContactNumbers -> {
                    Log.d(TAG, "onNext() called with: contactWithContactNumbers = [" + contactWithContactNumbers + "]");
                    if (!contactList.contains(contactWithContactNumbers)) {
                        if (contactList.size() == contactListReference.get().size()) {
                            contactList.remove(contactListReference.get().indexOf(contactWithContactNumbers));
                            contactList.add(contactListReference.get().indexOf(contactWithContactNumbers), contactWithContactNumbers);
                            contactsRecyclerViewAdapter.replaceContactToList(contactWithContactNumbers,
                                    contactList.indexOf(contactWithContactNumbers));
                        } else {
                            contactList.add(contactListReference.get().indexOf(contactWithContactNumbers), contactWithContactNumbers);
                            contactsRecyclerViewAdapter.addContactToList(contactWithContactNumbers,
                                    contactList.indexOf(contactWithContactNumbers));
                        }
                    }
                }, e -> {
                    Log.e(TAG, "onError: " + e.getMessage(), e);
                    showToast(this, getResources().getString(R.string.error_loading_contact), Toast.LENGTH_LONG);
                }, () -> {
                    Log.d(TAG, "onComplete() called");
                    contactsRecyclerViewAdapter.addTrailingViewsAtEnd();
                    if (activityDisplayContactsBinding.searchDisplayContact.getQuery().length() > 0)
                        activityDisplayContactsBinding.searchDisplayContact.setQuery(
                                activityDisplayContactsBinding.searchDisplayContact.getQuery(), true);
                }));

        contactsRecyclerViewAdapter.setOnLongClickContactListener(this::showLongClickMenu);

        contactsRecyclerViewAdapter.setOnContactNumbersReceiveListener((linearLayout, contactNumbers) -> {
            Log.d(TAG, "onReceive() called with: linearLayout = [" + linearLayout + "], contactNumbers = [" + contactNumbers + "]");
            linearLayout.removeAllViews();
            disposables.add(Observable.fromIterable(contactNumbers)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(contactNumber -> {
                        Log.d(TAG, "onReceive -> onNext called with contactNumber = [" + contactNumber.getNumber() + "]");
                        LinearLayout lL = getLinearLayout();
                        lL.addView(getTextViewForNumber(contactNumber.getCountryCode() + " " + contactNumber.getNumber()));
                        lL.addView(getImageButton(contactNumber.getCountryCode() + " " + contactNumber.getNumber(), true));
                        lL.addView(getImageButton(contactNumber.getCountryCode() + " " + contactNumber.getNumber(), false));
                        linearLayout.addView(lL);
                    }, e -> {
                        Log.e(TAG, "onReceive -> onError: " + e.getMessage(), e);
                        showToast(this, getResources().getString(R.string.error_loading_number), Toast.LENGTH_LONG);
                    }));

        });
    }

    private void makeACall(String number) {
        Log.d(TAG, "makeACall() called with: number = [" + number + "]");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG}, CALL_REQUEST_CODE);
        else callThroughIntent(this, number);
    }

    private void showSmsDialog(String number, String message) {
        Log.d(TAG, "showSmsDialog() called with: number = [" + number + "], message = [" + message + "]");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_REQUEST_CODE);
        else smsDialog(number, message);
    }

    private void sendSms(String number, String message) {
        Log.d(TAG, "sendSms() called with: number = [" + number + "], message = [" + message + "]");
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, message,
                PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0),
                PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0));
        showToast(this, getResources().getString(R.string.sent_message), Toast.LENGTH_LONG);
    }

    private void smsDialog(String number, String message) {
        Log.d(TAG, "smsDialog() called with: number = [" + number + "], message = [" + message + "]");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Type your message:");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(30, 20, 30, 0);

        TextView charCount = new TextView(this, Xml.asAttributeSet(getResources().getXml(R.xml.sms_textview_attribute)));
        charCount.setLayoutParams(params);
        charCount.setGravity(END);

        editTextSms = new EditText(this, Xml.asAttributeSet(getResources().getXml(R.xml.sms_edittext_attribute)));
        editTextSms.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editTextSms.setHint(R.string.sms_hint);
        editTextSms.setFilters(new InputFilter[]{new InputFilter.LengthFilter(160)});
        editTextSms.setLayoutParams(params);
        editTextSms.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "beforeTextChanged() called with: charSequence = [" + charSequence + "], i = [" + i + "], i1 = [" + i1 + "], i2 = [" + i2 + "]");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "onTextChanged() called with: charSequence = [" + charSequence + "], i = [" + i + "], i1 = [" + i1 + "], i2 = [" + i2 + "]");
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG, "afterTextChanged() called with: editable = [" + editable + "]");
                charCount.setText(getResources().getString(R.string.char_count, editable.length()));
            }
        });

        if (!message.isEmpty()) {
            editTextSms.setText(message);
            editTextSms.setSelection(message.length());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            charCount.setTypeface(getResources().getFont(R.font.raleway_light_italic));
            editTextSms.setTypeface(getResources().getFont(R.font.raleway_regular));
        }
        container.addView(editTextSms);
        container.addView(charCount);
        builder.setView(container);
        builder.setPositiveButton("Send", (dialog, which) -> sendSms(number, editTextSms.getText().toString()));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.setOnCancelListener(dialog -> {
            numberClicked = null;
            editTextSms = null;
        });
        builder.show();
    }

    private void showLongClickMenu(ContactWithContactNumbers contactWithContactNumbers, int position) {
        Log.d(TAG, "showLongClickMenu() called with: contactWithContactNumbers = [" + contactWithContactNumbers + "], position = [" + position + "]");
        final CharSequence[] items = {"Edit", "Delete"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Objects.requireNonNull(contactWithContactNumbers.getContact()).getName());
        builder.setItems(items, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            switch (i) {
                case 0:
                    goToAddContacts(contactWithContactNumbers);
                    break;
                case 1:
                    deleteAlert(contactWithContactNumbers, position);
                    break;
            }
        });
        builder.show();
    }

    private void deleteAlert(ContactWithContactNumbers contactWithContactNumbers, int position) {
        Log.d(TAG, "deleteAlert() called with: contactWithContactNumbers = [" + contactWithContactNumbers + "], position = [" + position + "]");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete the contact?");
        builder.setPositiveButton("Yes", (dialog, which) -> disposables.add(contactsViewModel.deleteContact(contactWithContactNumbers)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                            Log.d(TAG, "onComplete() called");
                            contactList.remove(contactWithContactNumbers);
                            contactsRecyclerViewAdapter.removeContactFromList(position);
                            showToast(this, getResources().getString(R.string.delete_successful), Toast.LENGTH_SHORT);
                        }, e -> {
                            Log.e(TAG, "onError: " + e.getMessage(), e);
                            showToast(this,
                                    getResources().getString(R.string.error_try_again),
                                    Toast.LENGTH_LONG);
                        }
                )));

        builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private LinearLayout getLinearLayout() {
        Log.d(TAG, "getLinearLayout() called");
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setWeightSum(7);
        return linearLayout;
    }

    private ImageButton getImageButton(String number, boolean forCall) {
        Log.d(TAG, "getImageButton() called with: number = [" + number + "], forCall = [" + forCall + "]");
        ImageButton imageButton = new ImageButton(this);
        imageButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        imageButton.setContentDescription(forCall ? getResources().getString(R.string.call) : getResources().getString(R.string.sms));
        imageButton.setImageResource(forCall ? R.drawable.call_icon : R.drawable.sms_icon);
        imageButton.setTag(R.string.contact_number_image_button_key, number);
        imageButton.setTag(R.string.for_call_boolean_image_button_key, forCall);
        imageButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        imageButton.setOnClickListener(v -> {
            numberClicked = v.getTag(R.string.contact_number_image_button_key).toString();
            if (Boolean.parseBoolean(v.getTag(R.string.for_call_boolean_image_button_key).toString()))
                makeACall(numberClicked);
            else showSmsDialog(numberClicked, "");
        });
        return imageButton;
    }

    private TextView getTextViewForNumber(String number) {
        Log.d(TAG, "getTextViewForNumber() called with: number = [" + number + "]");
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 5));
        textView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            textView.setTypeface(getResources().getFont(R.font.raleway_light_italic));
        textView.setText(number);
        return textView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult() called with: requestCode = [" + requestCode + "], permissions = [" + Arrays.toString(permissions) + "], grantResults = [" + Arrays.toString(grantResults) + "]");
        boolean denied = false;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                denied = true;
                break;
            }
        }
        switch (requestCode) {
            case CALL_REQUEST_CODE:
                if (!denied) {
                    disposables.add(ContentResolverForCallLog.INSTANCE.setCursor(getContentResolver())
                            .andThen(ContentResolverForCallLog.INSTANCE.getCellPhoneCallLogsCount())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .flatMapCompletable(count -> contactsViewModel.insertOrUpdateCallLogsCount(new CallLogsCount(count))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread()))
                            .subscribe(() -> Log.d(TAG, "onRequestPermissionsResult insertOrUpdateCallLogsCount completed"),
                                    e -> Log.e(TAG, "onRequestPermissionsResult: insertOrUpdateCallLogsCount: error = " + e.getMessage(),
                                            e)));
                    if (numberClicked != null)
                        makeACall(numberClicked);
                }
                break;
            case SMS_REQUEST_CODE:
                if (!denied && numberClicked != null)
                    showSmsDialog(numberClicked, "");
                break;
        }
        numberClicked = null;
    }

    private void goToAddContacts(ContactWithContactNumbers contactWithContactNumbers) {
        Log.d(TAG, "goToAddContacts() called with: contactWithContactNumbers = [" + contactWithContactNumbers + "]");
        Intent intent = new Intent(this, AddContactsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (contactWithContactNumbers != null)
            intent.putExtra(Constants.CONTACT_WITH_NUMBER_KEY, contactWithContactNumbers);
        startActivity(intent);
    }

    private void goToActionOverlayPermission() {
        Log.d(TAG, "goToActionOverlayPermission() called");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission required");
            builder.setMessage("\nWe need permission to display over other apps to show you the details of your last call.\n");
            builder.setCancelable(false);
            builder.setPositiveButton("Allow", (dialog, which) ->
                    startActivity(
                            new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()))
                    ));
            builder.setNegativeButton("Deny", (dialog, which) -> {
                dialog.dismiss();
                finishAffinity();
            });
            builder.show();
        }
    }

    @Override
    public void onBackPressed() {
        if (isBackPressed) {
            super.onBackPressed();
            finishAffinity();
        } else {
            showToast(this, getResources().getString(R.string.exit), Toast.LENGTH_SHORT);
            isBackPressed = true;
            new Handler(Looper.getMainLooper()).postDelayed(() -> isBackPressed = false, 1500);
        }
    }
}
