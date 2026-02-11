package com.example.mycalculator.utils

import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import android.util.Log

class ClientZMQ(private val addr : String) {

    var con_tag : String = "CONNECTION_TAG"
    val context = ZContext(1)
    lateinit var socket : ZMQ.Socket
    var isConnected = false

    fun SetConnection(): Boolean {
        return try {
            Log.d(con_tag, "Connecting to: $addr")
            socket = context.createSocket(SocketType.REQ)
            socket.connect(addr)
            isConnected = true
            Log.d(con_tag, "Connction success: $isConnected")
            true
        } catch (e : Exception) {
            Log.d(con_tag, "Сonnction failed: $isConnected")
            isConnected = false
            return false
        }
    }

    fun SendData(data: String): String? {

        if (!isConnected) return null

        socket.send(data.toByteArray(ZMQ.CHARSET))
        val reply = socket.recv(0)
        return String(reply, ZMQ.CHARSET)

    }

    fun CloseConnection() {

        if (::socket.isInitialized) socket.close()
        context.close()
        isConnected = false

    }
}