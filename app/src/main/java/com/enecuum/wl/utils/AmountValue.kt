package com.enecuum.wl.utils

import com.enecuum.wl.extensions.unifyDelimiter
import com.pixplicity.easyprefs.library.Prefs
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

object AmountValue {
    val cachedAvailableBalance: BigDecimal
        get() = convertFromApiRaw(cachedRawAvailableBalance)

    val cachedRawAvailableBalance: BigDecimal
        get() {
            val balance = Prefs.getString(Constants.BALANCE_KEY, null)?.unifyDelimiter()
                ?: return BigDecimal.ZERO
            return BigDecimal(balance)
        }

    val minValue: BigDecimal
        get() = BigDecimal.valueOf(CONVERSION_FROM_API_VALUE)

    val cachedSumBalance: BigDecimal
        get() = convertFromApiRaw(cachedRawSumBalance)

    private val cachedRawSumBalance: BigDecimal
        get() {
            val balance = Prefs.getString(Constants.SUM_BALANCE_KEY, null)?.unifyDelimiter()
                ?: return BigDecimal.ZERO
            return BigDecimal(balance)
        }

    private val decimalFormatter: DecimalFormat by lazy {
        val decimalFormat = DecimalFormat()
        val formatSymbols = DecimalFormatSymbols()
        formatSymbols.decimalSeparator = '.'
        formatSymbols.groupingSeparator = ','
        decimalFormat.decimalFormatSymbols = formatSymbols
        decimalFormat.maximumFractionDigits = DECIMALS_COUNT
        decimalFormat.minimumFractionDigits = DECIMALS_COUNT_MIN
        return@lazy decimalFormat
    }

    private val percentFormatter: DecimalFormat by lazy {
        val decimalFormat = DecimalFormat()
        val formatSymbols = DecimalFormatSymbols()
        formatSymbols.decimalSeparator = '.'
        decimalFormat.decimalFormatSymbols = formatSymbols
        decimalFormat.maximumFractionDigits = PERCENT_DECIMALS_COUNT
        decimalFormat.minimumFractionDigits = DECIMALS_COUNT_MIN
        return@lazy decimalFormat
    }

    fun cacheAvailableBalance(newValue: String) = Prefs.putString(Constants.BALANCE_KEY, newValue)

    fun cacheSumBalance(newValue: String) = Prefs.putString(Constants.SUM_BALANCE_KEY, newValue)

    fun convertFromApiRaw(value: String?): BigDecimal {
        value?.let {
            return try {
                convertFromApiRaw(BigDecimal(value))
            } catch (e: NumberFormatException) {
                BigDecimal.ZERO
            }
        } ?: return BigDecimal.ZERO
    }

    fun convertFromApiRaw(value: BigDecimal?, scale: Int = 10): BigDecimal {
        val ensureValue = value ?: BigDecimal.ZERO
        return ensureValue.multiply(BigDecimal.valueOf(CONVERSION_FROM_API_VALUE))
            .setScale(scale, RoundingMode.HALF_DOWN)
    }

    fun convertFromApiRawToInt(value: BigDecimal?, scale: Int = 10): Int =
        convertFromApiRaw(value, scale).toInt()

    fun formatFromApiRaw(value: String?): String {
        value?.let {
            return try {
                formatFromApiRaw(BigDecimal(value))
            } catch (e: NumberFormatException) {
                EMPTY_VALUE_STUB
            }
        } ?: return EMPTY_VALUE_STUB
    }

    fun formatFromApiRaw(value: BigDecimal?, scale: Int = 10): String {
        val convertedValue = convertFromApiRaw(value, scale)
        return format(convertedValue)
    }

    fun convertToApiDecimal(decimal: BigDecimal): BigDecimal = decimal
        .multiply(BigDecimal.valueOf(CONVERSION_TO_API_VALUE))
        .setScale(0, RoundingMode.HALF_DOWN)

    fun convertToApiRawString(decimal: BigDecimal): String =
        convertToApiDecimal(decimal).toPlainString()

    fun format(value: BigDecimal): String = decimalFormatter.format(value.toDouble())
    fun formatPercent(value: BigDecimal): String = formatPercent(value.toDouble())
    fun formatPercent(value: Double): String = percentFormatter.format(value)

    const val UNKNOWN_VALUE = "..."
    const val EMPTY_VALUE_STUB = "0.0"

    private const val CONVERSION_FROM_API_VALUE = 0.0000000001
    private const val CONVERSION_TO_API_VALUE = 10000000000

    private const val DECIMALS_COUNT = 10
    private const val DECIMALS_COUNT_MIN = 0
    private const val PERCENT_DECIMALS_COUNT = 2
}