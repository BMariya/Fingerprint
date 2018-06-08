package com.marywaft.fingerprint.extensions

import java.security.MessageDigest

/**
 * Extension for String type.
 * @return Returns String of hex-format bytes hashed by md5.
 */
internal fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digested = md.digest(toByteArray())
    return digested.joinToString(separator = "") { String.format("%02x", it) }
}