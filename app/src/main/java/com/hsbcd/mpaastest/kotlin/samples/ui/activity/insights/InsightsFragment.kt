package com.hsbcd.mpaastest.kotlin.samples.ui.activity.insights

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.hsbcsd.mpaastest.databinding.FragmentInsightsBinding

class InsightsFragment : Fragment() {
    private lateinit var binding: FragmentInsightsBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isClickable = true
        initViewAndData(view, savedInstanceState)
    }

    private fun initViewAndData(view: View, savedInstanceState: Bundle?) {
        binding.ivInsightsWatch01.setOnClickListener {
            startActivity(Intent(requireActivity(), PlayerActivity::class.java).putExtra("number", "1"))
        }
        binding.ivInsightsWatch02.setOnClickListener {
            startActivity(Intent(requireActivity(), PlayerActivity::class.java).putExtra("number", "2"))
        }
    }
}
