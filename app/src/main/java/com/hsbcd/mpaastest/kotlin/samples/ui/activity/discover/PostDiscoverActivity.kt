package com.hsbcd.mpaastest.kotlin.samples.ui.activity.discover

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import cn.hsbcsd.mpaastest.R
import cn.hsbcsd.mpaastest.databinding.ActivityPostDiscoverBinding
class PostDiscoverActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDiscoverBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDiscoverBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init(){
        binding.postEditText.addTextChangedListener{
            if (it?.isEmpty() != true){
                binding.ivPost.setImageResource(R.mipmap.bg_post_send_active)
                binding.ivPost.isEnabled = true
            } else {
                binding.ivPost.setImageResource(R.mipmap.bg_send_post)
                binding.ivPost.isEnabled = true
            }
        }

        binding.ivPost.setOnClickListener {
            finish()
        }
    }
}