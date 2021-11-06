package com.giko.quicksesame.cmd

import java.io.Serializable
import java.io.UnsupportedEncodingException
import java.lang.RuntimeException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Base64
import kotlin.collections.HashMap

class QRCodeInfo(scanStr: String): Serializable {
    private val _textHeader : String = "ssm://UI"

    private var _scanStr: String? = null
    var uuid: String? = null
        get() {
            val part1 = field!!.substring(0, 8)
            val part2 = field!!.substring(8, 12)
            val part3 = field!!.substring(12, 16)
            val part4 = field!!.substring(16, 20)
            val part5 = field!!.substring(20)
            return "$part1-$part2-$part3-$part4-$part5"
        }
    var dataType: String? = null
    var permission: String? = null
    var name: String? = null
    var deviceType: String? = null
    var secretKey: String? = null
    var publicKey: String? = null
    var keyIndex: String? = null

    init {
        if (!scanStr.startsWith(_textHeader)) {
            throw RuntimeException("QR Code string is not valid!")
        }
        this._scanStr = scanStr
        this.initialQRCode()
    }

    private fun initialQRCode() {
        if (_scanStr == null) {
            throw RuntimeException("QR Code string is null!")
        }
        val paramParts: Array<String> = getParamParts(_scanStr!!)
        val paramMap: HashMap<String, String> = getParamMap(paramParts)
        dataType = paramMap["t"]
        permission = paramMap["l"]
        name = paramMap["n"]
        val sk = paramMap["sk"]
        val skBytes: ByteArray = Base64.getDecoder().decode(sk)
        deviceType = skBytes.copyOfRange(0, 1).toHexString()
        secretKey = skBytes.copyOfRange(1, 17).toHexString()
        publicKey = skBytes.copyOfRange(17, 81).toHexString()
        keyIndex = skBytes.copyOfRange(81, 83).toHexString()
        uuid = skBytes.copyOfRange(83, 99).toHexString()
    }

    private fun ByteArray.toHexString() = asUByteArray().joinToString("") {
        it.toString(16).padStart(2, '0')
    }

    private fun getParamMap(paramParts: Array<String>): HashMap<String, String> {
        val paramMap: HashMap<String, String> = HashMap()
        for (paramPart in paramParts) {
            val keyVal = paramPart.split("=").toTypedArray()
            if (keyVal.size != 2) {
                throw RuntimeException("QR code information is not correct!")
            }
            paramMap[keyVal[0]] = keyVal[1]
        }
        return paramMap
    }

    private fun getParamParts(uri: String): Array<String> {
        val uriParts = uri.split("?").toTypedArray()
        if (uriParts.size < 2) {
            throw RuntimeException("QR code uri information is not correct!")
        }
        return try {
            val paramUrl = URLDecoder.decode(uriParts[1], StandardCharsets.UTF_8.name())
            paramUrl.split("&").toTypedArray()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            throw RuntimeException("QR code uri information is not correct!")
        }
    }
}