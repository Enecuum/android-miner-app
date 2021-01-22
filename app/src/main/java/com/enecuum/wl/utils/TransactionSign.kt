package com.enecuum.wl.utils

import android.content.Context
import android.util.Log
import com.enecuum.lib.KeyStore
import com.enecuum.lib.SageSign
import org.bouncycastle.util.encoders.Hex
import java.security.Provider
import java.security.Security
import java.security.Signature

object TransactionSign {

    fun sign(context: Context, message: ByteArray): String {
        // Постановка подписи
        Security.insertProviderAt(
            org.spongycastle.jce.provider.BouncyCastleProvider() as Provider?,
            1
        )

        val privateKey = SageSign.convertSecret(KeyStore.secretKey(context))

        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(privateKey)
        signature.update(message)

        // Подпись
        val signed = signature.sign()
        val signedHex = Hex.toHexString(signed)

        Log.d("TransactionSign", "Sign: $signedHex")
        return signedHex

    }

    fun testSign(message: ByteArray): String {
        // Постановка подписи

        Security.insertProviderAt(
            org.spongycastle.jce.provider.BouncyCastleProvider() as Provider?,
            1
        )

        val privateKeyData = "46836604b6fd4409567773e099717412e2e3ad0e273503eb284bb1a4ec66f779"
        val privateKey = SageSign.convertSecret(privateKeyData)

        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(privateKey)
        signature.update(message)

        // Подпись
        val signed = signature.sign()
        val signedHex = Hex.toHexString(signed)

        println("Sign: $signedHex")
        return signedHex

    }

}