package com.mrt.project

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import javax.net.ssl.SSLSocketFactory

class MainActivity : AppCompatActivity() {
    var connection = true
    var disconnection = false

    fun publish(client: MqttClient, topic:String, message:String)
    {
        client.publish(
            topic,
            message.toByteArray(),
            2,  // QoS = 2
            false
        )
    }

    fun subscribe(client:MqttClient,topic:String)
    {

        client.subscribe(topic, 1)

    }

    fun unsubscribe(client:MqttClient,topic:String)
    {

        client.unsubscribe(topic)

    }

    lateinit var client:MqttClient;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("myPref", MODE_PRIVATE)
        val editor = sharedPref.edit()
        var cont = this.applicationContext

        client = MqttClient(//"ssl://"+key.text+":8883"
            "ssl://94467eee6d1f449887465956c3c306c5.s2.eu.hivemq.cloud:8883",  // serverURI in format: "protocol://name:port"
            MqttClient.generateClientId(),  // ClientId
            MemoryPersistence()
        ) // Persistence



        client.setCallback(object : MqttCallback {
            // Called when the client lost the connection to the broker
            override fun connectionLost(cause: Throwable) {
                var tfv = findViewById<TextView>(R.id.connectNotConnect)
                tfv.text = ("client lost connection $cause").toString()
                connection=true
            }

            override fun messageArrived(topic: String, message: MqttMessage) { //gelen mesajlar dinlenecek
                //tfv.text =(topic + ": " + String((message.payload))) //Arrays.toString

                if (topic=="accel_mov")
                {
                    var ert = findViewById<TextView>(R.id.accelmov)
                    //publish(client,"accelmov","IvmeOlcer Hareketi Algılandı")
                    ert.text = "Hareket İvmeÖlçer."

                    //Toast.makeText(this@MainActivity, "IvmeOlcer Hareketi Algilandi.", Toast.LENGTH_SHORT)
                        //.show()
                    //acc_rate=String((message.payload)).toDouble()
                    //Toast.makeText(cont, "hey", Toast.LENGTH_SHORT).show()
                }

                if (topic=="camera_mov")
                {
                    var mrt = findViewById<TextView>(R.id.cameramov)
                    //publish(client,"accelmov","IvmeOlcer Hareketi Algılandı")
                    mrt.text = "Hareket Kamera."
                    //publish(client,"cameramov","Kamera Hareketi Algılandı")
                    //cameramov.setText("Kamera Hareket")

                    //Toast.makeText(this@MainActivity, "Kamera Hareketi Algilandi.", Toast.LENGTH_SHORT)
                       // .show()
                    //acc_rate=String((message.payload)).toDouble()
                }

                /*if (topic=="disconnect")
                {
                    connection=true
                    disconnection=false
                    client.disconnect()
                }*/
                if(topic=="ping")
                {
                    publish(client,"ping_response","HI!")
                }

            }

            // Called when an outgoing publish is complete
            override fun deliveryComplete(token: IMqttDeliveryToken) {
                //tfv.text =("delivery complete $token").toString()
                //tfv.text="connected"
            }
        })

        loadData()
        //loadUserData()

        button.setOnClickListener { //ivme olcer sayısı ve saniye fark sayısını buradan yolla

                if (ivme_olcer_uyari.text.isEmpty() || resim_saniye.text.isEmpty()/*gyro_orani.text.isEmpty()*/) {
                    Toast.makeText(this, "Bosluk olamaz. Bosluklari Doldurun.", Toast.LENGTH_SHORT)
                        .show()
                    /*deger_one.text = "0"
                    deger_two.text = "0"*/
                }

                if (resim_saniye.text.toString() == ("1") || resim_saniye.text.toString() == ("2") || resim_saniye.text.toString() == ("3") || resim_saniye.text.toString() == ("4") || resim_saniye.text.toString() == ("5")) {
                    Toast.makeText(this, "Saniye Fark, 5' ten büyük olmalı", Toast.LENGTH_SHORT)
                        .show()
                }

                else {
                    saveData()
                }

                if (!connection) {

               /*client.connect(mqttConnectOptions)

               subscribe(client, "#")*/
                    val d1 = deger_one.text
                    val d2 = deger_two.text
                    publish(client,"accelrate",d1.toString())
                    publish(client,"camsecs",d2.toString())

            }
        }

        connectButton.setOnClickListener {//mesaj gönderirken buraya yaz

            if (user_name.text.isEmpty() || password_.text.isEmpty() || key.text.isEmpty()) {
                Toast.makeText(this, "Bosluk birakmamanizi oneririz.", Toast.LENGTH_SHORT)
                    .show()
                    connection=false
            }
            else {
                val username = user_name.text.toString()
                val password = password_.text.toString()
                val keyOfUser = key.text.toString()

                editor.apply {
                    putString("username", username)
                    putString("password", password)
                    putString("keyOfUser", keyOfUser)
                    apply()
                }
                connection=true
            }

            /*if(!connection){
                camcontrol.setOnClickListener{
                    if (camcontrol.isChecked) {
                        publish(client, "camcontrol", "1")
                    } else {
                        publish(client, "camcontrol", "0")
                    }
                }

                accelcontrol.setOnClickListener{
                    if (accelcontrol.isChecked) {
                        publish(client, "accelcontrol", "1")
                    } else {
                        publish(client, "accelcontrol", "0")
                    }
                }
            }*/

            val mqttConnectOptions = MqttConnectOptions()
            mqttConnectOptions.userName = user_name.text.toString()
            mqttConnectOptions.password = (password_.text).toString().toCharArray()//(pass.text).toCharArray()"testconnpassword".toCharArray()
            mqttConnectOptions.socketFactory =
                SSLSocketFactory.getDefault() // using the default socket factory

            if (connection)
            {
                connection=false
                disconnection=true
                client.connect(mqttConnectOptions)

                subscribe(client,"#")

                connectNotConnect.setText("Connected")

                publish(client,"test","hello")
                /*publish(client,"accelrate",deger_one.text.toString())
                publish(client,"camsecs",deger_two.text.toString())*/
                //cam_thread.start()
            }

        }

        camcontrol.setOnClickListener {
            if (!connection) {
                if (camcontrol.isChecked) {
                    publish(client, "camcontrol", "1")
                } else {
                    publish(client, "camcontrol", "0")
                }
            }
        }


            accelcontrol.setOnClickListener {
                if (!connection) {
                    if (accelcontrol.isChecked) {
                        publish(client, "accelcontrol", "1")
                    } else {
                        publish(client, "accelcontrol", "0")
                    }
                }
            }


        loadBtn.setOnClickListener {
            val username = sharedPref.getString("username", null)
            val password = sharedPref.getString("password", null)
            val keyOfUser = sharedPref.getString("keyOfUser", null)

            user_name.setText(username)
            password_.setText(password)
            key.setText(keyOfUser)
        }

        disconnectButton.setOnClickListener {
            if (disconnection)//disconnection
            {
                try {
                    client.disconnect()
                    connectNotConnect.setText("Disconnected")
                }
                catch (e: Error) {
                    // handler
                }

                disconnection=false
                connection=true
                //publish(client,"test","hello")
            }
        }
    }

    private fun saveData(){
        val insertedText1 = ivme_olcer_uyari.text.toString().toDouble()
        deger_one.text = insertedText1.toString()

        val insertedText2 = resim_saniye.text.toString().toInt()
        deger_two.text = insertedText2.toString()

        /*val insertedText3 = gyro_orani.text.toString()
        deger_three.text = insertedText3*/

        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply{
            putString("STRING_KEY1", insertedText1.toString())
            putString("STRING_KEY2", insertedText2.toString())

        }.apply()
        Toast.makeText(this, "Veri Kaydedildi.", Toast.LENGTH_SHORT).show()
    }

    private fun loadData(){
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val savedString1 = sharedPreferences.getString("STRING_KEY1", "5")
        val savedString2 = sharedPreferences.getString("STRING_KEY2", "15")

        deger_one.text = savedString1
        deger_two.text = savedString2
    }

}