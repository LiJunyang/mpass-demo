package com.hsbcd.mpaastest.kotlin.samples.ui.activity.insights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.hsbcsd.mpaastest.databinding.FragmentInsightsBinding
import com.mpaas.nebula.adapter.api.MPNebula

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
            MPNebula.startApp("2222222222222222")
        }
        binding.ivInsightsWatch02.setOnClickListener {
            MPNebula.startApp("3333333333333333")
        }
    }
}
