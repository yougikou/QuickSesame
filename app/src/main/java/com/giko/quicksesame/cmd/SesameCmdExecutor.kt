package com.giko.quicksesame.cmd

import org.bouncycastle.crypto.BlockCipher
import org.bouncycastle.crypto.CipherParameters
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.macs.CMac
import org.bouncycastle.crypto.params.KeyParameter
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Arrays
import java.util.Base64
import java.util.Date
import java.util.logging.Logger

class SesameCmdExecutor(info: QRCodeInfo,
                apiKey: String,
                cmd: String) : Runnable {
    private var _deviceId: String? = null
    private var _apiKey: String? = null
    private var _secretKey: String? = null
    private var _logger: Logger? = null
    private var _cmd: String? = null

    private var _c: ((code: Int)->Unit)? = null
    private var responseCode = 0

    companion object{
        const val LOCK = "82"
        const val UNLOCK = "83"
        const val TOGGLE = "88"
    }

    init {
        _deviceId = info.uuid
        _apiKey = apiKey
        _secretKey = info.secretKey
        _cmd = cmd
        _logger = Logger.getLogger("Sesame API")
    }

    override fun run() {
        val code = executeCmd(_cmd!!)
        _c?.invoke(code)
    }

    public fun executeCmdAsynchronously(c: (code: Int)->Unit) {
        _c = c
        Thread(this).start()
    }

    public fun executeCmdSynchronously(): Int {
        _c = { code -> this.responseCode = code }
        var timeout = 100000
        Thread(this).start()
        return try {
            while (responseCode == 0 && timeout > 0) {
                Thread.sleep(1000)
                timeout -= 1000
            }
            responseCode
        } catch (e: InterruptedException) {
            e.printStackTrace()
            responseCode
        } finally {
            _c = null
            responseCode = 0
        }
    }

    private fun executeCmd(cmdStr: String): Int {
        val base64History = Base64.getEncoder().encodeToString("NFC Unlock".toByteArray())
        val sign = generateRandomTag()
        val json = String.format(
            "{\"cmd\": \"%s\", \"history\": \"%s\",\"sign\": \"%s\"}",
            cmdStr,
            base64History,
            sign
        )
        return try {
            val url =
                URL("https://app.candyhouse.co/api/sesame2/" + _deviceId.toString() + "/cmd")
            val con: HttpURLConnection = url.openConnection() as HttpURLConnection
            con.requestMethod = "POST"
            con.setRequestProperty("Content-Type", "application/json; utf-8")
            con.setRequestProperty("x-api-key", _apiKey)
            con.doOutput = true
            val os: OutputStream = con.outputStream
            val input = json.toByteArray(charset("utf-8"))
            os.write(input, 0, input.size)
            os.close()
            val br = BufferedReader(
                InputStreamReader(con.inputStream, "utf-8")
            )
            val response = StringBuilder()
            var responseLine: String? = null
            while (br.readLine().also { responseLine = it } != null) {
                response.append(responseLine!!.trim { it <= ' ' })
            }
            println(response.toString())
            br.close()
            val code: Int = con.responseCode
            con.disconnect()
            code
        } catch (e: IOException) {
            e.printStackTrace()
            400
        }
    }

    fun generateRandomTag(): String {
        // 1. timestamp  (SECONDS SINCE JAN 01 1970. (UTC))  // 1621854456905
        val timestamp: Long = Date().getTime() / 1000
        // 2. timestamp to uint32  (little endian)   //f888ab60
        val buffer: ByteBuffer = ByteBuffer.allocate(8)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putLong(timestamp)
        // 3. remove most-significant byte    //0x88ab60
        val message: ByteArray = Arrays.copyOfRange(buffer.array(), 1, 4)
        return getCMAC(parseHexStr2Byte(_secretKey!!)!!, message).replace(" ", "")
    }

    private fun parseHexStr2Byte(hexStr: String): ByteArray? {
        if (hexStr.isEmpty()) return null
        val result = ByteArray(hexStr.length / 2)
        for (i in 0 until hexStr.length / 2) {
            val high = hexStr.substring(i * 2, i * 2 + 1).toInt(16)
            val low = hexStr.substring(i * 2 + 1, i * 2 + 2).toInt(16)
            result[i] = (high * 16 + low).toByte()
        }
        return result
    }

    private fun getCMAC(secretKey: ByteArray, msg: ByteArray): String {
        val params: CipherParameters = KeyParameter(secretKey)
        val aes: BlockCipher = AESEngine()
        val mac = CMac(aes)
        mac.init(params)
        mac.update(msg, 0, msg.size)
        val out = ByteArray(mac.macSize)
        mac.doFinal(out, 0)
        val s19 = StringBuilder()
        for (b in out) {
            s19.append(String.format("%02X ", b))
        }
        return s19.toString()
    }
}