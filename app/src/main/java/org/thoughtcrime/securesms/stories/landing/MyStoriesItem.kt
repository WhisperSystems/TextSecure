package org.thoughtcrime.securesms.stories.landing

import android.view.View
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.avatar.view.AvatarView
import org.thoughtcrime.securesms.badges.BadgeImageView
import org.thoughtcrime.securesms.components.settings.PreferenceModel
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.util.adapter.mapping.LayoutFactory
import org.thoughtcrime.securesms.util.adapter.mapping.MappingAdapter
import org.thoughtcrime.securesms.util.adapter.mapping.MappingViewHolder

/**
 * Item displayed on an empty Stories landing page allowing the user to add a new story.
 */
object MyStoriesItem {

  fun register(mappingAdapter: MappingAdapter) {
    mappingAdapter.registerFactory(Model::class.java, LayoutFactory(::ViewHolder, R.layout.stories_landing_item_my_stories))
  }

  class Model(
    val onClick: () -> Unit,
    val onClickThumbnail: () -> Unit
  ) : PreferenceModel<Model>() {
    override fun areItemsTheSame(newItem: Model): Boolean = true
  }

  private class ViewHolder(itemView: View) : MappingViewHolder<Model>(itemView) {

    private val thumbnail: View = itemView.findViewById(R.id.story)
    private val avatarView: AvatarView = itemView.findViewById(R.id.avatar)
    private val badgeView: BadgeImageView = itemView.findViewById(R.id.badge)

    override fun bind(model: Model) {
      if (payload.contains(LandingPayload.RESUMED)) {
        return
      }

      itemView.setOnClickListener { model.onClick() }
      thumbnail.setOnClickListener { model.onClickThumbnail() }

      avatarView.displayProfileAvatar(Recipient.self())
      badgeView.setBadgeFromRecipient(Recipient.self())
    }
  }
}
