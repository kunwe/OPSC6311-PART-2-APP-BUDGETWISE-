package com.example.budgetwise.util

import java.security.MessageDigest
import java.security.SecureRandom

object HashUtils {
    private const val SALT_LENGTH = 16

    fun hashPassword(password: String, salt: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        val hashedBytes = md.digest(password.toByteArray(Charsets.UTF_8))
        return bytesToHex(hashedBytes)
    }

    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt
    }

    // Store as "salt:hash"
    fun saltedHash(password: String): String {
        val salt = generateSalt()
        val hash = hashPassword(password, salt)
        return "${bytesToHex(salt)}:$hash"
    }

    fun verifyPassword(password: String, storedValue: String): Boolean {
        val parts = storedValue.split(":")
        if (parts.size != 2) return false
        val salt = hexToBytes(parts[0])
        val hash = hashPassword(password, salt)
        return hash == parts[1]
    }

    private fun bytesToHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }

    private fun hexToBytes(hex: String): ByteArray =
        hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}