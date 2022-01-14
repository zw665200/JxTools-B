package com.wx.tools.controller

import com.wx.tools.utils.AudioTracker
import com.wx.tools.utils.FileUtil
import com.wx.tools.utils.JLog
import com.zly.media.silk.SilkDecoder
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object AudioManager {
    private var audioTracker = AudioTracker()
    private var callback:AudioTracker.AudioPlayListener?=null
    private const val DEFAULT_TATE = 16000


    fun play(from: String, callback: AudioTracker.AudioPlayListener) {
        JLog.i("status = ${audioTracker.status}")

        this.callback = callback

        when (audioTracker.status) {
            AudioTracker.Status.STATUS_NO_READY,AudioTracker.Status.STATUS_READY -> playAmr(from, callback)
            AudioTracker.Status.STATUS_START -> audioTracker.stop()
            AudioTracker.Status.STATUS_STOP -> audioTracker.start()
        }

    }

    fun pause() {
        if (audioTracker.status == AudioTracker.Status.STATUS_START) {
            audioTracker.pause()
        }
    }

    private fun playAmr(from: String, callback: AudioTracker.AudioPlayListener) {
        val trans = transformStandardSLK(from)
        if (trans.isNullOrEmpty()) {
            return
        }

        val result = decodeAmr(from, trans)
        if (result.isNullOrEmpty()) {
            return
        }

        //转化成功删除临时文件
        FileUtil.deleteFile(trans)

        JLog.i("result = $result")
        audioTracker.createAudioTrack(result, callback)

    }

    private fun playOther(from: String, callback: AudioTracker.AudioPlayListener) {
        audioTracker.createAudioTrack(from, object : AudioTracker.AudioPlayListener {
            override fun onStart() {
                JLog.i("onStart")
            }

            override fun onStop() {
                JLog.i("onStop")
                callback.onStop()
            }

            override fun onError(message: String) {
                JLog.i("onError")
            }
        })

    }


    /**
     * 返回解码后的语音文件
     */
    fun getTransformedAudio(from: String): String? {
        val trans = transformStandardSLK(from)
        if (trans.isNullOrEmpty()) {
            return null
        }

        val result = decodeAmr(from, trans)
        if (result.isNullOrEmpty()) {
            return null
        }

        FileUtil.deleteFile(trans)

        return result
    }

    fun release() {
        JLog.i("release audio status = ${audioTracker.status}")
        when (audioTracker.status) {
            AudioTracker.Status.STATUS_NO_READY -> {
            }

            //预备
            AudioTracker.Status.STATUS_READY -> {
                audioTracker.setAudioPlayListener(null)
                audioTracker.release()
            }

            //播放
            AudioTracker.Status.STATUS_START -> {
                audioTracker.pause()
                audioTracker.stop()
                audioTracker.setAudioPlayListener(null)
                return
            }

            //暂停中
            AudioTracker.Status.STATUS_PAUSE -> {
                audioTracker.stop()
                audioTracker.setAudioPlayListener(null)
                return
            }

            //停止
            AudioTracker.Status.STATUS_STOP -> {
                audioTracker.setAudioPlayListener(null)
                audioTracker.release()
            }

            else -> {
                audioTracker.setAudioPlayListener(null)
            }
        }

    }


    /**
     * 将不标准的微信amr文件转化成标准的SLK文件
     */
    private fun transformStandardSLK(from: String): String? {
        try {
            val file = File(from)
            val out = from.replace(file.name, "temp")
            val fis = FileInputStream(from)
            val bis = ByteArrayInputStream(fis.readBytes())
            val ous = FileOutputStream(out)

            //不要第一个字节
            bis.skip(1)
            val bufferArray = ByteArray(80)
            var read: Int

            //从第二个字节开始读
            do {
                read = bis.read(bufferArray)
                ous.write(bufferArray, 0, bufferArray.size)
            } while (read > 0)

            JLog.i("finish")
            return out

        } catch (ex: Exception) {
            return null
        }
    }

    /**
     * 将标准的SLK音频文件转换成PCM音频文件
     */
    private fun decodeAmr(from: String, trans: String): String? {
        val file = File(from)
        val name = file.name.replace("msg_", "")
        val out = from.replace(file.name, name)
        return SilkDecoder.transcode2PCM(trans, DEFAULT_TATE, out)
    }


}