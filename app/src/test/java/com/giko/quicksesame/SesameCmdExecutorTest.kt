package com.giko.quicksesame

import com.giko.quicksesame.cmd.QRCodeInfo
import com.giko.quicksesame.cmd.SesameCmdExecutor
import org.junit.Test


class SesameCmdExecutorTest {
    private val testQRStr =
        "ssm://UI\\?t=sk&sk=ADJdzA6d0y14m%2FP8Xp3mWAWJUKLxQ0aAl5zRHRS3qkZ1MsiZY1KLqwhAtnut8i0VUP7amGmbDVYeXC%2FiN0CBOJYBO5i4AGKyoA1AXmCljoh5AACzB9EbRcYwVCtYb2GoxR21&l=0&n=%E5%A4%A7%E9%97%A8"

    // get from sesame console
    private val apiKey = "1T3rmL1ddL6m0VIVYKJoBayKqhLePmfZ44uNKt2N"

    @Test
    fun testGenerateRandomTag() {
        val info = QRCodeInfo(testQRStr)
        val cmd = SesameCmdExecutor(
            info,
            apiKey,
            SesameCmdExecutor.TOGGLE
        )
        println(cmd.generateRandomTag())
    }

    @Test
    fun testWm2Cmd() {
        val info = QRCodeInfo(testQRStr)
        val cmd = SesameCmdExecutor(
            info,
            apiKey,
            SesameCmdExecutor.TOGGLE
        )
        cmd.executeCmdSynchronously()
    }
    
}