package com.hsbcd.mpaastest.kotlin.samples.ui.activity.discover

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import cn.hsbcsd.mpaastest.R
import cn.hsbcsd.mpaastest.databinding.ActivityPostDiscoverBinding
import com.hsbcd.mpaastest.kotlin.samples.util.GlideEngine
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener


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
                binding.ivPost.isEnabled = false
            }
        }

        binding.ivPost.setOnClickListener {
            finish()
        }

        binding.ivPostDiscoverBottom.setOnClickListener {

            PictureSelector.create(this)
                .openGallery(SelectMimeType.ofImage())
                .setImageEngine(GlideEngine.createGlideEngine())
                .forResult(object : OnResultCallbackListener<LocalMedia?> {
                    override fun onResult(result: ArrayList<LocalMedia?>?) {
                        binding.postImage.setImageIcon(Icon.createWithFilePath(result?.get(0)?.availablePath
                            ?: null))
                    }
                    override fun onCancel() {}
                })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }
}