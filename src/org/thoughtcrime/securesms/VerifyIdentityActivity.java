/**
 * Copyright (C) 2011 Whisper Systems
 * Copyright (C) 2013 Open Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thoughtcrime.securesms;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.TextView;
import android.widget.Toast;

import org.thoughtcrime.securesms.color.MaterialColor;
import org.thoughtcrime.securesms.crypto.IdentityKeyParcelable;
import org.thoughtcrime.securesms.crypto.IdentityKeyUtil;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.crypto.storage.TextSecureSessionStore;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.recipients.RecipientFactory;
import org.thoughtcrime.securesms.util.Hex;
import org.thoughtcrime.securesms.util.Util;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.state.SessionRecord;
import org.whispersystems.libsignal.state.SessionStore;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;

/**
 * Activity for verifying identity keys.
 *
 * @author Moxie Marlinspike
 */
public class VerifyIdentityActivity extends KeyScanningActivity implements Recipient.RecipientModifiedListener {

  private Recipient    recipient;
  private MasterSecret masterSecret;

  private TextView localIdentityFingerprint;
  private TextView remoteIdentityFingerprint;

  @Override
  protected void onCreate(Bundle state, @NonNull MasterSecret masterSecret) {
    this.masterSecret = masterSecret;
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle(R.string.AndroidManifest__verify_identity);

    setContentView(R.layout.verify_identity_activity);

    this.localIdentityFingerprint  = (TextView)findViewById(R.id.you_read);
    this.remoteIdentityFingerprint = (TextView)findViewById(R.id.friend_reads);
  }

  @Override
  public void onResume() {
    super.onResume();

    this.recipient = RecipientFactory.getRecipientForId(this, this.getIntent().getLongExtra("recipient", -1), true);
    this.recipient.addListener(this);

    setActionBarNotificationBarColor(recipient.getColor());
    initializeFingerprints();
  }

  public void setActionBarNotificationBarColor(MaterialColor color) {
    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color.toActionBarColor(this)));

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      getWindow().setStatusBarColor(color.toStatusBarColor(this));
    }
  }

  @Override
  public void onModified(final Recipient recipient) {
    Util.runOnMain(new Runnable() {
      @Override
      public void run() {
        setActionBarNotificationBarColor(recipient.getColor());
      }
    });
  }

  private void initializeFingerprints() {
    if (!IdentityKeyUtil.hasIdentityKey(this)) {
      localIdentityFingerprint.setText(R.string.VerifyIdentityActivity_you_do_not_have_an_identity_key);
      return;
    }

    localIdentityFingerprint.setText(Hex.toString(IdentityKeyUtil.getIdentityKey(this).serialize()));

    IdentityKey identityKey = getRemoteIdentityKey(masterSecret, recipient);

    if (identityKey == null) {
      remoteIdentityFingerprint.setText(R.string.VerifyIdentityActivity_recipient_has_no_identity_key);
    } else {
      remoteIdentityFingerprint.setText(Hex.toString(identityKey.serialize()));
    }
  }

  @Override
  protected void initiateDisplay() {
    if (!IdentityKeyUtil.hasIdentityKey(this)) {
      Toast.makeText(this,
                     R.string.VerifyIdentityActivity_you_don_t_have_an_identity_key_exclamation,
                     Toast.LENGTH_LONG).show();
      return;
    }

    super.initiateDisplay();
  }

  @Override
  protected void initiateScan() {
    IdentityKey identityKey = getRemoteIdentityKey(masterSecret, recipient);

    if (identityKey == null) {
      Toast.makeText(this, R.string.VerifyIdentityActivity_recipient_has_no_identity_key_exclamation,
                     Toast.LENGTH_LONG).show();
    } else {
      super.initiateScan();
    }
  }

  @Override
  protected String getScanString() {
    return getString(R.string.VerifyIdentityActivity_scan_contacts_qr_code);
  }

  @Override
  protected String getDisplayString() {
    return getString(R.string.VerifyIdentityActivity_display_your_qr_code);
  }

  @Override
  protected IdentityKey getIdentityKeyToCompare() {
    return getRemoteIdentityKey(masterSecret, recipient);
  }

  @Override
  protected IdentityKey getIdentityKeyToDisplay() {
    return IdentityKeyUtil.getIdentityKey(this);
  }

  @Override
  protected String getNotVerifiedMessage() {
    return getString(R.string.VerifyIdentityActivity_warning_the_scanned_key_does_not_match_please_check_the_fingerprint_text_carefully);
  }

  @Override
  protected String getNotVerifiedTitle() {
    return getString(R.string.VerifyIdentityActivity_not_verified_exclamation);
  }

  @Override
  protected String getVerifiedMessage() {
    return getString(R.string.VerifyIdentityActivity_their_key_is_correct_it_is_also_necessary_to_verify_your_key_with_them_as_well);
  }

  @Override
  protected String getVerifiedTitle() {
    return getString(R.string.VerifyIdentityActivity_verified_exclamation);
  }

  private @Nullable IdentityKey getRemoteIdentityKey(MasterSecret masterSecret, Recipient recipient) {
    IdentityKeyParcelable identityKeyParcelable = getIntent().getParcelableExtra("remote_identity");

    if (identityKeyParcelable != null) {
      return identityKeyParcelable.get();
    }

    SessionStore          sessionStore   = new TextSecureSessionStore(this, masterSecret);
    SignalProtocolAddress axolotlAddress = new SignalProtocolAddress(recipient.getNumber(), SignalServiceAddress.DEFAULT_DEVICE_ID);
    SessionRecord         record         = sessionStore.loadSession(axolotlAddress);

    if (record == null) {
      return null;
    }

    return record.getSessionState().getRemoteIdentityKey();
  }
}
