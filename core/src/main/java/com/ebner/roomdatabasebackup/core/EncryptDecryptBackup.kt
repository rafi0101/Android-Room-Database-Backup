package com.ebner.roomdatabasebackup.core

import android.content.SharedPreferences
import android.util.Base64
import java.io.*
import java.nio.file.Path
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * https://medium.com/@oshanm1/how-to-encrypt-and-decrypt-a-file-in-android-kotlin-80c8e9168513#:~:text=We%20can%20use%20the%20Java,case%20to%20encrypt%20the%20file.
 */
class EncryptDecryptBackup {

    private val BACKUP_SECRET_KEY = "backupsecretkey"

    @Throws(Exception::class)
    fun readFile(filePath: Path): ByteArray {
        val file = File(filePath.toUri())
        val fileContents = file.readBytes()
        val inputBuffer = BufferedInputStream(
            FileInputStream(file)
        )
        inputBuffer.read(fileContents)
        inputBuffer.close()
        return fileContents
    }

    @Throws(Exception::class)
    fun saveFile(fileData: ByteArray, path: String) {
        val file = File(path)
        val bos = BufferedOutputStream(FileOutputStream(file, false))
        bos.write(fileData)
        bos.flush()
        bos.close()
    }

    @Throws(Exception::class)
    fun encrypt(yourKey: SecretKey, fileData: ByteArray): ByteArray {
        val data = yourKey.encoded
        val skeySpec = SecretKeySpec(data, 0, data.size, "AES")
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(fileData)
    }

    @Throws(Exception::class)
    fun decrypt(yourKey: SecretKey, fileData: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, yourKey, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(fileData)
    }

    fun getSecretKey(sharedPref: SharedPreferences): SecretKey {

        val key = sharedPref.getString(BACKUP_SECRET_KEY, null)

        if (key == null) {
            //generate secure random
            val secretKey = generateSecretKey()
            saveSecretKey(sharedPref, secretKey!!)
            return secretKey
        }

        val decodedKey = Base64.decode(key, Base64.NO_WRAP)
        val originalKey = SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")

        return originalKey
    }

    private fun saveSecretKey(sharedPref: SharedPreferences, secretKey: SecretKey): String {
        val encodedKey = Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP)
        sharedPref.edit().putString(BACKUP_SECRET_KEY, encodedKey).apply()
        return encodedKey
    }

    @Throws(Exception::class)
    fun generateSecretKey(): SecretKey? {
        val secureRandom = SecureRandom()
        val keyGenerator = KeyGenerator.getInstance("AES")
        //generate a key with secure random
        keyGenerator?.init(128, secureRandom)
        return keyGenerator?.generateKey()
    }

}