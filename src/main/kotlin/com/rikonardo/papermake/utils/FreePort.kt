package com.rikonardo.papermake.utils

import java.net.Socket

fun isPortUsed(port: Int): Boolean {
    return try {
        val socket = Socket("127.0.0.1", port)
        socket.close()
        true
    } catch (e: Exception) {
        false
    }
}

fun freePort(initial: Int): Int {
    var port = initial
    while (isPortUsed(port)) {
        port++
    }
    return port
}
