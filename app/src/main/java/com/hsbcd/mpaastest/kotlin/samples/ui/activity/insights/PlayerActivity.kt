package com.hsbcd.mpaastest.kotlin.samples.ui.activity.insights

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.hsbcsd.mpaastest.R
import cn.hsbcsd.mpaastest.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var uri = "android.resource://$packageName/"
        when (intent.getStringExtra("number")) {
            "1" -> {
                uri += R.raw.watch01
            }
            "2" -> {
                uri += R.raw.watch02
            }
        }
        binding.videoView.apply {
            setVideoURI(Uri.parse(uri))
            start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.videoView.suspend()
    }
}
