package com.enecuum.wl.vvm.version_alert

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.enecuum.wl.R
import kotlinx.android.synthetic.main.fragment_version_alert.*

class VersionAlertActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_version_alert)

        btnUpdate.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(intent.getStringExtra("url"))
            startActivity(i)
        }
    }

}