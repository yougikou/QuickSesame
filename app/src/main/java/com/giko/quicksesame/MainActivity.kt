package com.giko.quicksesame

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.giko.quicksesame.cmd.QRCodeInfo
import com.giko.quicksesame.cmd.SesameCmdExecutor
import com.giko.quicksesame.databinding.ActivityMainBinding
import com.huawei.hmf.tasks.TaskCompletionSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val scanKitActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult ->
        if(activityResult.resultCode == Activity.RESULT_OK){
            val result = activityResult.data?.getStringExtra(DefinedActivity.SCAN_RESULT)
            try {
                val info = QRCodeInfo(result!!)
                saveQRInfo(info)
                findViewById<TextView>(R.id.sesame_device_value).text = info.name
                findViewById<TextView>(R.id.sesame_uuid_value).text = info.uuid
            } catch (e: Exception) {
                Toast.makeText(applicationContext,e.message,Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun getAPIKey(): String? {
        return getPreferences(MODE_PRIVATE).getString(getString(R.string.saved_api_key), null)
    }

    private fun getQRInfo(): QRCodeInfo? {
        val info = getPreferences(Context.MODE_PRIVATE).getString(getString(R.string.saved_info),null)
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

    private fun saveQRInfo(info: QRCodeInfo) {
        with (getPreferences(Context.MODE_PRIVATE).edit()) {
            val bos = ByteArrayOutputStream()
            val oos = ObjectOutputStream(bos)
            oos.writeObject(info)
            putString(getString(R.string.saved_info), Base64.getEncoder().encodeToString(bos.toByteArray()))
            apply()
            oos.close()
            bos.close()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.fab.setOnClickListener {
            val intent = Intent(this, DefinedActivity::class.java)
            scanKitActivityLauncher.launch(intent)
        }

        val apiKey = getAPIKey()
        val info = getQRInfo()
        if (intent.action != "android.intent.action.SETTING" && apiKey != null && info != null) {
            if (toggleSesame(apiKey, info)) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, ResponseFragment(), null)
                    .commit()
                return
            } else {
                Toast.makeText(applicationContext,"请求失败",Toast.LENGTH_SHORT).show()
                exitProcess(0)
            }
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, SettingFragment(), null)
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_copy_package_name -> copyPackageNameToClipboard()
            R.id.action_close_app -> exitProcess(0)
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun copyPackageNameToClipboard():Boolean {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("package name", "com.giko.quicksesame")
        clipboard.setPrimaryClip(clip)
        return true
    }

    private fun toggleSesame(apiKey : String, info: QRCodeInfo): Boolean {
        val sesameCmd = SesameCmdExecutor(info, apiKey, SesameCmdExecutor.TOGGLE)
        val result = sesameCmd.executeCmdSynchronously()
        return result == 200
    }
}