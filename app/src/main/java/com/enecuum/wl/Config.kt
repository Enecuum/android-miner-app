package com.enecuum.wl

object Config {

    const val GET_BALANCE_TIMEOUT_IN_MILLIS_INACTIVE = 120 * 1000L //120 seconds
    const val GET_BALANCE_TIMEOUT_IN_MILLIS_ACTIVE = 60 * 1000L //60 seconds

    const val PING_TIMEOUT_IN_SECONDS = 5L
    const val CONNECT_TIMEOUT_IN_MINUTES = 5L
    const val READ_TIMEOUT_IN_MINUTES = 5L
    const val WRITE_TIMEOUT_IN_MINUTES = 5L

    const val SOCKET_RECONNECT_TIMEOUT_IN_MILLIS = 5 * 1000L //1 sec
}