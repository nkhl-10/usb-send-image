package com.app.usbsendframe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.usbsendframe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sender.setOnClickListener {
                startActivity(Intent(this,Sender::class.java))
        }

        binding.receiver.setOnClickListener {
            startActivity(Intent(this,Receiver::class.java))
        }
    }
}