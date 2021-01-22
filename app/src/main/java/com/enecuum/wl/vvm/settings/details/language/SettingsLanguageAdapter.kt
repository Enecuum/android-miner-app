package com.enecuum.wl.vvm.settings.details.language

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enecuum.wl.R
import com.enecuum.wl.utils.Constants.LANGUAGE_KEY
import com.pixplicity.easyprefs.library.Prefs

class SettingsLanguageAdapter(private val listener: OnItemChooseListener) :
    RecyclerView.Adapter<SettingsLanguageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsLanguageViewHolder {
        return SettingsLanguageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_language, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return 2
    }

    override fun onBindViewHolder(holder: SettingsLanguageViewHolder, position: Int) {

        holder.languageTitle.text =
            holder.languageTitle.context.resources.getStringArray(R.array.language_list)[position]
        holder.languageSubtitle.text =
            holder.languageTitle.context.resources.getStringArray(R.array.language_subtitle_list)[position]

        if (holder.languageTitle.context.resources.getStringArray(R.array.language_code)[position] == Prefs.getString(
                LANGUAGE_KEY,
                ""
            )
        ) {
            holder.imgChoosed.visibility = View.VISIBLE
        } else {
            holder.imgChoosed.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            listener.onItemClick(holder.languageTitle.context.resources.getStringArray(R.array.language_code)[position])
        }

    }

    interface OnItemChooseListener {
        fun onItemClick(languageCode: String)
    }

}