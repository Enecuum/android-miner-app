package com.enecuum.wl.di

import com.enecuum.wl.data.livedata.*
import com.enecuum.wl.vvm.host_main.ActivityViewModel
import com.enecuum.wl.vvm.main.MainViewModel
import com.enecuum.wl.vvm.qr.QRViewModel
import com.enecuum.wl.vvm.referral.ReferralViewModel
import com.enecuum.wl.vvm.roi.RoiViewModel
import com.enecuum.wl.vvm.send_receive.tabs.send.SendViewModel
import com.enecuum.wl.vvm.statistic.StatisticViewModel
import com.enecuum.wl.vvm.transactions.TransactionsViewModel
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val appModule = module {

    single<AmountLiveDataRepository> { AmountLiveData() }
    single<TransactionLiveDataRepository> { TransactionLiveData() }
    single<TransactionsLiveDataRepository> { TransactionsLiveData() }
    single<RewardsLiveDataRepository> { RewardsLiveData() }
    single<TickersLiveDataRepository> { TickersLiveData() }
    single<StatisticLiveDataRepository> { StatisticLiveData() }
    single<RoiLiveDataRepository> { RoiLiveData() }
    single<PriceLiveDataRepository> { PriceLiveData() }

    viewModel { MainViewModel(get(), get(), get(), get()) }
    viewModel { StatisticViewModel(get(), get()) }
    viewModel { TransactionsViewModel(get(), get(), get(), get()) }
    viewModel { SendViewModel(get(), get(), get()) }
    viewModel { ReferralViewModel(get()) }
    viewModel { ActivityViewModel(get()) }
    viewModel { RoiViewModel(get(), get(), get(), get(), get()) }
    viewModel { QRViewModel(get(), get(), get()) }
}