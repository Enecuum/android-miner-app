package com.enecuum.wl.vvm.referral

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.enecuum.wl.R
import com.enecuum.wl.utils.*
import com.enecuum.wl.vvm.scanner.SimpleScannerActivity
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.fragment_referral.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.regex.Pattern

class ReferralFragment : BaseFragment() {

    private val viewModel: ReferralViewModel by viewModel()
    private var fromLogin = false
    private var fromMain = false
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (count >= 60 && s != null) {
                validateKey(s.toString())
            }
        }
    }
    private val publicKeyPattern = Pattern.compile("(02|03)[0-9a-fA-F]{64}")
    private val referralKeyPattern = Pattern.compile("(ref_)[0-9a-fA-F]{66}")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_referral, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fromLogin = arguments?.getBoolean("from_login", false) ?: false
        fromMain = arguments?.getBoolean("from_main", false) ?: false
        if (fromLogin) {
            btnBack.hide()
            requireActivity().onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        Prefs.putString(Constants.PUBLIC_KEY, "")
                        Navigation.findNavController(this@ReferralFragment.requireView())
                            .popBackStack()
                    }
                })
        }
        if (fromMain) {
            btnBack.hide()
            requireActivity().onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        Prefs.putBoolean(Constants.REFERRAL_SHOWED_KEY, true)
                        Navigation.findNavController(this@ReferralFragment.requireView())
                            .popBackStack()
                    }
                })
        }
        setSkipButton()

        var refPublicKey = Prefs.getString(Constants.REFERRAL_PUBLIC_KEY, "")

        txtReferral.setText(refPublicKey)

        imgScan.setOnClickListener {
            if (checkSelfPermission(context!!, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE
                )
            } else {
                startActivityForResult(
                    Intent(context, SimpleScannerActivity::class.java),
                    SCAN_REQUEST_CODE
                )
            }
        }

        txtReferral.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = txtReferral.text.toString()
                if (text.isNotEmpty()) {
                    validateKey(text)
                } else {
                    setSkipButton()
                }
                hideSoftKeyboard()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        txtReferral.addTextChangedListener(textWatcher)

        btnBack.setOnClickListener {
            activity!!.onBackPressed()
        }

        viewModel.validatedKey.observe(viewLifecycleOwner, Observer {
            vProgress.hide()
            when (it) {
                "" -> Toast.makeText(context, R.string.referral_not_enough, Toast.LENGTH_SHORT)
                    .show()
                "error" -> Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show()
                else -> {
                    txtReferral.removeTextChangedListener(textWatcher)
                    txtReferral.setText(publicToRef(it))
                    setNextButton()
                    txtReferral.addTextChangedListener(textWatcher)
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SCAN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringExtra(SCAN_RESULT_EXTRA) ?: ""
            validateKey(result)
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

    private fun setSkipButton() {
        btnAction.text = resources.getString(R.string.skip)
        lblReferralDescription.text = resources.getString(R.string.referral_skip_message)
        btnAction.setOnClickListener {
            Prefs.putBoolean(Constants.REFERRAL_SHOWED_KEY, true)
            if (fromLogin) {
                Navigation.findNavController(it)
                    .navigate(R.id.action_referralFragment_to_mainFragment)
            } else {
                activity!!.onBackPressed()
            }
        }
    }

    private fun setNextButton() {
        btnAction.text = resources.getString(R.string.next)
        lblReferralDescription.text = resources.getString(R.string.referral_next_message)
        btnAction.setOnClickListener {
            Prefs.putString(Constants.REFERRAL_PUBLIC_KEY, txtReferral.text.toString())
            Prefs.putBoolean(Constants.REFERRAL_SHOWED_KEY, true)
            if (fromLogin) {
                Navigation.findNavController(it)
                    .navigate(R.id.action_referralFragment_to_mainFragment)
            } else {
                activity!!.onBackPressed()
            }
        }
    }

    companion object {
        const val SCAN_REQUEST_CODE = 301
        const val CAMERA_REQUEST_CODE = 302

        const val SCAN_RESULT_EXTRA = "SCAN_RESULT_EXTRA"
        const val FROM_LOGIN_FLOW = "true"
    }

    private fun validateKey(referralKey: String) {
        if (referralKeyPattern.matcher(referralKey).matches()) {
            val publicKey = refToPublic(referralKey)
            if (!publicKeyPattern.matcher(publicKey).matches()) {
                Toast.makeText(
                    context!!,
                    resources.getString(R.string.wrong_format),
                    Toast.LENGTH_SHORT
                ).show()
                setSkipButton()
            } else if (publicKey == Prefs.getString(Constants.PUBLIC_KEY, "")) {
                Toast.makeText(
                    context!!,
                    resources.getString(R.string.referral_same_address),
                    Toast.LENGTH_SHORT
                ).show()
                setSkipButton()
            } else {
                vProgress.show()
                viewModel.validateReferral(publicKey)
            }
        } else {
            Toast.makeText(
                context!!,
                resources.getString(R.string.wrong_format),
                Toast.LENGTH_SHORT
            ).show()
            setSkipButton()
        }
    }
}