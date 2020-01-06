package org.thoughtcrime.securesms.loki.redesign.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_qr_code.*
import kotlinx.android.synthetic.main.fragment_view_my_qr_code.*
import network.loki.messenger.R
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.conversation.ConversationActivity
import org.thoughtcrime.securesms.database.Address
import org.thoughtcrime.securesms.database.DatabaseFactory
import org.thoughtcrime.securesms.database.ThreadDatabase
import org.thoughtcrime.securesms.loki.redesign.fragments.ScanQRCodeWrapperFragment
import org.thoughtcrime.securesms.loki.redesign.fragments.ScanQRCodeWrapperFragmentDelegate
import org.thoughtcrime.securesms.loki.redesign.utilities.QRCodeUtilities
import org.thoughtcrime.securesms.loki.toPx
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.util.TextSecurePreferences
import org.whispersystems.signalservice.loki.utilities.PublicKeyValidation


class QRCodeActivity : PassphraseRequiredActionBarActivity(), ScanQRCodeWrapperFragmentDelegate {
    private val adapter = QRCodeActivityAdapter(this)

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?, isReady: Boolean) {
        super.onCreate(savedInstanceState, isReady)
        // Set content view
        setContentView(R.layout.activity_qr_code)
        // Set title
        supportActionBar!!.title = "QR Code"
        // Set up view pager
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }
    // endregion

    // region Interaction
    override fun handleQRCodeScanned(hexEncodedPublicKey: String) {
        createPrivateChatIfPossible(hexEncodedPublicKey)
    }

    fun createPrivateChatIfPossible(hexEncodedPublicKey: String) {
        if (!PublicKeyValidation.isValid(hexEncodedPublicKey)) { return Toast.makeText(this, "Invalid Session ID", Toast.LENGTH_SHORT).show() }
        val masterHexEncodedPublicKey = TextSecurePreferences.getMasterHexEncodedPublicKey(this)
        val userHexEncodedPublicKey = TextSecurePreferences.getLocalNumber(this)
        val targetHexEncodedPublicKey = if (hexEncodedPublicKey == masterHexEncodedPublicKey) userHexEncodedPublicKey else hexEncodedPublicKey
        val recipient = Recipient.from(this, Address.fromSerialized(targetHexEncodedPublicKey), true)
        val intent = Intent(this, ConversationActivity::class.java)
        intent.putExtra(ConversationActivity.ADDRESS_EXTRA, recipient.address)
        intent.putExtra(ConversationActivity.TEXT_EXTRA, getIntent().getStringExtra(ConversationActivity.TEXT_EXTRA))
        intent.setDataAndType(getIntent().data, getIntent().type)
        val existingThread = DatabaseFactory.getThreadDatabase(this).getThreadIdIfExistsFor(recipient)
        intent.putExtra(ConversationActivity.THREAD_ID_EXTRA, existingThread)
        intent.putExtra(ConversationActivity.DISTRIBUTION_TYPE_EXTRA, ThreadDatabase.DistributionTypes.DEFAULT)
        startActivity(intent)
        finish()
    }
    // endregion
}

// region Adapter
private class QRCodeActivityAdapter(val activity: QRCodeActivity) : FragmentPagerAdapter(activity.supportFragmentManager) {

    override fun getCount(): Int {
        return 2
    }

    override fun getItem(index: Int): Fragment {
        return when (index) {
            0 -> ViewMyQRCodeFragment()
            1 -> {
                val result = ScanQRCodeWrapperFragment()
                result.delegate = activity
                result.message = "Scan someone\'s QR code to start a conversation with them"
                result
            }
            else -> throw IllegalStateException()
        }
    }

    override fun getPageTitle(index: Int): CharSequence? {
        return when (index) {
            0 -> "View My QR Code"
            1 -> "Scan QR Code"
            else -> throw IllegalStateException()
        }
    }
}
// endregion

// region View My QR Code Fragment
class ViewMyQRCodeFragment : Fragment() {

    private val hexEncodedPublicKey: String
        get() {
            val masterHexEncodedPublicKey = TextSecurePreferences.getMasterHexEncodedPublicKey(context!!)
            val userHexEncodedPublicKey = TextSecurePreferences.getLocalNumber(context!!)
            return masterHexEncodedPublicKey ?: userHexEncodedPublicKey
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_view_my_qr_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val size = toPx(240, resources)
        val qrCode = QRCodeUtilities.encode(hexEncodedPublicKey, size)
        qrCodeImageView.setImageBitmap(qrCode)
        val explanation = SpannableStringBuilder("This is your unique public QR code. Other users may scan this in order to begin a conversation with you.")
        explanation.setSpan(StyleSpan(Typeface.BOLD), 8, 34, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        explanationTextView.text = explanation
        shareButton.setOnClickListener { shareQRCode() }
    }

    private fun shareQRCode() {
        // TODO: Implement
    }
}
// endregion