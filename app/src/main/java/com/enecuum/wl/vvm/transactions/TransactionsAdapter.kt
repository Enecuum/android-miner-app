package com.enecuum.wl.vvm.transactions

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.enecuum.wl.R
import com.enecuum.wl.utils.AmountValue
import com.enecuum.lib.api.TransactionItem
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class TransactionsAdapter(
    private val list: List<TransactionItem>,
    val viewModel: TransactionsViewModel
) : RecyclerView.Adapter<TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TransactionViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction: TransactionItem = list[position]
        holder.bind(transaction, viewModel)
    }

    override fun getItemCount(): Int = list.size
}

class TransactionViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_transaction, parent, false)) {

    private var icon: ImageView? = itemView.findViewById(R.id.txIcon)
    private var amount: TextView? = itemView.findViewById(R.id.txAmount)
    private var ticker: TextView? = itemView.findViewById(R.id.txTicker)
    private var type: TextView? = itemView.findViewById(R.id.txType)
    private var status: TextView? = itemView.findViewById(R.id.txStatus)
    private var time: TextView? = itemView.findViewById(R.id.txTime)

    private var red: Int = Color.argb(255, 107, 51, 52)
    private var green: Int = Color.argb(255, 52, 111, 53)
    private var gray: Int = Color.argb(255, 193, 193, 193)

    fun bind(transaction: TransactionItem, viewModel: TransactionsViewModel) {

        var textAmount = AmountValue.convertFromApiRaw(transaction.amount, 2).toString()

        //Transactions

        var textType = "Unknown"
        if (transaction.rectype == "iout") {
            textAmount = "- $textAmount"
            textType = "Send"
            icon?.setColorFilter(red)
            // delegate, undelegate, claim reward, mint, burn, create token, create pos, transfer
        }

        if (transaction.rectype == "iin") {
            textAmount = "+ $textAmount"
            textType = "Received"
            icon?.setColorFilter(green)
        }

        if (transaction.rectype == "pending") {
            textAmount = "- $textAmount"
            status?.text = "Pending"
            textType = "Send"
            icon?.setColorFilter(gray)
        }

//        Rewards
//        'ik' - PoW
//        'im' - PoA
//        'iref' - Referral / Реферал.
//        'iv' - PoS
//        'ic' - Staking claim / Стейкинг
//        'ifk' - PoW TX fee
//        'ifg' - Genesis TX fee
//        'ifl' - PoS leader fees
//        'idust' - Dust

        if (transaction.rectype == "im") {
            textAmount = "+ $textAmount"
            textType = "PoA"
            icon?.setColorFilter(green)
        }

        if (transaction.rectype == "ik") {
            textAmount = "+ $textAmount"
            textType = "PoW"
            icon?.setColorFilter(green)
        }

        if (transaction.rectype == "iref") {
            textAmount = "+ $textAmount"
            textType = "Referral"
            icon?.setColorFilter(green)
        }

        if (transaction.rectype == "iv") {
            textAmount = "+ $textAmount"
            textType = "PoS"
            icon?.setColorFilter(green)
        }

        if (transaction.rectype == "ic") {
            textAmount = "+ $textAmount"
            textType = "Staking Claim"
            icon?.setColorFilter(green)
        }

        if (transaction.rectype == "ifk") {
            textAmount = "+ $textAmount"
            textType = "PoW TX Fee"
            icon?.setColorFilter(green)
        }

        if (transaction.rectype == "ifg") {
            textAmount = "+ $textAmount"
            textType = "Genesis TX Fee"
            icon?.setColorFilter(green)
        }

        if (transaction.rectype == "ifl") {
            textAmount = "+ $textAmount"
            textType = "PoS Leader Fee"
            icon?.setColorFilter(green)
        }

        if (transaction.rectype == "idust") {
            textAmount = "+ $textAmount"
            textType = "Dust"
            icon?.setColorFilter(green)
        }

        //Status

        if (transaction.status == 3) {
            status?.text = "Confirmed"
        }

        if (transaction.status == 2) {
            status?.text = "Rejected"
            icon?.setColorFilter(gray)
        }

        //Time
        val dateFormat: DateFormat = SimpleDateFormat("HH:mm / dd MMM yyyy")
        val date = Date(transaction.time.toLong() * 1000)
        val strDate: String = dateFormat.format(date)
        time?.text = strDate

        amount?.text = textAmount
        type?.text = textType

        //TODO
        if (transaction.token_hash !== null && transaction.token_hash.isNotEmpty()) {
            ticker?.text = viewModel.context.resources.getString(R.string.ticker)
        }
    }
}