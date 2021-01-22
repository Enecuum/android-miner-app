package com.enecuum.wl.vvm.roi

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.enecuum.wl.R
import com.enecuum.wl.utils.BaseFragment
import com.enecuum.wl.utils.Constants
import com.enecuum.wl.utils.Constants.BALANCE_KEY
import com.enecuum.wl.vvm.host_main.MainActivity
import com.enecuum.lib.api.Roi
import com.pixplicity.easyprefs.library.Prefs
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import kotlinx.android.synthetic.main.fragment_roi.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.math.BigDecimal
import java.math.RoundingMode

class RoiFragment : BaseFragment(), Observer<List<Roi>> {
    private val viewModel: RoiViewModel by viewModel()
    private val roiList = arrayListOf<Roi>()
    private val stakeTextWatcher = StakeTextWatcher()
    private var step = BigDecimal.ONE
    private var minStake = BigDecimal.ZERO
    private var maxStake = BigDecimal.ZERO
    private val roiCoefficient: BigDecimal
        get() = BigDecimal(if (referralCheckBox.isChecked) 1.05 else 1.0)

    private val onSeekChangeListener = object : OnSeekChangeListener {
        override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
            txtStake.removeTextChangedListener(stakeTextWatcher)
        }

        override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
            txtStake.addTextChangedListener(stakeTextWatcher)

            val current = BigDecimal(sbStake.progress)
            txtStake.setText(current.toPlainString())
            txtStake.setSelection(txtStake.text.length)
        }

        override fun onSeeking(seekParams: SeekParams) {
            val current = BigDecimal(seekParams.progress)
            txtStake.setText(current.toPlainString())
            txtStake.setSelection(txtStake.text.length)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_roi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sbStake.setDecimalScale(10)
        btnMenu.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_roiFragment_to_settingsFragment)
            activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.GONE
        }

        btnBuy.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_roiFragment_to_buyFragment)
            activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.GONE
        }

        viewModel.observeRoi(this, this)
        viewModel.observeBalance(this, Observer {
            if (it != null) {
                when {
                    BigDecimal(it.amount) != BigDecimal(0.0) -> {
                        Prefs.putString(Constants.BALANCE_KEY, it.amount.toString())
                        val balance = BigDecimal(Prefs.getString(Constants.BALANCE_KEY, ""))
                            .multiply(BigDecimal.valueOf(0.0000000001))
                            .setScale(10, RoundingMode.HALF_DOWN)
                        lblBalanceVal.text = balance.toPlainString()
                    }
                    Prefs.getString(Constants.BALANCE_KEY, "").isNotEmpty() -> {
                        val balance = BigDecimal(Prefs.getString(Constants.BALANCE_KEY, ""))
                            .multiply(BigDecimal.valueOf(0.0000000001))
                            .setScale(10, RoundingMode.HALF_DOWN)
                        lblBalanceVal.text = balance.toPlainString()
                    }
                    else -> lblBalanceVal.text = "..."
                }
            }
        })
        txtStake.addTextChangedListener(stakeTextWatcher)
        sbStake.onSeekChangeListener = onSeekChangeListener
        txtStake.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (txtStake.text.toString()
                        .isEmpty() || sbStake.progressFloat == sbStake.min || sbStake.progressFloat == sbStake.max
                ) {
                    txtStake.setText(BigDecimal(sbStake.progress).toPlainString())
                }
                hideSoftKeyboard()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        vCard.setOnClickListener {
            val balance = BigDecimal(Prefs.getString(Constants.BALANCE_KEY, ""))
                .multiply(BigDecimal.valueOf(0.0000000001))
                .setScale(0, RoundingMode.HALF_DOWN)
            when {
                balance <= minStake -> txtStake.setText(minStake.toPlainString())
                balance >= maxStake -> txtStake.setText(maxStake.toPlainString())
                else -> txtStake.setText(balance.toPlainString())
            }
        }

        referralCheckBox.setOnCheckedChangeListener { _, _ ->
            val current = BigDecimal(sbStake.progress)
            txtStake.setText(current.toPlainString())
            updateRoi(current)
            txtStake.setSelection(txtStake.text.length)
        }

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
        (activity!! as MainActivity).selectedMenuId = R.id.bottomMenuRoi
    }

    override fun onChanged(updatedRoiList: List<Roi>?) {
        if (updatedRoiList == null || updatedRoiList.isEmpty()) {
            Log.d("RoiFragment", "roi list is null or empty")
            return
        }
        roiList.clear()
        updatedRoiList.forEach {
            roiList.add(
                Roi(
                    it.stake.multiply(BigDecimal.valueOf(0.0000000001))
                        .setScale(10, RoundingMode.HALF_DOWN),
                    it.roi.multiply(BigDecimal.valueOf(0.0000000001))
                        .setScale(10, RoundingMode.HALF_DOWN)
                )
            )
        }
        var maxRoi = Roi(BigDecimal.ZERO, BigDecimal.ZERO)
        var maxPercent = BigDecimal.ZERO
        roiList.forEach {
            val percent = (it.roi - it.stake) / it.stake * BigDecimal(100)
            if (percent > maxPercent) {
                maxPercent = percent
                maxRoi = it
            }
        }
        lblMaxRoi.text = resources.getString(
            R.string.max_roi,
            maxPercent.setScale(2, RoundingMode.HALF_DOWN).toPlainString(),
            maxRoi.stake.setScale(0, RoundingMode.HALF_DOWN).toPlainString()
        )
        lblMaxRoi.setOnClickListener {
            txtStake.setText(maxRoi.stake.setScale(0, RoundingMode.HALF_DOWN).toPlainString())
        }
        minStake = roiList.first().stake.setScale(0, RoundingMode.HALF_DOWN)
        maxStake = roiList.last().stake.setScale(0, RoundingMode.HALF_DOWN)
        step = maxStake.minus(minStake).divide(BigDecimal(100))
        lblMin.text = resources.getString(R.string.min_value, minStake.toPlainString())
        lblMax.text = resources.getString(R.string.max_value, maxStake.toPlainString())
        txtStake.setText(minStake.toPlainString())
        txtStake.setSelection(txtStake.text.length)

        sbStake.min = minStake.toFloat()
        sbStake.max = maxStake.toFloat()
    }

    private fun updateRoi(stake: BigDecimal) {
        if (roiList.isEmpty()) {
            Log.d("RoiFragment", "roi list is empty")
            return
        }

        var selectedStep: Roi = roiList[0]

        for (roiStep in roiList) {
            if (stake > roiStep.stake)
                selectedStep = roiStep
        }

        var roiPercent = BigDecimal(0)

        var percentToNextStep: BigDecimal = BigDecimal(0)
        for (index in roiList.indices) {

            if (roiList[index].stake.compareTo(selectedStep.stake) == 0 && stake.compareTo(roiList[index].stake) != 0) {

                percentToNextStep =
                    (stake - roiList[index].stake) / (roiList[index + 1].stake - roiList[index].stake)
                roiPercent = (roiList[index + 1].roi - roiList[index].roi) * percentToNextStep
            }
        }

        val dailyProfit = selectedStep.roi + (roiPercent) - stake
        val weeklyProfit = dailyProfit * BigDecimal(WEEK_IN_DAYS)
        val monthlyProfit = dailyProfit * BigDecimal(MONTH_IN_DAYS)
        val annualProfit = dailyProfit * BigDecimal(YEAR_IN_DAYS)

        lblRoiDailyPercent.text = formPercentDescription(dailyProfit, stake)
        lblRoiDailyValue.text = formEnqDescription(dailyProfit)

        lblRoiWeeklyPercent.text = formPercentDescription(weeklyProfit, stake)
        lblRoiWeeklyValue.text = formEnqDescription(weeklyProfit)

        lblRoiMonthlyPercent.text = formPercentDescription(monthlyProfit, stake)
        lblRoiMonthlyValue.text = formEnqDescription(monthlyProfit)

        lblRoiAnnualPercent.text = formPercentDescription(annualProfit, stake)
        lblRoiAnnualValue.text = formEnqDescription(annualProfit)
    }

    private fun formPercentDescription(value: BigDecimal, stake: BigDecimal): String {
        return resources.getString(
            R.string.percent_value,
            (value / stake * BigDecimal(100) * roiCoefficient).setScale(2, RoundingMode.HALF_DOWN)
                .toPlainString()
        )
    }

    private fun formEnqDescription(value: BigDecimal): String {
        return resources.getString(
            R.string.enq_value,
            (value * roiCoefficient).setScale(2, RoundingMode.HALF_DOWN).toPlainString()
        )
    }

    inner class StakeTextWatcher : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (!s.isNullOrBlank()) {
                sbStake.onSeekChangeListener = null
                txtStake.removeTextChangedListener(stakeTextWatcher)
                var amount = BigDecimal(s.toString())

                if (amount.toFloat() < sbStake.min) {
                    amount = BigDecimal(sbStake.min.toInt())
                } else if (amount.toFloat() > sbStake.max) {
                    amount = BigDecimal(sbStake.max.toInt())
                }
                sbStake.setProgress(amount.toFloat())
                updateRoi(amount)
                sbStake.onSeekChangeListener = onSeekChangeListener
                txtStake.addTextChangedListener(stakeTextWatcher)
            }
        }
    }

    companion object {
        const val WEEK_IN_DAYS = 7
        const val MONTH_IN_DAYS = 30
        const val YEAR_IN_DAYS = 365
    }
}