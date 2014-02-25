package org.thoughtcrime.securesms;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.google.android.gcm.GCMRegistrar;
import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.thoughtcrime.securesms.util.ActionBarUtil;
import org.thoughtcrime.securesms.util.TextWatcherAfterTextChanged;
import org.whispersystems.textsecure.crypto.MasterSecret;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.whispersystems.textsecure.util.PhoneNumberFormatter;
import org.whispersystems.textsecure.util.Util;

/**
 * The register account activity.  Prompts ths user for their registration information
 * and begins the account registration process.
 *
 * @author Moxie Marlinspike
 *
 */
public class RegistrationActivity extends SherlockActivity {

  private static final int PICK_COUNTRY = 1;

  private AsYouTypeFormatter   countryFormatter;
  private ArrayAdapter<String> countrySpinnerAdapter;
  private Spinner              countrySpinner;
  private TextView             countryCode;
  private TextView             number;
  private Button               createButton;
  private Button               skipButton;
  private TelephonyManager     telephonyManager;

  private MasterSecret masterSecret;

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.registration_activity);

    ActionBarUtil.initializeDefaultActionBar(this, getSupportActionBar(), getString(R.string.RegistrationActivity_connect_with_textsecure));

    telephonyManager = ((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE));
    
    initializeResources();
    initializeSpinner();
    initializeNumber();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == PICK_COUNTRY && resultCode == RESULT_OK && data != null) {
      setCountryCodeAndName(data.getIntExtra("country_code", 1), data.getStringExtra("country_name"));
    }
  }

  private void setCountryCodeAndName(int countryCode, String countryName) {
    this.countryCode.setText(countryCode + "");
    setCountryDisplay(countryName);
    setCountryFormatter(countryCode);
  }

  private void initializeResources() {
    this.masterSecret   = getIntent().getParcelableExtra("master_secret");
    this.countrySpinner = (Spinner)findViewById(R.id.country_spinner);
    this.countryCode    = (TextView)findViewById(R.id.country_code);
    this.number         = (TextView)findViewById(R.id.number);
    this.createButton   = (Button)findViewById(R.id.registerButton);
    this.skipButton     = (Button)findViewById(R.id.skipButton);

    this.countryCode.addTextChangedListener(new CountryCodeChangedListener());
    this.number.addTextChangedListener(new NumberChangedListener());
    this.createButton.setOnClickListener(new CreateButtonListener());
    this.skipButton.setOnClickListener(new CancelButtonListener());
  }

  private void initializeSpinner() {
    this.countrySpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
    this.countrySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    
    String simCountryIso = telephonyManager.getSimCountryIso();
    
    if (simCountryIso != null)
      setCountryCodeAndName(PhoneNumberFormatter.getRegionDisplayName(simCountryIso), PhoneNumberUtil.getInstance().getCountryCodeForRegion(simCountryIso));
    else
      setCountryDisplay(getString(R.string.RegistrationActivity_select_your_country));

    this.countrySpinner.setAdapter(this.countrySpinnerAdapter);
    this.countrySpinner.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
          Intent intent = new Intent(RegistrationActivity.this, CountrySelectionActivity.class);
          startActivityForResult(intent, PICK_COUNTRY);
        }
        return true;
      }
    });
  }

  private void initializeNumber() {
    String localNumber = telephonyManager.getLine1Number();

    if (!Util.isEmpty(localNumber) && !localNumber.startsWith("+")) {
      if (localNumber.length() == 10) localNumber = "+1" + localNumber;
      else                            localNumber = "+"  + localNumber;
    }

    try {
      if (!Util.isEmpty(localNumber)) {
        PhoneNumberUtil numberUtil                = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber localNumberObject = numberUtil.parse(localNumber, null);

        if (localNumberObject != null) {
          this.countryCode.setText(localNumberObject.getCountryCode()+"");
          this.number.setText(localNumberObject.getNationalNumber()+"");
        }
      }
    } catch (NumberParseException npe) {
      Log.w("CreateAccountActivity", npe);
    }
  }

  private void setCountryDisplay(String value) {
    this.countrySpinnerAdapter.clear();
    this.countrySpinnerAdapter.add(value);
  }

  private void setCountryFormatter(int countryCode) {
    PhoneNumberUtil util = PhoneNumberUtil.getInstance();
    String regionCode    = util.getRegionCodeForCountryCode(countryCode);

    if (regionCode == null) this.countryFormatter = null;
    else                    this.countryFormatter = util.getAsYouTypeFormatter(regionCode);
  }

  private String getConfiguredE164Number() {
    return PhoneNumberFormatter.formatE164(countryCode.getText().toString(),
                                           number.getText().toString());
  }

  private class CreateButtonListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
      final RegistrationActivity self = RegistrationActivity.this;

      TextSecurePreferences.setPromptedPushRegistration(self, true);

      if (Util.isEmpty(countryCode.getText())) {
        Toast.makeText(self,
                       getString(R.string.RegistrationActivity_you_must_specify_your_country_code),
                       Toast.LENGTH_LONG).show();
        return;
      }

      if (Util.isEmpty(number.getText())) {
        Toast.makeText(self,
                       getString(R.string.RegistrationActivity_you_must_specify_your_phone_number),
                       Toast.LENGTH_LONG).show();
        return;
      }

      final String e164number = getConfiguredE164Number();

      if (!PhoneNumberFormatter.isValidNumber(e164number)) {
        Util.showAlertDialog(self,
                             getString(R.string.RegistrationActivity_invalid_number),
                             String.format(getString(R.string.RegistrationActivity_the_number_you_specified_s_is_invalid),
                                           e164number));
        return;
      }

      try {
        GCMRegistrar.checkDevice(self);
      } catch (UnsupportedOperationException uoe) {
        Util.showAlertDialog(self, getString(R.string.RegistrationActivity_unsupported),
                             getString(R.string.RegistrationActivity_sorry_this_device_is_not_supported_for_data_messaging));
        return;
      }

      AlertDialog.Builder dialog = new AlertDialog.Builder(self);
      dialog.setMessage(String.format(getString(R.string.RegistrationActivity_we_will_now_verify_that_the_following_number_is_associated_with_your_device_s),
                                      PhoneNumberFormatter.getInternationalFormatFromE164(e164number)));
      dialog.setPositiveButton(getString(R.string.RegistrationActivity_continue),
                               new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                   Intent intent = new Intent(self, RegistrationProgressActivity.class);
                                   intent.putExtra("e164number", e164number);
                                   intent.putExtra("master_secret", masterSecret);
                                   startActivity(intent);
                                   finish();
                                 }
                               });
      dialog.setNegativeButton(getString(R.string.RegistrationActivity_edit), null);
      dialog.show();
    }
  }

  private class CountryCodeChangedListener extends TextWatcherAfterTextChanged {
    @Override
    public void afterTextChanged(Editable s) {
      if (Util.isEmpty(s)) {
        setCountryDisplay(getString(R.string.RegistrationActivity_select_your_country));
        countryFormatter = null;
        return;
      }

      int countryCode   = Integer.parseInt(s.toString());
      String regionCode = PhoneNumberUtil.getInstance().getRegionCodeForCountryCode(countryCode);

      setCountryFormatter(countryCode);
      setCountryDisplay(PhoneNumberFormatter.getRegionDisplayName(regionCode));

      if (!Util.isEmpty(regionCode) && !regionCode.equals("ZZ")) {
        number.requestFocus();
      }
    }
  }

  private class NumberChangedListener extends TextWatcherAfterTextChanged {

    @Override
    public void afterTextChanged(Editable s) {
      if (countryFormatter == null)
        return;

      if (Util.isEmpty(s))
        return;

      countryFormatter.clear();

      String number          = s.toString().replaceAll("[^\\d.]", "");
      String formattedNumber = null;

      for (int i=0;i<number.length();i++) {
        formattedNumber = countryFormatter.inputDigit(number.charAt(i));
      }

      if (!s.toString().equals(formattedNumber)) {
        s.replace(0, s.length(), formattedNumber);
      }
    }
  }

  private class CancelButtonListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
      TextSecurePreferences.setPromptedPushRegistration(RegistrationActivity.this, true);
      Intent nextIntent = getIntent().getParcelableExtra("next_intent");

      if (nextIntent == null) {
        nextIntent = new Intent(RegistrationActivity.this, RoutingActivity.class);
      }

      startActivity(nextIntent);
      finish();
    }
  }
}
