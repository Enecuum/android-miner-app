package com.enecuum.wl.vvm.send_receive.tabs.send

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.Observer
import com.enecuum.wl.BuildConfig
import com.enecuum.wl.R
import com.enecuum.wl.utils.AmountValue
import com.enecuum.wl.utils.BaseFragment
import com.enecuum.wl.utils.Constants
import com.enecuum.wl.utils.TransactionSign
import com.enecuum.wl.vvm.scanner.SimpleScannerActivity
import com.enecuum.lib.KeyStore
import com.enecuum.lib.api.Transaction
import com.pixplicity.easyprefs.library.Prefs
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import kotlinx.android.synthetic.main.fragment_send.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.regex.Pattern
import kotlin.random.Random

class SendFragment : BaseFragment() {

    var balance = BigDecimal(0)
    val viewModel: SendViewModel by viewModel()
    var send = false
    var sended = false

    lateinit var amountTextWatcher: AmountTextWatcher

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_send, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        imgScan.setOnClickListener {
            if (checkSelfPermission(context!!, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE
                )
            } else {
                viewModel.removeObserver(this)
                startActivityForResult(
                    Intent(context, SimpleScannerActivity::class.java),
                    SCAN_REQUEST_CODE
                )
            }
        }

        if (Prefs.getString(Constants.BALANCE_KEY, "").isNotEmpty()) {
            balance = BigDecimal(Prefs.getString(Constants.BALANCE_KEY, "")).multiply(
                BigDecimal.valueOf(0.0000000001)
            )
                .setScale(10, RoundingMode.HALF_DOWN)
            lblBalance.text = resources.getString(R.string.balance_enq, balance.toPlainString())
        }

/*
        sbAmount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (balance.multiply(BigDecimal.valueOf(10000000000))
                        .setScale(10, RoundingMode.HALF_DOWN)
                        .toInt() > 0 &&
                    progress > 0
                ) {
                    txtAmount.setText(
                        "${balance.divide(BigDecimal(100)).multiply(BigDecimal(progress)).setScale(
                            10,
                            RoundingMode.HALF_DOWN
                        )}"
                    )
                } else {
                    txtAmount.setText("0.0000000000")
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                txtAmount.removeTextChangedListener(amountTextWatcher)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                txtAmount.addTextChangedListener(amountTextWatcher)
            }

        })
        */

        sbAmountBar.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
                txtAmount.removeTextChangedListener(amountTextWatcher)
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                txtAmount.addTextChangedListener(amountTextWatcher)
            }

            override fun onSeeking(seekParams: SeekParams?) {
                if (balance > BigDecimal.ZERO &&
                    seekParams!!.progress > 0
                ) {
                    txtAmount.setText(
                        "${
                            balance.divide(BigDecimal(100))
                                .multiply(BigDecimal(seekParams.progress)).setScale(
                                    10,
                                    RoundingMode.HALF_DOWN
                                )
                        }"
                    )
                } else {
                    txtAmount.setText("0.0000000000")
                }
            }

        }

        txtTo.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = txtTo.text.toString()
                if (text.isNotEmpty() && !Pattern.matches("(02|03)[0-9a-fA-F]{64}", text)) {
                    txtTo.setText("")
                    Toast.makeText(
                        context!!,
                        resources.getString(R.string.wrong_format),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                hideSoftKeyboard()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        amountTextWatcher = AmountTextWatcher(balance, sbAmountBar, txtAmount)
        txtAmount.addTextChangedListener(amountTextWatcher)
        txtAmount.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (txtAmount.text.toString().isNotEmpty()
                    && BigDecimal(txtAmount.text.toString()) > balance
                ) {
                    if (balance > BigDecimal.valueOf(0.0000000001)) {
                        txtAmount.setText("$balance")
                    } else {
                        txtAmount.setText("0.0000000000")
                    }
                }
                hideSoftKeyboard()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        btnSend.setOnClickListener {
            if (networkAvailable(true)) {
                send = true
                viewModel.getBalance(context)
            }
        }

    }

    private fun showTransactionErrorResult() {
        AlertDialog.Builder(context!!)
            .setTitle(resources.getString(R.string.transaction_result))
            .setMessage(resources.getString(R.string.error))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SCAN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            txtTo.setText(data?.getStringExtra(SCAN_RESULT_EXTRA))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(
                    Intent(context, SimpleScannerActivity::class.java),
                    SCAN_REQUEST_CODE
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.observeBalance(this, Observer {
            it?.let {

                try {

                    balance = BigDecimal(it.amount.toBigInteger()).multiply(
                        BigDecimal.valueOf(0.0000000001)
                    ).setScale(10, RoundingMode.HALF_DOWN)

                } catch (e: Exception) {

                    BigDecimal(
                        Prefs.getString(
                            Constants.BALANCE_KEY,
                            ""
                        )
                    ).multiply(BigDecimal.valueOf(0.0000000001))
                        .setScale(10, RoundingMode.HALF_DOWN)
                }

                lblBalance.text = resources.getString(R.string.balance_enq, balance.toPlainString())

                if (send) {

                    if (balance <= BigDecimal.valueOf(0)) {

                        Toast.makeText(
                            context!!,
                            resources.getString(R.string.zero_balance),
                            Toast.LENGTH_SHORT
                        ).show()

                    } else if (balance < BigDecimal.valueOf(txtAmount.text.toString().toDouble())) {

                        Toast.makeText(
                            context!!,
                            resources.getString(R.string.amount_too_big),
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {

                        val text = txtTo.text.toString()

                        val amount = BigDecimal.valueOf(txtAmount.text.toString().toDouble())
                        val percent =
                            BigDecimal.valueOf(100).divide(balance, MathContext.DECIMAL128)
                                .multiply(amount).toInt()

                        txtAmount.setText(
                            amount.setScale(10, RoundingMode.HALF_DOWN).toPlainString()
                        )

                        if (text.isEmpty() || !Pattern.matches("(02|03)[0-9a-fA-F]{64}", text)) {

                            txtTo.setText("")
                            Toast.makeText(
                                context!!,
                                resources.getString(R.string.wrong_format),
                                Toast.LENGTH_SHORT
                            ).show()

                        } else if (percent > 100) {

                            Toast.makeText(
                                context!!,
                                resources.getString(R.string.amount_too_big),
                                Toast.LENGTH_SHORT
                            ).show()

                        } else if (txtAmount.text.toString().toDouble() == 0.0) {

                            sbAmountBar.setProgress(percent.toFloat())
                            Toast.makeText(
                                context!!,
                                resources.getString(R.string.zero_amount),
                                Toast.LENGTH_SHORT
                            ).show()

                        } else {

                            val viewDialog =
                                layoutInflater.inflate(R.layout.dialog_transaction_confirm, null)

                            val dialog = AlertDialog.Builder(context!!)
                                .setView(viewDialog)
                                .create()

                            viewDialog.findViewById<TextView>(R.id.lblAmountVal).text =
                                txtAmount.text.toString()
                            viewDialog.findViewById<TextView>(R.id.lblAddressVal).text =
                                txtTo.text.toString()
                            viewDialog.findViewById<TextView>(R.id.btnReject).setOnClickListener {
                                dialog.dismiss()
                            }

                            viewDialog.findViewById<View>(R.id.btnConfirm).setOnClickListener {
                                if (networkAvailable(true)) {
                                    val random =
                                        BigDecimal.valueOf(
                                            Random.nextLong(0, 4294967296).toDouble()
                                        )
                                            .toPlainString() //from 0 to 2^32
                                    val amount =
                                        BigDecimal.valueOf(txtAmount.text.toString().toDouble())
                                            .multiply(BigDecimal.valueOf(10000000000))
                                            .setScale(0, RoundingMode.HALF_DOWN)
                                            .toPlainString()

                                    var amountNew =
                                        AmountValue.convertToApiRawString(BigDecimal(amount))

                                    val txHash = Transaction.constructHash(
                                        amount,
                                        random,
                                        KeyStore.publicKey(context!!),
                                        txtTo.text.toString(),
                                        "",
                                        BuildConfig.TOKEN
                                    )
                                    val request = Transaction.Request(
                                        amount,
                                        KeyStore.publicKey(context!!),
                                        random.toLong(),
                                        TransactionSign.sign(context!!, txHash.toByteArray()),
                                        txtTo.text.toString(),
                                        "",
                                        BuildConfig.TOKEN
                                    )

                                    if (networkAvailable(true)) {
                                        sended = true
                                        viewModel.postTransaction(listOf(request))
                                        dialog.dismiss()
                                    }
                                }
                            }

                            dialog.show()
                        }
                    }
                    send = false
                }
            }
        })

        viewModel.observeTransaction(this, Observer
        {
            if (it != null) {
                if (sended) {
                    if (it.err == 0) {
                        when (it.result[0].status) {
                            0 -> {
                                txtAmount.setText("0.0000000000")
                                sbAmountBar.setProgress(0.0F)
                                AlertDialog.Builder(context!!)
                                    .setTitle(resources.getString(R.string.transaction_result))
                                    .setMessage(resources.getString(R.string.success))
                                    .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                    .create()
                                    .show()
                            }
                            1 -> {
                                showTransactionErrorResult()
                            }
                            2 -> {
                                showTransactionErrorResult()
                            }
                        }
                    } else {
                        showTransactionErrorResult()
                    }
                    viewModel.removeTransactionResult()
                    sended = false
                }
            }
        })
    }

    companion object {
        const val SCAN_REQUEST_CODE = 301
        const val CAMERA_REQUEST_CODE = 302

        const val SCAN_RESULT_EXTRA = "SCAN_RESULT_EXTRA"
    }

}

class AmountTextWatcher(
    val balance: BigDecimal,
    val sbAmountBar: IndicatorSeekBar,
    val txtAmount: EditText
) : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (!s.isNullOrBlank()) {
            try {
                val amount = BigDecimal.valueOf(s.toString().toDouble())
                val percent =
                    BigDecimal.valueOf(100).divide(balance, MathContext.DECIMAL128).multiply(amount)
                        .toInt()
                sbAmountBar.onSeekChangeListener = object : OnSeekChangeListener {
                    override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
                    }

                    override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                    }

                    override fun onSeeking(seekParams: SeekParams?) {
                    }
                }
                sbAmountBar.setProgress(percent.toFloat())
                sbAmountBar.onSeekChangeListener = object : OnSeekChangeListener {
                    override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
                        txtAmount.removeTextChangedListener(this@AmountTextWatcher)
                    }

                    override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                        txtAmount.addTextChangedListener(this@AmountTextWatcher)
                    }

                    override fun onSeeking(seekParams: SeekParams?) {
                        if (balance > BigDecimal.ZERO &&
                            seekParams!!.progress > 0
                        ) {
                            txtAmount.setText(
                                "${
                                    balance.divide(BigDecimal(100))
                                        .multiply(BigDecimal(seekParams.progress)).setScale(
                                            10,
                                            RoundingMode.HALF_DOWN
                                        )
                                }"
                            )
                        } else {
                            txtAmount.setText("0.0000000000")
                        }
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}