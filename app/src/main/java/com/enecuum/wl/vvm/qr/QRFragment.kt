package com.enecuum.wl.vvm.qr

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.enecuum.wl.R
import com.enecuum.wl.utils.BaseFragment
import com.enecuum.wl.utils.Constants.BALANCE_KEY
import com.enecuum.wl.utils.Constants.PUBLIC_REF_KEY
import com.enecuum.wl.vvm.host_main.MainActivity
import com.enecuum.lib.KeyStore
import com.google.zxing.EncodeHintType
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.fragment_main.*
import net.glxn.qrgen.android.QRCode
import org.koin.android.viewmodel.ext.android.viewModel
import java.math.BigDecimal
import java.math.RoundingMode


class QRFragment : BaseFragment() {

    private val viewModel: QRViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        btnSettings.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_mainFragment_to_settingsFragment)
            activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.GONE
        }

        btnBuy.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_mainFragment_to_buyFragment)
            activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.GONE
        }

        viewModel.referrerStake.observe(viewLifecycleOwner, Observer {
            when (it) {
                BigDecimal.ZERO -> Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT)
                    .show()
                else -> {

                    val balance = Prefs.getString(BALANCE_KEY, "")

                    if (balance != "" && BigDecimal(balance).multiply(
                            BigDecimal.valueOf(
                                0.0000000001
                            )
                        ) >= it
                    ) {
                        showReferrerDialog()
                    } else {
                        showBuyDialog(it.setScale(0, RoundingMode.HALF_DOWN).toPlainString())
                    }
                }
            }
        })

        viewModel.getTokenInfo()
    }

    override fun onResume() {
        super.onResume()
        activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.VISIBLE
        (activity!! as MainActivity).selectedMenuId = R.id.bottomMenuHome
    }

    private fun showBuyDialog(referrerStake: String) {
        AlertDialog.Builder(context!!)
            .setCancelable(false)
            .setTitle(R.string.referral_buy_title)
            .setMessage(resources.getString(R.string.referral_buy_message, referrerStake))
            .setNeutralButton(resources.getString(R.string.cancel)) { dialogInterface, _ ->
                activity?.onBackPressed()
            }
            .setPositiveButton(resources.getString(R.string.buy_enq)) { dialogInterface, _ ->
                Navigation.findNavController(this.view!!)
                    .navigate(R.id.action_qrFragment_to_buyFragment)
            }
            .create()
            .show()
    }

    private fun showReferrerDialog() {
        val img = ImageView(context)
        img.setImageBitmap(
            QRCode.from(KeyStore.userReferral(context!!)).withSize(500, 500)
                .withColor(
                    Color.BLACK,
                    ContextCompat.getColor(requireContext(), R.color.colorAccent)
                )
                .withHint(EncodeHintType.MARGIN, 2)
                .bitmap()
        )

        AlertDialog.Builder(context!!)
            .setCancelable(false)
            .setTitle(R.string.referral_title)
            //TODO here our value /2
            .setMessage(R.string.referral_message)
            .setNeutralButton(resources.getString(R.string.cancel)) { dialogInterface, _ ->
                activity?.onBackPressed()
            }
            .setNegativeButton(resources.getString(R.string.save)) { dialogInterface, _ ->
                if (ContextCompat.checkSelfPermission(
                        context!!,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        WRITE_EXTERNAL_CODE
                    )
                } else {
                    val bitmap = QRCode.from(KeyStore.userReferral(context!!)).withSize(500, 500)
                        .withColor(
                            Color.BLACK,
                            ContextCompat.getColor(requireContext(), R.color.colorAccent)
                        )
                        .bitmap()
                    val res = MediaStore.Images.Media.insertImage(
                        requireContext().contentResolver,
                        bitmap,
                        "ref_code",
                        "ref_code"
                    )
                    if (res != null) {
                        Toast.makeText(context, R.string.qr_saved, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show()
                    }
                }
                activity?.onBackPressed()
            }
            .setView(img)
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val bitmap = QRCode.from(Prefs.getString(PUBLIC_REF_KEY, "")).withSize(500, 500)
                    .withColor(
                        Color.BLACK,
                        ContextCompat.getColor(requireContext(), R.color.colorAccent)
                    )
                    .bitmap()
                val res = MediaStore.Images.Media.insertImage(
                    requireContext().contentResolver,
                    bitmap,
                    "ref_code",
                    "ref_code"
                )
                if (res != null) {
                    Toast.makeText(context, R.string.qr_saved, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        const val WRITE_EXTERNAL_CODE = 303
    }
}