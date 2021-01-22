package com.enecuum.wl.vvm.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.enecuum.wl.BuildConfig
import com.enecuum.wl.R
import com.enecuum.wl.utils.BaseFragment
import com.enecuum.wl.vvm.host_main.MainActivity
import kotlinx.android.synthetic.main.fragment_transactions.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.koin.android.viewmodel.ext.android.getViewModel
import kotlin.coroutines.CoroutineContext


class TransactionsFragment : BaseFragment(), CoroutineScope {

    private val viewModel: TransactionsViewModel by lazy {
        requireActivity().getViewModel<TransactionsViewModel>()
    }

    private lateinit var parentJob: Job
    private val coroutineScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.Main + parentJob)

    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.IO

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        rvTransactions.layoutManager = LinearLayoutManager(context)

        viewModel.observeTransactions(this, Observer { it ->

            if (it.records !== null) {

                val tokenRecords = it.records.filter {
                    it.token_hash == BuildConfig.TOKEN
                }

                rvTransactions.adapter = TransactionsAdapter(tokenRecords, viewModel)

            } else
                rvTransactions.adapter = TransactionsAdapter(emptyList(), viewModel)
        })
    }

    override fun onResume() {
        super.onResume()
        activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.VISIBLE
        (activity!! as MainActivity).selectedMenuId = R.id.bottomMenuTransactions
    }
}