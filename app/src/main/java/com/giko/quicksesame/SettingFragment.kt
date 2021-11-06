package com.giko.quicksesame

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.giko.quicksesame.cmd.QRCodeInfo
import com.giko.quicksesame.cmd.SesameCmdExecutor
import com.giko.quicksesame.databinding.FragmentSettingBinding
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*
import kotlin.system.exitProcess

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)

        val apiKey = getAPIKey()
        val info = getQRInfo()
        if (apiKey != null) {
            _binding!!.root.findViewById<EditText>(R.id.sesame_api_key_value).setText(apiKey)
        }
        if (info != null) {
            _binding!!.root.findViewById<TextView>(R.id.sesame_device_value).text = info.name
            _binding!!.root.findViewById<TextView>(R.id.sesame_uuid_value).text = info.uuid
        }
        _binding!!.root.findViewById<Button>(R.id.test_and_save_button).setOnClickListener {
            val inputApiKey = _binding!!.root.findViewById<EditText>(R.id.sesame_api_key_value).text.toString()
            if (inputApiKey.trim().isEmpty()) {
                Toast.makeText(activity?.applicationContext,"请输入API Key后测试", Toast.LENGTH_SHORT).show()
            }
            val savedInfo = getQRInfo()
            if (info == null) {
                Toast.makeText(activity?.applicationContext,"请添加Sesame共享钥匙后测试", Toast.LENGTH_SHORT).show()
            }

            val sesameCmd = SesameCmdExecutor(savedInfo!!, inputApiKey, SesameCmdExecutor.TOGGLE)
            if (sesameCmd.executeCmdSynchronously() == 200) {
                saveAPIKey(inputApiKey)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getAPIKey(): String? {
        return activity?.getPreferences(AppCompatActivity.MODE_PRIVATE)
            ?.getString(getString(R.string.saved_api_key), null)
    }

    private fun saveAPIKey(key: String) {
        with (activity?.getPreferences(Context.MODE_PRIVATE)?.edit()) {
            this?.putString(getString(com.giko.quicksesame.R.string.saved_api_key), key)
            this?.apply()
        }
    }

    private fun getQRInfo(): QRCodeInfo? {
        val info = activity?.getPreferences(Context.MODE_PRIVATE)
            ?.getString(getString(R.string.saved_info),null)
        if (info == null || info.isEmpty()) {
            return null
        }
        val data: ByteArray = Base64.getDecoder().decode(info)
        val ois = ObjectInputStream(
            ByteArrayInputStream(data)
        )
        ois.close()
        return ois.readObject() as QRCodeInfo
    }
}