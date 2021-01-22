package com.enecuum.wl.vvm.main

import android.Manifest
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.enecuum.wl.Config.GET_BALANCE_TIMEOUT_IN_MILLIS_ACTIVE
import com.enecuum.wl.Config.GET_BALANCE_TIMEOUT_IN_MILLIS_INACTIVE
import com.enecuum.wl.R
import com.enecuum.wl.service.DateService
import com.enecuum.wl.service.ServiceNotification
import com.enecuum.wl.service.ServiceStatus
import com.enecuum.wl.utils.*
import com.enecuum.wl.utils.Constants.BALANCE_KEY
import com.enecuum.wl.utils.Constants.MINIMUM_STAKE_KEY
import com.enecuum.wl.utils.Constants.PUBLIC_KEY
import com.enecuum.wl.utils.Constants.PUBLIC_REF_KEY
import com.enecuum.wl.utils.Constants.REFERRAL_PUBLIC_KEY
import com.enecuum.wl.utils.Constants.REFERRAL_SHOWED_KEY
import com.enecuum.wl.utils.Constants.URL_KEY
import com.enecuum.wl.vvm.host_main.MainActivity
import com.enecuum.lib.KeyStore
import com.enecuum.lib.SageSign
import com.enecuum.lib.api.MinStake
import com.enecuum.lib.api.main.ApiRouter
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.zxing.EncodeHintType
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.glxn.qrgen.android.QRCode
import org.koin.android.viewmodel.ext.android.viewModel
import java.math.BigDecimal
import java.math.RoundingMode


class MainFragment : BaseFragment() {

    private val viewModel: MainViewModel by viewModel()
    private var serviceBound = false
    private var dialogClicked = false
    private var enabled = false

    private var dateService: DateService? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (Prefs.getString(BALANCE_KEY, "").isNotEmpty()) {
            val balance = BigDecimal(Prefs.getString(BALANCE_KEY, "")).multiply(
                BigDecimal.valueOf(0.0000000001)
            )
                .setScale(10, RoundingMode.HALF_DOWN)
            lblAmount.text = balance.toPlainString()
        }

        vCard.setOnClickListener {
            viewModel.getBalance()
        }

        btnSettings.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_mainFragment_to_settingsFragment)
            activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.GONE
        }

        btnBuy.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_mainFragment_to_buyFragment)
            activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.GONE
        }

        viewModel.referrerStake.observe(viewLifecycleOwner, Observer {
            if (!dialogClicked) return@Observer
            dialogClicked = false
            vProgress.hide()
            when (it) {
                BigDecimal.ZERO -> Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT)
                    .show()
                else -> {
                    if (BigDecimal(Prefs.getString(BALANCE_KEY, "")) >= it) {
                        showReferrerDialog()
                    } else {
                        showBuyDialog(
                            it.multiply(BigDecimal.valueOf(0.0000000001))
                                .setScale(0, RoundingMode.HALF_DOWN).toPlainString()
                        )
                    }
                }
            }
        })
        viewModel.minStake.observe(viewLifecycleOwner, Observer {
            enableButton(it)
        })

        viewModel.observePrice(viewLifecycleOwner, {

            if (Prefs.getString(BALANCE_KEY, "").isNotEmpty()) {
                val balance = BigDecimal(Prefs.getString(BALANCE_KEY, ""))
                    .multiply(BigDecimal.valueOf(0.0000000001))
                    .setScale(10, RoundingMode.HALF_DOWN)

                lblCourse.text = resources.getString(
                    R.string.enq_as_usd,
                    it.usd
                )
                lblConverted.text = resources.getString(
                    R.string.dollar_value,
                    (BigDecimal(it.usd) * balance).setScale(2, RoundingMode.HALF_EVEN)
                )
            }
        })
    }

    override fun onResume() {
        super.onResume()
        activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.VISIBLE
        (activity!! as MainActivity).selectedMenuId = R.id.bottomMenuHome

        if (!Prefs.getBoolean(REFERRAL_SHOWED_KEY, false) && Prefs.getString(
                REFERRAL_PUBLIC_KEY,
                ""
            ).isEmpty()
        ) {
            val bundle = bundleOf("from_main" to true)
            Navigation.findNavController(view!!)
                .navigate(R.id.action_mainFragment_to_referralFragment, bundle)
            activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.GONE
        }
    }

    override fun onStart() {
        bindService()
        super.onStart()

        viewModel.observeBalance(this, Observer {
            if (it == null) {
                Log.d("MainFragment", "Amount is null")
                return@Observer
            }
            when {
                it.amount != resources.getString(R.string.ellipsis_placeholder) -> {
                    Prefs.putString(BALANCE_KEY, it.amount)
                    val balance =
                        BigDecimal(Prefs.getString(BALANCE_KEY, "")).multiply(
                            BigDecimal.valueOf(
                                0.0000000001
                            )
                        )
                            .setScale(10, RoundingMode.HALF_DOWN)
                    lblAmount.text = balance.toPlainString()
                }
                Prefs.getString(BALANCE_KEY, "").isNotEmpty() -> {
                    val balance =
                        BigDecimal(Prefs.getString(BALANCE_KEY, "")).multiply(
                            BigDecimal.valueOf(
                                0.0000000001
                            )
                        )
                            .setScale(10, RoundingMode.HALF_DOWN)
                    lblAmount.text = balance.toPlainString()
                }
                else -> lblAmount.text = "0.0"
            }
        })
    }

    override fun onStop() {
        unbindService()
        viewModel.clearBalanceObserver(this)
        super.onStop()
    }

    private fun updateButton(status: ServiceStatus) {
        when (status) {
            ServiceStatus.STARTED -> {
                viewModel.balanceDelay = GET_BALANCE_TIMEOUT_IN_MILLIS_ACTIVE
                btnStart.setOnClickListener {
                    btnStart.isEnabled = false
                    stopService()
                    ServiceNotification.clear(context)
                    CoroutineScope(Dispatchers.Default).launch {
                        delay(3000)
                        ServiceNotification.clear(context)
                    }
                    btnStart.isEnabled = true
                }
                btnActText.text = resources.getString(R.string.stop_activity)
            }
            ServiceStatus.CONNECTING -> {
                viewModel.balanceDelay = GET_BALANCE_TIMEOUT_IN_MILLIS_INACTIVE
                btnActText.text = resources.getString(R.string.connecting_btn)
                btnStart.setOnClickListener {
                    btnStart.isEnabled = false
                    Constants.refuseReconnection = true
                    stopService()
                    updateButton(ServiceStatus.STOPPED)
                    btnStart.isEnabled = true
                }
                (activity!! as MainActivity).checkVersion()
            }
            ServiceStatus.STOPPED,
            ServiceStatus.TERMINATED_DUP_KEY,
            ServiceStatus.TERMINATED_PROTO_ERROR -> {
                viewModel.balanceDelay = GET_BALANCE_TIMEOUT_IN_MILLIS_INACTIVE
                btnStart.setOnClickListener {
                    if (enabled) {
                        btnStart.isEnabled = false
                        updateButton(ServiceStatus.CONNECTING)
                        if (Prefs.getString(URL_KEY, "").isNotEmpty()) {
                            startService(15000)
                            viewModel.getBalance()
                        } else {

                            if (Prefs.getString(PUBLIC_KEY, "").isEmpty()) {
                                SageSign.generateKeys()
                            }

                            Prefs.putString(URL_KEY, ApiRouter.wsURL)
                            startService(15000)
                            viewModel.getBalance()
                        }
                        btnStart.isEnabled = true

                        //TODO: temporarily removed
//                        checkBatteryOptimization()
                    } else {
                        showDialog()
                    }
                }
                btnActText.text = resources.getString(R.string.start_activity)

                if (status == ServiceStatus.TERMINATED_DUP_KEY) {

                } else if (status == ServiceStatus.TERMINATED_PROTO_ERROR) {
                    stopService()
                    ServiceNotification.clear(context)

                    (activity!! as MainActivity).checkVersion()
                }
            }
        }

    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            dateService = (binder as DateService.DatesBinder).getService()
            dateService?.let { service ->
                service.service.status.observe(viewLifecycleOwner, Observer { status ->
                    updateButton(status)
                })
                if (!service.running) updateButton(ServiceStatus.STOPPED)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            dateService = null
        }
    }

    private fun startService(ms: Long) {
        val s = ms * 1000
        val intent = Intent(context, DateService::class.java).apply {
            putExtra(URL_KEY, ApiRouter.wsURL)
            putExtra(Constants.MS_KEY, s)
        }
        if (!serviceBound) bindService()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(intent)
        } else {
            activity?.startService(intent)
        }
    }

    private fun stopService() {
        val intent = Intent(context, DateService::class.java)
        activity?.stopService(intent)
        unbindService()
        updateButton(ServiceStatus.STOPPED)
    }

    private fun bindService() {
        activity?.bindService(
            Intent(context, DateService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
        serviceBound = true
    }

    private fun unbindService() {
        if (serviceBound) {
            try {
                activity?.unbindService(connection)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
            }
            serviceBound = false
        }
    }

    private fun showBuyDialog(referrerStake: String) {
        AlertDialog.Builder(context!!)
            .setTitle(R.string.referral_buy_title)
            .setMessage(resources.getString(R.string.referral_buy_message, referrerStake))
            .setPositiveButton(resources.getString(R.string.buy_enq)) { dialogInterface, _ ->
                Navigation.findNavController(this.view!!)
                    .navigate(R.id.action_mainFragment_to_buyFragment)
                activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.GONE
                dialogInterface.dismiss()
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
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

        viewModel.referrerShare.value

        AlertDialog.Builder(context!!)
            .setTitle(R.string.referral_title)
            //TODO here our value /2
            .setMessage(R.string.referral_message)
            .setPositiveButton(resources.getString(R.string.share_code)) { dialogInterface, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    shareQRcode(
                        KeyStore.userReferral(context!!), requireContext(),
                        ContextCompat.getColor(requireContext(), R.color.colorAccent)
                    )
                }
                dialogInterface.dismiss()
            }
            .setNeutralButton(resources.getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
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
                dialogInterface.dismiss()
            }
            .setView(img)
            .create()
            .show()
    }

    private fun enableButton(minStake: MinStake) {

        Prefs.putString(MINIMUM_STAKE_KEY, minStake.minStake.toString())
        enabled = BigDecimal(Prefs.getString(BALANCE_KEY, "0")).multiply(BigDecimal.valueOf(0.0000000001)) >= minStake.minStake
        if (enabled) {
            btnStart.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.circle_accent
                )
            )
            btnStart.addCircleRipple()
            btnActText.setTextColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.colorBlue,
                    context?.theme
                )
            )
        } else {
            stopService()
            btnStart.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.circle_grey
                )
            )
            btnStart.removeRipple()
            btnActText.setTextColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.colorPrimaryLight,
                    context?.theme
                )
            )
        }
    }

    private fun showDialog() {
        val minStake = BigDecimal(Prefs.getString(MINIMUM_STAKE_KEY, ""))
            .setScale(0, RoundingMode.HALF_DOWN).toPlainString()
        val message =
            SpannableString(resources.getString(R.string.min_stake_dialog_message, minStake))
        Linkify.addLinks(message, Linkify.WEB_URLS)
        val alertDialog = AlertDialog.Builder(context!!)
            .setTitle(resources.getString(R.string.min_stake_dialog_title, minStake))
            .setMessage(message)
            .setPositiveButton(resources.getString(R.string.buy_enq)) { dialogInterface, _ ->
                Navigation.findNavController(this.view!!)
                    .navigate(R.id.action_mainFragment_to_buyFragment)
                activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.GONE
                dialogInterface.dismiss()
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        alertDialog.show()
        (alertDialog.findViewById(android.R.id.message) as TextView).movementMethod =
            LinkMovementMethod.getInstance()
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