/**
 * Copyright (C) 2011 Whisper Systems
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thoughtcrime.securesms;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.ByteString;

import org.thoughtcrime.securesms.components.CircledImageView;
import org.thoughtcrime.securesms.components.EmojiDrawer;
import org.thoughtcrime.securesms.components.EmojiToggle;
import org.thoughtcrime.securesms.components.SendButton;
import org.thoughtcrime.securesms.contacts.ContactAccessor;
import org.thoughtcrime.securesms.contacts.ContactAccessor.ContactData;
import org.thoughtcrime.securesms.crypto.KeyExchangeInitiator;
import org.thoughtcrime.securesms.crypto.MasterCipher;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.crypto.SecurityEvent;
import org.thoughtcrime.securesms.crypto.storage.TextSecureSessionStore;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.DraftDatabase;
import org.thoughtcrime.securesms.database.DraftDatabase.Draft;
import org.thoughtcrime.securesms.database.DraftDatabase.Drafts;
import org.thoughtcrime.securesms.database.GroupDatabase;
import org.thoughtcrime.securesms.database.MmsSmsColumns.Types;
import org.thoughtcrime.securesms.database.ThreadDatabase;
import org.thoughtcrime.securesms.mms.AttachmentManager;
import org.thoughtcrime.securesms.mms.AttachmentTypeSelectorAdapter;

import org.thoughtcrime.securesms.mms.ImageSlide;

import org.thoughtcrime.securesms.mms.MediaConstraints;
import org.thoughtcrime.securesms.mms.MediaTooLargeException;
import org.thoughtcrime.securesms.mms.MmsMediaConstraints;
import org.thoughtcrime.securesms.mms.OutgoingGroupMediaMessage;
import org.thoughtcrime.securesms.mms.OutgoingLegacyMmsConnection;
import org.thoughtcrime.securesms.mms.OutgoingMediaMessage;
import org.thoughtcrime.securesms.mms.OutgoingSecureMediaMessage;
import org.thoughtcrime.securesms.mms.PushMediaConstraints;
import org.thoughtcrime.securesms.mms.Slide;
import org.thoughtcrime.securesms.mms.SlideDeck;
import org.thoughtcrime.securesms.notifications.MessageNotifier;
import org.thoughtcrime.securesms.protocol.Tag;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.recipients.RecipientFactory;
import org.thoughtcrime.securesms.recipients.RecipientFormattingException;
import org.thoughtcrime.securesms.recipients.Recipients;
import org.thoughtcrime.securesms.service.KeyCachingService;
import org.thoughtcrime.securesms.sms.MessageSender;
import org.thoughtcrime.securesms.sms.OutgoingEncryptedMessage;
import org.thoughtcrime.securesms.sms.OutgoingEndSessionMessage;
import org.thoughtcrime.securesms.sms.OutgoingTextMessage;
import org.thoughtcrime.securesms.util.BitmapDecodingException;
import org.thoughtcrime.securesms.util.BitmapUtil;
import org.thoughtcrime.securesms.util.CharacterCalculator;
import org.thoughtcrime.securesms.util.Dialogs;
import org.thoughtcrime.securesms.util.DirectoryHelper;
import org.thoughtcrime.securesms.util.DynamicLanguage;
import org.thoughtcrime.securesms.util.DynamicTheme;
import org.thoughtcrime.securesms.util.EncryptedCharacterCalculator;
import org.thoughtcrime.securesms.util.GroupUtil;
import org.thoughtcrime.securesms.util.MemoryCleaner;
import org.thoughtcrime.securesms.util.SmsCharacterCalculator;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.thoughtcrime.securesms.util.Util;
import org.whispersystems.libaxolotl.AxolotlAddress;
import org.whispersystems.libaxolotl.InvalidMessageException;
import org.whispersystems.libaxolotl.state.SessionStore;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import de.gdata.messaging.components.SelectTransportButton;
import de.gdata.messaging.components.SelfDestructionButton;
import de.gdata.messaging.util.GDataPreferences;
import de.gdata.messaging.util.GUtil;
import de.gdata.messaging.util.PrivacyBridge;
import ws.com.google.android.mms.ContentType;

import de.gdata.messaging.util.ProfileAccessor;


import static org.thoughtcrime.securesms.database.GroupDatabase.GroupRecord;
import static org.thoughtcrime.securesms.recipients.Recipient.RecipientModifiedListener;
import static org.whispersystems.textsecure.internal.push.PushMessageProtos.PushMessageContent.GroupContext;

/**
 * Activity for displaying a message thread, as well as
 * composing/sending a new message into that thread.
 *
 * @author Moxie Marlinspike
 */
public class ConversationActivity extends PassphraseRequiredActionBarActivity
    implements ConversationFragment.ConversationFragmentListener,
    AttachmentManager.AttachmentListener {
  private static final String TAG = ConversationActivity.class.getSimpleName();


  public static final String RECIPIENTS_EXTRA = "recipients";
  public static final String THREAD_ID_EXTRA = "thread_id";
  public static final String MASTER_SECRET_EXTRA = "master_secret";
  public static final String DRAFT_TEXT_EXTRA = "draft_text";
  public static final String DRAFT_IMAGE_EXTRA = "draft_image";
  public static final String DRAFT_AUDIO_EXTRA = "draft_audio";
  public static final String DRAFT_VIDEO_EXTRA = "draft_video";
  public static final String DRAFT_MEDIA_EXTRA = "draft_media";
  public static final String DRAFT_MEDIA_TYPE_EXTRA = "draft_media_type";

  public static final String DISTRIBUTION_TYPE_EXTRA = "distribution_type";

  private static final int PICK_IMAGE = 1;
  private static final int PICK_VIDEO = 2;
  private static final int PICK_AUDIO = 3;
  private static final int PICK_CONTACT_INFO = 4;
  private static final int GROUP_EDIT = 5;
  private static final int SET_CALLFILTER = 10;

  private MasterSecret masterSecret;
  private EditText composeText;
  private SendButton sendButton;
  private SelectTransportButton transportButton;
  private SelfDestructionButton bombTransportButton;
  private TextView charactersLeft;

  private AttachmentTypeSelectorAdapter attachmentAdapter;
  private AttachmentManager attachmentManager;
  private BroadcastReceiver securityUpdateReceiver;
  private BroadcastReceiver groupUpdateReceiver;
  private EmojiDrawer emojiDrawer;
  private EmojiToggle emojiToggle;

  private Recipients recipients;
  private long threadId;
  private int distributionType;
  private boolean isEncryptedConversation;
  private boolean isMmsEnabled = true;
  private boolean isCharactersLeftViewEnabled;

  public static CharacterCalculator characterCalculator = new EncryptedCharacterCalculator();
  private DynamicTheme dynamicTheme = new DynamicTheme();
  private DynamicLanguage dynamicLanguage = new DynamicLanguage();

  private SelectTransportListener selectTransportButtonListener = new SelectTransportListener();
  private BombTransportListener bombTransportButtonListener = new BombTransportListener();
  private SendButtonListener sendButtonListener = new SendButtonListener();
  private AddAttachmentListener addAttachmentButtonListener = new AddAttachmentListener();
  private int currentMediaSize;

  @Override
  protected void onCreate(Bundle state) {
    overridePendingTransition(R.anim.slide_from_right, R.anim.fade_scale_out);
    dynamicTheme.onCreate(this);
    dynamicLanguage.onCreate(this);
    super.onCreate(state);
    setContentView(R.layout.conversation_activity);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    initializeReceivers();
    initializeViews();
    initializeResources();
    initializeDraft();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    if (!Util.isEmpty(composeText) || attachmentManager.isAttachmentPresent()) {
      saveDraft();
      attachmentManager.clear();
      composeText.setText("");
    }
    setIntent(intent);

    initializeResources();
    initializeDraft();

    ConversationFragment fragment = getFragment();

    if (fragment != null) {
      fragment.onNewIntent();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    dynamicTheme.onResume(this);
    dynamicLanguage.onResume(this);

    initializeSecurity();
    initializeTitleBar();
    initializeEnabledCheck();
    initializeMmsEnabledCheck();
    initializeIme();
    initializeCharactersLeftViewEnabledCheck();
    calculateCharactersRemaining();

    MessageNotifier.setVisibleThread(threadId);
    markThreadAsRead();
  }

  @Override
  protected void onPause() {
    super.onPause();
    MessageNotifier.setVisibleThread(-1L);
    if (isFinishing()) overridePendingTransition(R.anim.fade_scale_in, R.anim.slide_to_right);
  }

  @Override
  protected void onDestroy() {
    saveDraft();
    unregisterReceiver(securityUpdateReceiver);
    unregisterReceiver(groupUpdateReceiver);
    MemoryCleaner.clean(masterSecret);
    super.onDestroy();
  }

  @Override
  public void onActivityResult(int reqCode, int resultCode, Intent data) {
    Log.w(TAG, "onActivityResult called: " + reqCode + ", " + resultCode + " , " + data);
    super.onActivityResult(reqCode, resultCode, data);

    if (data == null || resultCode != RESULT_OK) return;
    switch (reqCode) {

      case SET_CALLFILTER:
        new GDataPreferences(getApplicationContext()).saveFilterGroupIdForContact(recipients.getPrimaryRecipient().getNumber(), data.getExtras().getLong("filterGroupId"));
        break;
      case PICK_IMAGE:
        addAttachmentImage(data.getData());
        break;
      case PICK_VIDEO:
        addAttachmentVideo(data.getData());
        break;
      case PICK_AUDIO:
        addAttachmentAudio(data.getData());
        break;
      case PICK_CONTACT_INFO:
        addAttachmentContactInfo(data.getData());
        break;
      case GROUP_EDIT:
        this.recipients = RecipientFactory.getRecipientsForIds(this, data.getLongArrayExtra(GroupCreateActivity.GROUP_RECIPIENT_EXTRA), true);
        initializeTitleBar();
        break;
    }
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    MenuInflater inflater = this.getMenuInflater();
    menu.clear();

    boolean pushRegistered = TextSecurePreferences.isPushRegistered(this);

    if (isSingleConversation() && isEncryptedConversation) {
      inflater.inflate(R.menu.conversation_secure_identity, menu);
      //inflater.inflate(R.menu.conversation_secure_sms, menu.findItem(R.id.menu_security).getSubMenu());
    } else if (isSingleConversation()) {
      if (!pushRegistered) inflater.inflate(R.menu.conversation_insecure_no_push, menu);
      inflater.inflate(R.menu.conversation_insecure, menu);
    }

    if (isSingleConversation()) {
      inflater.inflate(R.menu.conversation_callable, menu);
    } else if (isGroupConversation()) {
      inflater.inflate(R.menu.conversation_group_options, menu);

      if (!isPushGroupConversation()) {
        inflater.inflate(R.menu.conversation_mms_group_options, menu);
        if (distributionType == ThreadDatabase.DistributionTypes.BROADCAST) {
          menu.findItem(R.id.menu_distribution_broadcast).setChecked(true);
        } else {
          menu.findItem(R.id.menu_distribution_conversation).setChecked(true);
        }
      } else if (isActiveGroup()) {
        inflater.inflate(R.menu.conversation_push_group_options, menu);
      }
    }

    inflater.inflate(R.menu.conversation, menu);

    if (isSingleConversation() && getRecipients().getPrimaryRecipient().getContactUri() == null) {
      inflater.inflate(R.menu.conversation_add_to_contacts, menu);
    }
    MenuItem itemHide = menu.findItem(R.id.menu_hide_contact);
    MenuItem itemBlock = menu.findItem(R.id.menu_block_contact);
    if (!isSingleConversation()) {
      if (itemHide != null && itemBlock != null) {
        itemHide.setVisible(false);
        itemBlock.setVisible(false);
      }
    }

    super.onPrepareOptionsMenu(menu);
    return true;
  }

  private void handleOpenProfile(String profileId) {
    final Intent intent = new Intent(this, ProfileActivity.class);
    intent.putExtra("master_secret", masterSecret);
    intent.putExtra("profile_id", profileId);
    if (getSupportActionBar() != null) {
      intent.putExtra("profile_name", getSupportActionBar().getTitle());
    }
    if (recipients != null) {
      intent.putExtra("profile_number", recipients.getPrimaryRecipient().getNumber());
    }
    startActivity(intent);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    switch (item.getItemId()) {
      case R.id.menu_call:
        // handleDial(getRecipients().getPrimaryRecipient());
        ProfileAccessor.sendProfileUpdateToContacts(getApplicationContext(), masterSecret, isEncryptedConversation);
        return true;
      case R.id.menu_delete_thread:
        handleDeleteThread();
        return true;
      case R.id.menu_add_attachment:
        handleAddAttachment();
        return true;
      case R.id.menu_hide_contact:
        handleHideContact();
        return true;
      case R.id.menu_block_contact:
        handleBlockContact();
        return true;
      case R.id.menu_add_to_contacts:
        handleAddToContacts();
        return true;
      case R.id.menu_start_secure_session:
        handleStartSecureSession();
        return true;
      case R.id.menu_abort_session:
        handleAbortSecureSession();
        return true;
      case R.id.menu_verify_identity:
        handleVerifyIdentity();
        return true;
      case R.id.menu_group_recipients:
        handleDisplayGroupRecipients();
        return true;
      case R.id.menu_distribution_broadcast:
        handleDistributionBroadcastEnabled(item);
        return true;
      case R.id.menu_distribution_conversation:
        handleDistributionConversationEnabled(item);
        return true;
      case R.id.menu_edit_group:
        handleEditPushGroup();
        return true;
      case R.id.menu_leave:
        handleLeavePushGroup();
        return true;
      case R.id.menu_invite:
        handleInviteLink();
        return true;
      case android.R.id.home:
        handleReturnToConversationList();
        return true;
    }

    return false;
  }

  private void handleHideContact() {
    if (GUtil.featureCheck(getApplicationContext(), true)) {
      ArrayList<String> numbers = new ArrayList<>();
      String number = recipients.getPrimaryRecipient().getNumber();
      numbers.add(number);
      PrivacyBridge.addContactToPrivacy(recipients.getPrimaryRecipient().getName(), numbers);
    }
  }

  private void handleBlockContact() {
    if (GUtil.featureCheck(getApplicationContext(), true)) {
      try {
        Intent intent = new Intent("de.gdata.mobilesecurity.activities.filter.NewFilterActivity");
        intent.putExtra("title", getString(R.string.app_name));
        intent.putExtra("phoneNo", recipients.getPrimaryRecipient().getNumber());
        intent.putExtra("displayName", recipients.getPrimaryRecipient().getName());
        intent.putExtra("filterGroupId", new GDataPreferences(getApplicationContext()).getFilterGroupIdForContact(recipients.getPrimaryRecipient().getNumber()));
        startActivityForResult(intent, SET_CALLFILTER);
      } catch (Exception e) {
        Log.d("GDATA", "Activity not found " + e.toString());
      }
    }
  }

  @Override
  public void onBackPressed() {
    if (emojiDrawer.getVisibility() == View.VISIBLE) {
      emojiDrawer.setVisibility(View.GONE);
      emojiToggle.toggle();
    } else {
      super.onBackPressed();
    }
  }

  //////// Event Handlers

  private void handleReturnToConversationList() {
    Intent intent = new Intent(this, ConversationListActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.putExtra("master_secret", masterSecret);
    startActivity(intent);
    finish();
  }

  private void handleInviteLink() {
    try {
      boolean a = SecureRandom.getInstance("SHA1PRNG").nextBoolean();
      if (a)
        composeText.setText(getString(R.string.ConversationActivity_get_with_it, getString(R.string.conversation_activity_invite_link)));
      else
        composeText.setText(getString(R.string.ConversationActivity_install_textsecure, getString(R.string.conversation_activity_invite_link)));
    } catch (NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    }
  }

  private void handleVerifyIdentity() {
    Intent verifyIdentityIntent = new Intent(this, VerifyIdentityActivity.class);
    verifyIdentityIntent.putExtra("recipient", getRecipients().getPrimaryRecipient().getRecipientId());
    verifyIdentityIntent.putExtra("master_secret", masterSecret);
    startActivity(verifyIdentityIntent);
  }

  private void handleStartSecureSession() {
    if (getRecipients() == null) {
      Toast.makeText(this, getString(R.string.ConversationActivity_invalid_recipient),
          Toast.LENGTH_LONG).show();
      return;
    }

    final Recipient recipient = getRecipients().getPrimaryRecipient();
    String recipientName = (recipient.getName() == null ? recipient.getNumber() : recipient.getName());
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.ConversationActivity_initiate_secure_session_question);
    builder.setIcon(Dialogs.resolveIcon(this, R.attr.dialog_info_icon));
    builder.setCancelable(true);
    builder.setMessage(String.format(getString(R.string.ConversationActivity_initiate_secure_session_with_s_question),
        recipientName));
    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        KeyExchangeInitiator.initiate(ConversationActivity.this, masterSecret,
            recipient, true);
      }
    });

    builder.setNegativeButton(R.string.no, null);
    builder.show();
  }

  private void handleAbortSecureSession() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.ConversationActivity_abort_secure_session_confirmation);
    builder.setIcon(Dialogs.resolveIcon(this, R.attr.dialog_alert_icon));
    builder.setCancelable(true);
    builder.setMessage(R.string.ConversationActivity_are_you_sure_that_you_want_to_abort_this_secure_session_question);
    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (isSingleConversation()) {
          final Context context = getApplicationContext();

          OutgoingEndSessionMessage endSessionMessage =
              new OutgoingEndSessionMessage(new OutgoingTextMessage(getRecipients(), "TERMINATE"));

          new AsyncTask<OutgoingEndSessionMessage, Void, Long>() {
            @Override
            protected Long doInBackground(OutgoingEndSessionMessage... messages) {
              return MessageSender.send(context, masterSecret, messages[0], threadId, false);
            }

            @Override
            protected void onPostExecute(Long result) {
              sendComplete(result);
            }
          }.execute(endSessionMessage);
        }
      }
    });
    builder.setNegativeButton(R.string.no, null);
    builder.show();
  }

  private void handleLeavePushGroup() {
    if (getRecipients() == null) {
      Toast.makeText(this, getString(R.string.ConversationActivity_invalid_recipient),
          Toast.LENGTH_LONG).show();
      return;
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.ConversationActivity_leave_group));
    builder.setIcon(Dialogs.resolveIcon(this, R.attr.dialog_info_icon));
    builder.setCancelable(true);
    builder.setMessage(getString(R.string.ConversationActivity_are_you_sure_you_want_to_leave_this_group));
    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Context self = ConversationActivity.this;
        try {
          byte[] groupId = GroupUtil.getDecodedId(getRecipients().getPrimaryRecipient().getNumber());
          DatabaseFactory.getGroupDatabase(self).setActive(groupId, false);

          GroupContext context = GroupContext.newBuilder()
              .setId(ByteString.copyFrom(groupId))
              .setType(GroupContext.Type.QUIT)
              .build();

          OutgoingGroupMediaMessage outgoingMessage = new OutgoingGroupMediaMessage(self, getRecipients(),
              context, null);
          MessageSender.send(self, masterSecret, outgoingMessage, threadId, false);
          DatabaseFactory.getGroupDatabase(self).remove(groupId, TextSecurePreferences.getLocalNumber(self));
          initializeEnabledCheck();
        } catch (IOException e) {
          Log.w(TAG, e);
          Toast.makeText(self, R.string.ConversationActivity_error_leaving_group, Toast.LENGTH_LONG).show();
        }
      }
    });

    builder.setNegativeButton(R.string.no, null);
    builder.show();
  }

  private void handleEditPushGroup() {
    Intent intent = new Intent(ConversationActivity.this, GroupCreateActivity.class);
    intent.putExtra(GroupCreateActivity.MASTER_SECRET_EXTRA, masterSecret);
    intent.putExtra(GroupCreateActivity.GROUP_RECIPIENT_EXTRA, recipients.getPrimaryRecipient().getRecipientId());
    startActivityForResult(intent, GROUP_EDIT);
  }

  private void handleDistributionBroadcastEnabled(MenuItem item) {
    distributionType = ThreadDatabase.DistributionTypes.BROADCAST;
    item.setChecked(true);

    if (threadId != -1) {
      new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
          DatabaseFactory.getThreadDatabase(ConversationActivity.this)
              .setDistributionType(threadId, ThreadDatabase.DistributionTypes.BROADCAST);
          return null;
        }
      }.execute();
    }
  }

  private void handleDistributionConversationEnabled(MenuItem item) {
    distributionType = ThreadDatabase.DistributionTypes.CONVERSATION;
    item.setChecked(true);

    if (threadId != -1) {
      new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
          DatabaseFactory.getThreadDatabase(ConversationActivity.this)
              .setDistributionType(threadId, ThreadDatabase.DistributionTypes.CONVERSATION);
          return null;
        }
      }.execute();
    }
  }

  private void handleDial(Recipient recipient) {
    try {
      if (recipient == null) return;

      Intent dialIntent = new Intent(Intent.ACTION_DIAL,
          Uri.parse("tel:" + recipient.getNumber()));
      startActivity(dialIntent);
    } catch (ActivityNotFoundException anfe) {
      Log.w(TAG, anfe);
      Dialogs.showAlertDialog(this,
          getString(R.string.ConversationActivity_calls_not_supported),
          getString(R.string.ConversationActivity_this_device_does_not_appear_to_support_dial_actions));
    }
  }

  private void handleDisplayGroupRecipients() {
    new GroupMembersDialog(this, getRecipients()).display();
  }

  private void handleDeleteThread() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.ConversationActivity_delete_thread_confirmation);
    builder.setIcon(Dialogs.resolveIcon(this, R.attr.dialog_alert_icon));
    builder.setCancelable(true);
    builder.setMessage(R.string.ConversationActivity_are_you_sure_that_you_want_to_permanently_delete_this_conversation_question);
    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (threadId > 0) {
          DatabaseFactory.getThreadDatabase(ConversationActivity.this).deleteConversation(threadId);
          finish();
        }
      }
    });

    builder.setNegativeButton(R.string.no, null);
    builder.show();
  }

  private void handleAddToContacts() {
    final Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
    intent.putExtra(ContactsContract.Intents.Insert.PHONE, recipients.getPrimaryRecipient().getNumber());
    intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
    startActivity(intent);
  }

  private void handleAddAttachment() {
    if (this.isMmsEnabled || DirectoryHelper.isPushDestination(this, getRecipients())) {
      AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.GSecure_Light_Dialog));
      builder.setIcon(R.drawable.ic_dialog_attach);
      builder.setTitle(R.string.ConversationActivity_add_attachment);
      builder.setAdapter(attachmentAdapter, new AttachmentTypeListener());
      builder.show();
    } else {
      handleManualMmsRequired();
    }
  }

  private void handleManualMmsRequired() {
    Toast.makeText(this, R.string.MmsDownloader_error_reading_mms_settings, Toast.LENGTH_LONG).show();

    Intent intent = new Intent(this, PromptMmsActivity.class);
    intent.putExtras(getIntent().getExtras());
    startActivity(intent);
  }

  ///// Initializers

  private void initializeTitleBar() {
    final String title;
    final String subtitle;
    final Recipient recipient = getRecipients().getPrimaryRecipient();

    if (isSingleConversation()) {
      if (TextUtils.isEmpty(recipient.getName())) {
        title = recipient.getNumber();
        subtitle = null;
      } else {
        title = recipient.getName();

        final Long profileId = GUtil.numberToLong(recipient.getNumber());
        String status = ProfileAccessor.getProfileStatusForRecepient(this, profileId + "");
        subtitle = TextUtils.isEmpty(status) ? PhoneNumberUtils.formatNumber(recipient.getNumber()) : status;

        ImageSlide avatarSlide = ProfileAccessor.getProfileAsImageSlide(this, masterSecret, profileId + "");

        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.actionbar_conversation, null);
        TextView mTitleTextView = (TextView) mCustomView.findViewById(R.id.action_bar_title);
        TextView mTitleTextViewSubtitle = (TextView) mCustomView.findViewById(R.id.action_bar_subtitle);
        CircledImageView thumbnail = (CircledImageView) mCustomView.findViewById(R.id.profile_picture);
        if (avatarSlide != null) {
          ProfileAccessor.buildGlideRequest(avatarSlide).into(thumbnail);
        }
        mTitleTextView.setText(title);
        mTitleTextViewSubtitle.setText(subtitle);

        mCustomView.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View view) {
            handleOpenProfile(profileId + "");
          }
        });

        getSupportActionBar().setCustomView(mCustomView);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
      }
    } else if (isGroupConversation()) {
      if (isPushGroupConversation()) {
        final String groupName = recipient.getName();
        final Bitmap avatar = recipient.getContactPhoto();
        if (avatar != null) {
          getSupportActionBar().setIcon(new BitmapDrawable(getResources(), BitmapUtil.getCircleBitmap(avatar)));
        }

        title = (!TextUtils.isEmpty(groupName)) ? groupName : getString(R.string.ConversationActivity_unnamed_group);
        subtitle = null;
      } else {
        final int size = getRecipients().getRecipientsList().size();

        title = getString(R.string.ConversationActivity_group_conversation);
        subtitle = (size == 1) ? getString(R.string.ConversationActivity_d_recipients_in_group_singular)
            : String.format(getString(R.string.ConversationActivity_d_recipients_in_group), size);
      }
    } else {
      title = getString(R.string.ConversationActivity_compose_message);
      subtitle = "";
    }

    getSupportActionBar().setTitle(title);
    getSupportActionBar().setSubtitle(subtitle);

    getWindow().getDecorView().setContentDescription(getString(R.string.conversation_activity__window_description, title));

    this.supportInvalidateOptionsMenu();
  }

  private void initializeDraft() {
    String draftText = getIntent().getStringExtra(DRAFT_TEXT_EXTRA);
    Uri draftImage = getIntent().getParcelableExtra(DRAFT_IMAGE_EXTRA);
    Uri draftAudio = getIntent().getParcelableExtra(DRAFT_AUDIO_EXTRA);
    Uri draftVideo = getIntent().getParcelableExtra(DRAFT_VIDEO_EXTRA);
    String contentType = getIntent().getStringExtra(DRAFT_MEDIA_TYPE_EXTRA);
    if (draftText != null) composeText.setText(draftText);
    if (draftImage != null) addAttachmentImage(draftImage);
    if (draftAudio != null && ContentType.isAudioType(contentType))
      addAttachmentAudio(draftAudio, contentType);
    if (draftVideo != null && ContentType.isVideoType(contentType))
      addAttachmentVideo(draftVideo, contentType);

    if (draftText == null && draftImage == null && draftAudio == null && draftVideo == null) {
      initializeDraftFromDatabase();
    }
  }

  private void initializeEnabledCheck() {
    boolean enabled = !(isPushGroupConversation() && !isActiveGroup());
    composeText.setEnabled(enabled);
    sendButton.setEnabled(enabled);
  }

  private void initializeCharactersLeftViewEnabledCheck() {
    isCharactersLeftViewEnabled = !(isPushGroupConversation() ||
        (TextSecurePreferences.isPushRegistered(this) && !TextSecurePreferences.isFallbackSmsAllowed(this)));
  }

  private void initializeDraftFromDatabase() {
    new AsyncTask<Void, Void, List<Draft>>() {
      @Override
      protected List<Draft> doInBackground(Void... params) {
        MasterCipher masterCipher = new MasterCipher(masterSecret);
        DraftDatabase draftDatabase = DatabaseFactory.getDraftDatabase(ConversationActivity.this);
        List<Draft> results = draftDatabase.getDrafts(masterCipher, threadId);

        draftDatabase.clearDrafts(threadId);

        return results;
      }

      @Override
      protected void onPostExecute(List<Draft> drafts) {
        for (Draft draft : drafts) {
          if (draft.getType().equals(Draft.TEXT)) {
            composeText.setText(draft.getValue());
          } else if (draft.getType().equals(Draft.IMAGE)) {
            addAttachmentImage(Uri.parse(draft.getValue()));
          } else if (draft.getType().equals(Draft.AUDIO)) {
            addAttachmentAudio(Uri.parse(draft.getValue()));
          } else if (draft.getType().equals(Draft.VIDEO)) {
            addAttachmentVideo(Uri.parse(draft.getValue()));
          }
        }
      }
    }.execute();
  }

  private void initializeSecurity() {
    updateSendBarViews();
    SessionStore sessionStore = new TextSecureSessionStore(this, masterSecret);
    Recipient primaryRecipient = getRecipients() == null ? null : getRecipients().getPrimaryRecipient();
    boolean isPushDestination = DirectoryHelper.isPushDestination(this, getRecipients());
    AxolotlAddress axolotlAddress = new AxolotlAddress(primaryRecipient.getNumber(), TextSecureAddress.DEFAULT_DEVICE_ID);
    boolean isSecureDestination = (isSingleConversation() && sessionStore.containsSession(axolotlAddress)) /*|| isPushGroupConversation()*/;

    if (isPushDestination || isSecureDestination) {
      this.isEncryptedConversation = true;
      this.characterCalculator = new EncryptedCharacterCalculator();
    } else {
      this.isEncryptedConversation = false;
      this.characterCalculator = new SmsCharacterCalculator();
    }
    transportButton.initializeAvailableTransports(!recipients.isSingleRecipient() || attachmentManager.isAttachmentPresent());
    bombTransportButton.initializeAvailableSelfDests();
    if (!isPushDestination) transportButton.disableTransport("textsecure");
    if (!isSecureDestination) transportButton.disableTransport("secure_sms");

    if(isGroupConversation()) {
      transportButton.disableTransport("secure_sms");
      transportButton.disableTransport("insecure_sms");
    }

    if (isPushDestination) {
      transportButton.setDefaultTransport("textsecure");
    } else if (isSecureDestination) {
      transportButton.setDefaultTransport("secure_sms");
    } else {
      transportButton.setDefaultTransport("insecure_sms");
    }
    if(transportButton.getSelectedTransport().isForcedPlaintext()) {
      characterCalculator = new SmsCharacterCalculator();

    }
    calculateCharactersRemaining();
    getCurrentMediaSize();
  }

  private void initializeMmsEnabledCheck() {
    new AsyncTask<Void, Void, Boolean>() {
      @Override
      protected Boolean doInBackground(Void... params) {
        return OutgoingLegacyMmsConnection.isConnectionPossible(ConversationActivity.this);
      }

      @Override
      protected void onPostExecute(Boolean isMmsEnabled) {
        ConversationActivity.this.isMmsEnabled = isMmsEnabled;
      }
    }.execute();
  }

  private void initializeIme() {
    if (TextSecurePreferences.isEnterImeKeyEnabled(this)) {
      composeText.setInputType(composeText.getInputType() & (~InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE));
    } else {
      composeText.setInputType(composeText.getInputType() | (InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE));
    }
  }

  private void initializeViews() {
    sendButton = (SendButton) findViewById(R.id.send_button);
    transportButton = (SelectTransportButton) findViewById(R.id.select_transport_button);
    bombTransportButton = (SelfDestructionButton) findViewById(R.id.select_bomb_button);
    composeText = (EditText) findViewById(R.id.embedded_text_editor);
    charactersLeft = (TextView) findViewById(R.id.space_left);
    emojiDrawer = (EmojiDrawer) findViewById(R.id.emoji_drawer);
    emojiToggle = (EmojiToggle) findViewById(R.id.emoji_toggle);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      emojiToggle.setVisibility(View.GONE);
    }

    attachmentAdapter = new AttachmentTypeSelectorAdapter(this);
    attachmentManager = new AttachmentManager(this, this);

    ComposeKeyPressedListener composeKeyPressedListener = new ComposeKeyPressedListener();

    transportButton.setOnClickListener(selectTransportButtonListener);
    transportButton.setDestroyButtonReference(bombTransportButton);
    transportButton.setEnabled(true);
    transportButton.setComposeTextView(composeText);

    bombTransportButton.setOnClickListener(bombTransportButtonListener);
    bombTransportButton.setEnabled(true);
    bombTransportButton.setSelectTransportButtonReference(transportButton);
    bombTransportButton.setComposeTextView(composeText);

    composeText.setOnKeyListener(composeKeyPressedListener);
    composeText.addTextChangedListener(composeKeyPressedListener);
    composeText.setOnEditorActionListener(sendButtonListener);
    composeText.setOnClickListener(composeKeyPressedListener);
    composeText.setOnFocusChangeListener(composeKeyPressedListener);
    emojiDrawer.setComposeEditText(composeText);
    emojiToggle.setOnClickListener(new EmojiToggleListener());

    updateSendBarViews();
  }

  private void initializeResources() {
    recipients = RecipientFactory.getRecipientsForIds(this, getIntent().getLongArrayExtra(RECIPIENTS_EXTRA), true);
    threadId = getIntent().getLongExtra(THREAD_ID_EXTRA, -1);
    distributionType = getIntent().getIntExtra(DISTRIBUTION_TYPE_EXTRA, ThreadDatabase.DistributionTypes.DEFAULT);
    masterSecret = getIntent().getParcelableExtra(MASTER_SECRET_EXTRA);

    recipients.addListener(new RecipientModifiedListener() {
      @Override
      public void onModified(Recipient recipient) {
        initializeTitleBar();
      }
    });
  }

  private void initializeReceivers() {
    securityUpdateReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (intent.getLongExtra("thread_id", -1) == -1)
          return;

        if (intent.getLongExtra("thread_id", -1) == threadId) {
          initializeSecurity();
          initializeTitleBar();
          calculateCharactersRemaining();
        }
      }
    };

    groupUpdateReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.w("ConversationActivity", "Group update received...");
        if (recipients != null) {
          long[] ids = recipients.getIds();
          Log.w("ConversationActivity", "Looking up new recipients...");
          recipients = RecipientFactory.getRecipientsForIds(context, ids, false);
          initializeTitleBar();
        }
      }
    };

    registerReceiver(securityUpdateReceiver,
        new IntentFilter(SecurityEvent.SECURITY_UPDATE_EVENT),
        KeyCachingService.KEY_PERMISSION, null);

    registerReceiver(groupUpdateReceiver,
        new IntentFilter(GroupDatabase.DATABASE_UPDATE_ACTION));
  }

  private void updateSendBarViews() {
    if (!"".equals(composeText.getText().toString()) || attachmentManager.isAttachmentPresent()) {
      sendButton.setOnClickListener(sendButtonListener);
      sendButton.setEnabled(true);
      sendButton.setComposeTextView(composeText);
      sendButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_send_sms_gdata));
    } else {
      sendButton.setOnClickListener(addAttachmentButtonListener);
      sendButton.setEnabled(true);
      sendButton.setComposeTextView(composeText);
      sendButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_attachment_gdata));
    }
  }

  //////// Helper Methods

  private void addAttachment(int type) {
    Log.w("ComposeMessageActivity", "Selected: " + type);
    switch (type) {
      case AttachmentTypeSelectorAdapter.ADD_IMAGE:
        AttachmentManager.selectImage(this, PICK_IMAGE);
        break;
      case AttachmentTypeSelectorAdapter.ADD_VIDEO:
        AttachmentManager.selectVideo(this, PICK_VIDEO);
        break;
      case AttachmentTypeSelectorAdapter.ADD_SOUND:
        AttachmentManager.selectAudio(this, PICK_AUDIO);
        break;
      case AttachmentTypeSelectorAdapter.ADD_CONTACT_INFO:
        AttachmentManager.selectContactInfo(this, PICK_CONTACT_INFO);
        break;
    }
  }

  private void addAttachmentImage(Uri imageUri) {
    try {
      attachmentManager.setImage(imageUri);
    } catch (IOException | BitmapDecodingException e) {
      Log.w(TAG, e);
      attachmentManager.clear();
      Toast.makeText(this, R.string.ConversationActivity_sorry_there_was_an_error_setting_your_attachment,
          Toast.LENGTH_LONG).show();
    }
  }

  private void addAttachmentVideo(Uri videoUri) {
    try {
      attachmentManager.setVideo(videoUri, true);
    } catch (IOException e) {
      attachmentManager.clear();
      Toast.makeText(this, R.string.ConversationActivity_sorry_there_was_an_error_setting_your_attachment,
          Toast.LENGTH_LONG).show();
      Log.w("ComposeMessageActivity", e);
    } catch (MediaTooLargeException e) {
      attachmentManager.clear();

      Toast.makeText(this, getString(R.string.ConversationActivity_sorry_the_selected_video_exceeds_message_size_restrictions,
              (getCurrentMediaSize() / 1024)),
          Toast.LENGTH_LONG).show();
      Log.w("ComposeMessageActivity", e);
    }
  }
  private void addAttachmentVideo(Uri videoUri, String contentType) {
    try {
      attachmentManager.setVideo(videoUri, contentType, true);
    } catch (IOException e) {
      attachmentManager.clear();
      Toast.makeText(this, R.string.ConversationActivity_sorry_there_was_an_error_setting_your_attachment,
          Toast.LENGTH_LONG).show();
      Log.w("ComposeMessageActivity", e);
    } catch (MediaTooLargeException e) {
      attachmentManager.clear();

      Toast.makeText(this, getString(R.string.ConversationActivity_sorry_the_selected_video_exceeds_message_size_restrictions,
              (getCurrentMediaSize() / 1024)),
          Toast.LENGTH_LONG).show();
      Log.w("ComposeMessageActivity", e);
    }
  }

  private void addAttachmentAudio(Uri audioUri, String contentType) {
    try {
      attachmentManager.setAudio(audioUri, contentType, true);
    } catch (IOException e) {
      attachmentManager.clear();
      Toast.makeText(this, R.string.ConversationActivity_sorry_there_was_an_error_setting_your_attachment,
          Toast.LENGTH_LONG).show();
      Log.w("ComposeMessageActivity", e);
    } catch (MediaTooLargeException e) {
      attachmentManager.clear();
      Toast.makeText(this, getString(R.string.ConversationActivity_sorry_the_selected_audio_exceeds_message_size_restrictions,
              (getCurrentMediaSize() / 1024)),
          Toast.LENGTH_LONG).show();
      Log.w("ComposeMessageActivity", e);
    }
  }

  private void addAttachmentAudio(Uri audioUri) {
    try {
      attachmentManager.setAudio(audioUri, true);
    } catch (IOException e) {
      attachmentManager.clear();
      Toast.makeText(this, R.string.ConversationActivity_sorry_there_was_an_error_setting_your_attachment,
          Toast.LENGTH_LONG).show();
      Log.w("ComposeMessageActivity", e);
    } catch (MediaTooLargeException e) {
      attachmentManager.clear();
      Toast.makeText(this, getString(R.string.ConversationActivity_sorry_the_selected_audio_exceeds_message_size_restrictions,
              (getCurrentMediaSize() / 1024)),
          Toast.LENGTH_LONG).show();
      Log.w("ComposeMessageActivity", e);
    }
  }

  private void addAttachmentContactInfo(Uri contactUri) {
    ContactAccessor contactDataList = ContactAccessor.getInstance();
    ContactData contactData = contactDataList.getContactData(this, contactUri);

    if (contactData.numbers.size() == 1) composeText.append(contactData.numbers.get(0).number);
    else if (contactData.numbers.size() > 1) selectContactInfo(contactData);
  }

  private void selectContactInfo(ContactData contactData) {
    final CharSequence[] numbers = new CharSequence[contactData.numbers.size()];
    final CharSequence[] numberItems = new CharSequence[contactData.numbers.size()];

    for (int i = 0; i < contactData.numbers.size(); i++) {
      numbers[i] = contactData.numbers.get(i).number;
      numberItems[i] = contactData.numbers.get(i).type + ": " + contactData.numbers.get(i).number;
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setIcon(R.drawable.ic_contact_picture);
    builder.setTitle(R.string.ConversationActivity_select_contact_info);

    builder.setItems(numberItems, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        composeText.append(numbers[which]);
      }
    });
    builder.show();
  }

  private Drafts getDraftsForCurrentState() {
    Drafts drafts = new Drafts();

    if (!Util.isEmpty(composeText)) {
      drafts.add(new Draft(Draft.TEXT, composeText.getText().toString()));
    }

    for (Slide slide : attachmentManager.getSlideDeck().getSlides()) {
      if (slide.hasAudio()) drafts.add(new Draft(Draft.AUDIO, slide.getUri().toString()));
      else if (slide.hasVideo()) drafts.add(new Draft(Draft.VIDEO, slide.getUri().toString()));
      else if (slide.hasImage()) drafts.add(new Draft(Draft.IMAGE, slide.getUri().toString()));
    }

    return drafts;
  }

  private void saveDraft() {
    if (this.threadId <= 0 || this.recipients == null || this.recipients.isEmpty())
      return;

    final Drafts drafts = getDraftsForCurrentState();
    final long thisThreadId = this.threadId;
    final MasterSecret thisMasterSecret = this.masterSecret.parcelClone();

    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        MasterCipher masterCipher = new MasterCipher(thisMasterSecret);
        DatabaseFactory.getDraftDatabase(ConversationActivity.this).insertDrafts(masterCipher, thisThreadId, drafts);
        ThreadDatabase threadDatabase = DatabaseFactory.getThreadDatabase(ConversationActivity.this);
        if (drafts.size() > 0) {
          threadDatabase.updateSnippet(thisThreadId, drafts.getSnippet(ConversationActivity.this),
              System.currentTimeMillis(), Types.BASE_DRAFT_TYPE);
        } else {
          threadDatabase.update(thisThreadId);
        }
        MemoryCleaner.clean(thisMasterSecret);
        return null;
      }
    }.execute();
  }

  private void calculateCharactersRemaining() {
    int charactersSpent = composeText.getText().toString().length();
    CharacterCalculator.CharacterState characterState = characterCalculator.calculateCharacters(charactersSpent);
    if (characterState.charactersRemaining <= 15 && charactersLeft.getVisibility() != View.VISIBLE && isCharactersLeftViewEnabled) {
      charactersLeft.setVisibility(View.VISIBLE);
    } else if (characterState.charactersRemaining > 15 && charactersLeft.getVisibility() != View.GONE) {
      charactersLeft.setVisibility(View.GONE);
    }
    charactersLeft.setText(characterState.charactersRemaining + "/" + characterState.maxMessageSize + " (" + characterState.messagesSpent + ")");
  }

  private boolean isExistingConversation() {
    return this.recipients != null && this.threadId != -1;
  }

  private boolean isSingleConversation() {
    return getRecipients() != null && getRecipients().isSingleRecipient() && !getRecipients().isGroupRecipient();
  }

  private boolean isActiveGroup() {
    if (!isGroupConversation()) return false;

    try {
      byte[] groupId = GroupUtil.getDecodedId(getRecipients().getPrimaryRecipient().getNumber());
      GroupRecord record = DatabaseFactory.getGroupDatabase(this).getGroup(groupId);

      return record != null && record.isActive();
    } catch (IOException e) {
      Log.w("ConversationActivity", e);
      return false;
    }
  }

  private boolean isGroupConversation() {
    return getRecipients() != null &&
        (!getRecipients().isSingleRecipient() || getRecipients().isGroupRecipient());
  }

  private boolean isPushGroupConversation() {
    return getRecipients() != null && getRecipients().isGroupRecipient();
  }

  private Recipients getRecipients() {
    return this.recipients;
  }

  private String getMessage() throws InvalidMessageException {
    String rawText = composeText.getText().toString();
    String destroyTime = "";
    if (bombTransportButton.getSelectedSelfDestTime() != null && bombTransportButton.isEnabled()) {
      destroyTime = bombTransportButton.getSelectedSelfDestTime().key;
      int value = Integer.parseInt(destroyTime);
      if (value > 0) {
        destroyTime = GUtil.DESTROY_FLAG + value;
        rawText = rawText + " " + destroyTime;
      }
    }
    if (rawText.length() < 1 && !attachmentManager.isAttachmentPresent())
      throw new InvalidMessageException(getString(R.string.ConversationActivity_message_is_empty_exclamation));

    if (!isEncryptedConversation && Tag.isTaggable(rawText))
      rawText = Tag.getTaggedMessage(rawText);

    return rawText;
  }

  private void markThreadAsRead() {
    new AsyncTask<Long, Void, Void>() {
      @Override
      protected Void doInBackground(Long... params) {
        DatabaseFactory.getThreadDatabase(ConversationActivity.this).setRead(params[0]);
        MessageNotifier.updateNotification(ConversationActivity.this, masterSecret);
        return null;
      }
    }.execute(threadId);
  }

  private void sendComplete(long threadId) {
    boolean refreshFragment = (threadId != this.threadId);
    this.threadId = threadId;

    ConversationFragment fragment = getFragment();

    if (fragment == null) {
      return;
    }

    if (refreshFragment) {
      fragment.reload(recipients, threadId);

      initializeTitleBar();
      initializeSecurity();
    }

    fragment.scrollToBottom();
  }

  private ConversationFragment getFragment() {
    return (ConversationFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_content);
  }

  private void sendMessage() {
    try {
      final Recipients recipients = getRecipients();

      if (recipients == null) {
        throw new RecipientFormattingException("Badly formatted");
      }
      if ((!recipients.isSingleRecipient() || recipients.isEmailRecipient()) && !isMmsEnabled) {
        handleManualMmsRequired();
      } else if (attachmentManager.isAttachmentPresent() || !recipients.isSingleRecipient() || recipients.isGroupRecipient() || recipients.isEmailRecipient()) {
        sendMediaMessage(transportButton.getSelectedTransport().isForcedPlaintext(), transportButton.getSelectedTransport().isForcedSms());
      } else {
        sendTextMessage(transportButton.getSelectedTransport().isForcedPlaintext(), transportButton.getSelectedTransport().isForcedSms());
      }
    } catch (RecipientFormattingException ex) {
      Toast.makeText(ConversationActivity.this,
          R.string.ConversationActivity_recipient_is_not_a_valid_sms_or_email_address_exclamation,
          Toast.LENGTH_LONG).show();
      Log.w(TAG, ex);
    } catch (InvalidMessageException ex) {
      Toast.makeText(ConversationActivity.this, R.string.ConversationActivity_message_is_empty_exclamation,
          Toast.LENGTH_SHORT).show();
      Log.w(TAG, ex);
    }
  }

  private void sendMediaMessage(boolean forcePlaintext, final boolean forceSms)
      throws InvalidMessageException {
    final Context context = getApplicationContext();
    SlideDeck slideDeck;

    if (attachmentManager.isAttachmentPresent()) {
      slideDeck = new SlideDeck(attachmentManager.getSlideDeck());
    } else slideDeck = new SlideDeck();

    OutgoingMediaMessage outgoingMessage = new OutgoingMediaMessage(this, recipients, slideDeck,
        getMessage(), distributionType);

    if (isEncryptedConversation && !forcePlaintext) {
      outgoingMessage = new OutgoingSecureMediaMessage(outgoingMessage);
    }

    attachmentManager.clear();
    composeText.setText("");

    new AsyncTask<OutgoingMediaMessage, Void, Long>() {
      @Override
      protected Long doInBackground(OutgoingMediaMessage... messages) {
        return MessageSender.send(context, masterSecret, messages[0], threadId, forceSms);
      }

      @Override
      protected void onPostExecute(Long result) {
        sendComplete(result);
      }
    }.execute(outgoingMessage);
  }

  private void sendTextMessage(boolean forcePlaintext, final boolean forceSms)
      throws InvalidMessageException {
    final Context context = getApplicationContext();
    OutgoingTextMessage message;

    if (isEncryptedConversation && !forcePlaintext) {
      message = new OutgoingEncryptedMessage(recipients, getMessage());
    } else {
      message = new OutgoingTextMessage(recipients, getMessage());
    }

    this.composeText.setText("");

    new AsyncTask<OutgoingTextMessage, Void, Long>() {
      @Override
      protected Long doInBackground(OutgoingTextMessage... messages) {

        return MessageSender.send(context, masterSecret, messages[0], threadId, forceSms);
      }

      @Override
      protected void onPostExecute(Long result) {
        sendComplete(result);
      }
    }.execute(message);

  }

  public int getCurrentMediaSize() {
    int currentSize = MediaConstraints.CURRENT_MEDIA_SIZE;
    if(transportButton != null) {
      currentSize = (transportButton.getSelectedTransport().key.equals("secure_sms")
          || transportButton.getSelectedTransport().key.equals("insecure_sms")) ?
          MmsMediaConstraints.MAX_MESSAGE_SIZE : PushMediaConstraints.MAX_MESSAGE_SIZE;
      MediaConstraints.CURRENT_MEDIA_SIZE = currentSize;
    }
    return currentSize;
  }
  // Listeners

  private class AttachmentTypeListener implements DialogInterface.OnClickListener {
    @Override
    public void onClick(DialogInterface dialog, int which) {
      addAttachment(attachmentAdapter.buttonToCommand(which));
    }
  }

  private class EmojiToggleListener implements OnClickListener {
    @Override
    public void onClick(View v) {
      InputMethodManager input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

      if (emojiDrawer.isOpen()) {
        input.showSoftInput(composeText, 0);
        emojiDrawer.hide();
      } else {
        input.hideSoftInputFromWindow(composeText.getWindowToken(), 0);

        emojiDrawer.show();
      }
    }
  }

  private class SendButtonListener implements OnClickListener, TextView.OnEditorActionListener {
    @Override
    public void onClick(View v) {
      sendMessage();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
      if (actionId == EditorInfo.IME_ACTION_SEND) {
        sendButton.performClick();
        composeText.clearFocus();
        return true;
      }
      return false;
    }
  }

  private class AddContactButtonListener implements OnClickListener {
    @Override
    public void onClick(View v) {
      final Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
      intent.putExtra(ContactsContract.Intents.Insert.PHONE, recipients.getPrimaryRecipient().getNumber());
      intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
      startActivity(intent);
    }
  }

  private class DestroyButtonListener implements OnClickListener, TextView.OnEditorActionListener {
    @Override
    public void onClick(View v) {
      Log.d("GDATA", "DESTROY CLICK");
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
      if (actionId == EditorInfo.IME_ACTION_SEND) {
        sendButton.performClick();
        composeText.clearFocus();
        return true;
      }
      return false;
    }
  }

  private class SelectTransportListener implements OnClickListener, TextView.OnEditorActionListener {
    @Override
    public void onClick(View v) {
      charactersLeft.setVisibility(View.GONE);
      transportButton.performLongClick();
      attachmentManager.clear();

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
      if (actionId == EditorInfo.IME_ACTION_SEND) {
        sendButton.performClick();
        composeText.clearFocus();
        return true;
      }
      return false;
    }
  }

  private class BombTransportListener implements OnClickListener, TextView.OnEditorActionListener {
    @Override
    public void onClick(View v) {
      bombTransportButton.performLongClick();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

      return false;
    }
  }

  private class AddAttachmentListener implements OnClickListener, TextView.OnEditorActionListener {
    @Override
    public void onClick(View v) {
      handleAddAttachment();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
      if (actionId == EditorInfo.IME_ACTION_SEND) {
        sendButton.performClick();
        composeText.clearFocus();
        return true;
      }
      return false;
    }
  }


  private class ComposeKeyPressedListener implements OnKeyListener, OnClickListener, TextWatcher, OnFocusChangeListener {
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
      if (event.getAction() == KeyEvent.ACTION_DOWN) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
          if (TextSecurePreferences.isEnterSendsEnabled(ConversationActivity.this)) {
            sendButton.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            sendButton.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
            return true;
          }
        }
      }
      return false;
    }

    @Override
    public void onClick(View v) {
      if (emojiDrawer.isOpen()) {
        emojiToggle.performClick();
      }
    }

    @Override
    public void afterTextChanged(Editable s) {
      calculateCharactersRemaining();

      updateSendBarViews();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
      if (hasFocus && emojiDrawer.isOpen()) {
        emojiToggle.performClick();
      }
    }
  }

  @Override
  public void setComposeText(String text) {
    this.composeText.setText(text);
  }

  @Override
  public void onAttachmentChanged() {
    initializeSecurity();
  }
}
