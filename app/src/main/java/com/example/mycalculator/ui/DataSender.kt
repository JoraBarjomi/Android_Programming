package com.example.mycalculator.ui

import android.os.Handler
import android.os.Looper
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mycalculator.R
import kotlinx.coroutines.Runnable
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

class DataSender : AppCompatActivity() {

    private lateinit var btnSend: Button
    private lateinit var tvSockets: EditText
    private lateinit var tvSocketsCounter: TextView
    private lateinit var clientBoxMessage: EditText
    private var textString: String = ""
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_sender)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btnSend = findViewById(R.id.btnSend)
        tvSockets = findViewById(R.id.tvSockets)
        tvSocketsCounter = findViewById(R.id.tvSocketsCounter)
        clientBoxMessage = findViewById(R.id.editTextText)
        handler = Handler(Looper.getMainLooper())
    }

//    fun startServer(){
//        val context = ZMQ.context(1)
//        val socket = ZContext().createSocket(SocketType.REP)
//        socket.bind("tcp://*:12345")
//        var counter: Int = 0
//
//        while(true){
//            counter++
//            val requestBytes = socket.recv(0)
//            val request = String(requestBytes, ZMQ.CHARSET)
//            println("[SERVER] Received request: [$request]")
//
//            handler.postDelayed({
//                tvSocketsCounter.text = "Received MSG from Client = $counter"
//                tvSockets.setText(request)
//            }, 0)
//            Thread.sleep(2000)
//
//            val response = "Hello from Android ZMQ Server!"
//            socket.send(response.toByteArray(ZMQ.CHARSET), 0)
//            println("[SERVER] Received send: [$response]")
//        }
//        socket.close()
//        context.close()
//    }

    fun startClient(){
        val context = ZMQ.context(1)
        val socket = ZContext().createSocket(SocketType.REQ)
        socket.connect("tcp://192.168.0.130:12345")
//        socket.connect("tcp://localhost:12345")
//        socket.connect("tcp://2.59.161.68:12345")

        val request = clientBoxMessage.text.toString()

        socket.send(request.toByteArray(ZMQ.CHARSET), 0)
        val reply = socket.recv(0)
        val replyText = String(reply, ZMQ.CHARSET)

        handler.postDelayed({
            tvSockets.setText(replyText)
        }, 0)

        socket.close()
        context.close()
    }

    override fun onResume() {
        super.onResume()
//        val runnableServer = Runnable{startServer()}
//        val threadServer = Thread(runnableServer)
//        threadServer.start()

        val runnableClient = Runnable{startClient()}
        val threadClient = Thread(runnableClient)
        threadClient.start()

        btnSend.setOnClickListener {
            Thread { startClient() }.start()
        }
    }

}