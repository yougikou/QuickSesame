package com.giko.quicksesame

import com.giko.quicksesame.cmd.QRCodeInfo
import org.junit.Test


class QRCodeInfoTest {

    private val testQRStr =
        "ssm://UI\\?t=sk&sk=AAAAAAAAAAAA3mEE9cDtT7yJUKLxQ0aAl5zRHRS3qkZ1MsiZY1KLqwhAtnut8i0VUP7amGmbDVYeXC%2FiN0CBOJYBO5i4AGKyoA1AXmCljoh5AACzB9EbRcYwVCtYb2GoxR21&l=2&n=%E5%A4%A7%E9%97%A8"

    @Test
    fun testInit() {
        val info = QRCodeInfo(testQRStr)
        println(info.name)
        println(info.permission)
        println(info.deviceType)
        println(info.secretKey)
        println(info.publicKey)
        println(info.uuid)
    }
}