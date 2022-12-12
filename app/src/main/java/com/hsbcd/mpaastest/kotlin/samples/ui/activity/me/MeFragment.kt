/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.me

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.hsbcsd.mpaastest.databinding.FragmentMeBinding

/**
 * 我的信息tab页
 *
 * @author liyalong
 * @version MessageFragment.java, v 0.1 2022年07月29日 15:53 liyalong
 */
class MeFragment : Fragment() {
    private lateinit var binding: FragmentMeBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMeBinding.inflate(inflater, container, false)
        return binding.root
    }
}