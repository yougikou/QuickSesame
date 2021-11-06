package com.giko.quicksesame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.giko.quicksesame.databinding.FragmentResponseBinding
import kotlin.system.exitProcess

class ResponseFragment : Fragment(),Runnable {
    private var _binding: FragmentResponseBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResponseBinding.inflate(inflater, container, false)
        Thread(this).start()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun run() {
        Thread.sleep(2000)
        exitProcess(0)
    }
}