package com.enecuum.wl.vvm.settings.details.language

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.enecuum.wl.R

class SettingsLanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val languageTitle: TextView = itemView.findViewById(R.id.lblLanguageTitle)
    val languageSubtitle: TextView = itemView.findViewById(R.id.lblLanguageSubtitle)
    val imgChoosed: ImageView = itemView.findViewById(R.id.imgChoosed)

}