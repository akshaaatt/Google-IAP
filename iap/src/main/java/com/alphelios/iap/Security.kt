package com.alphelios.iap

import android.util.Base64
import java.io.IOException
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec

/**
 * Used to perform purchase signature verification process using RSA.
 * This is done to ensure more security.
 */
object Security {
    private fun generatePublicKey(encodedPublicKey: String): PublicKey {
        try {
            return KeyFactory.getInstance("RSA")
                .generatePublic(X509EncodedKeySpec(Base64.decode(encodedPublicKey, Base64.DEFAULT)))
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidKeySpecException) {
            throw IOException("Invalid key specification: $e")
        }
    }

    private fun verify(publicKey: PublicKey, signedData: String, signature: String): Boolean {
        val signatureBytes: ByteArray
        try {
            signatureBytes = Base64.decode(signature, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            return false
        }
        try {
            val signatureAlgorithm = Signature.getInstance("SHA1withRSA")
            signatureAlgorithm.initVerify(publicKey)
            signatureAlgorithm.update(signedData.toByteArray())
            if (!signatureAlgorithm.verify(signatureBytes))
                return false
            return true
        }
        catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
        catch (e: InvalidKeyException) { }
        catch (e: SignatureException) { }
        return false
    }

    fun verifyPurchase(base64PublicKey: String, signedData: String, signature: String): Boolean {
        if ((signedData.isEmpty() || base64PublicKey.isEmpty() || signature.isEmpty()))
            return false
        return verify(generatePublicKey(base64PublicKey), signedData, signature)
    }
}