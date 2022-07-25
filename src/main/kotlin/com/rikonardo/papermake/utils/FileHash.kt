package com.rikonardo.papermake.utils

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import kotlin.experimental.and

fun getFileSHA256(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val fis = FileInputStream(file)
    val data = ByteArray(1024)
    var count = fis.read(data)
    while (count != -1) {
        digest.update(data, 0, count)
        count = fis.read(data)
    }
    fis.close()
    val sha256 = digest.digest()
    return sha256.toHexString()
}

private fun ByteArray.toHexString(): String {
    val sb = StringBuilder()
    for (b in this) {
        sb.append(String.format("%02x", b and 0xff.toByte()))
    }
    return sb.toString()
}
