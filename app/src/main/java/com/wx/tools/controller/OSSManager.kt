package com.wx.tools.controller

import android.content.Context
import android.net.Uri
import com.alibaba.sdk.android.oss.ClientConfiguration
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.wx.tools.bean.OssParam
import com.wx.tools.utils.JLog

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/3/26 14:04
 */
object OSSManager {
    private val clientConfiguration = config()

    private fun config(): ClientConfiguration {
        val conf = ClientConfiguration()
        conf.connectionTimeout = 15 * 1000 // 连接超时，默认15秒
        conf.socketTimeout = 15 * 1000 // socket超时，默认15秒
        conf.maxConcurrentRequest = 5 // 最大并发请求数，默认5个
        conf.maxErrorRetry = 2 // 失败后最大重试次数，默认2次
        return conf
    }

    fun uploadFile(context: Context, stsModel: OssParam, filePath: Uri, success: (url: String) -> Unit, error: (msg: String) -> Unit) {
        val credentialProvider = OSSStsTokenCredentialProvider(
            stsModel.AccessKeyId,
            stsModel.AccessKeySecret,
            stsModel.SecurityToken
        )

//        JLog.i("id = ${stsModel.AccessKeyId}")
//        JLog.i("secret = ${stsModel.AccessKeySecret}")
//        JLog.i("token = ${stsModel.SecurityToken}")

        val name = "complaint/" + System.currentTimeMillis().toString() + ".jpg"
        val oss = OSSClient(context.applicationContext, Constant.END_POINT, credentialProvider, clientConfiguration)
        val put = PutObjectRequest(Constant.BUCKET_NAME, name, filePath)


        //progress
        put.setProgressCallback { request, currentSize, totalSize ->
//            JLog.i("currentSize = $currentSize , totalSize = $totalSize")
        }

        oss.asyncPutObject(put, object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
            override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult?) {
                JLog.i("upload success")
                success("http://${Constant.BUCKET_NAME}.${Constant.END_POINT_WITHOUT_HTTP}/$name")
            }

            override fun onFailure(request: PutObjectRequest?, clientException: ClientException?, serviceException: ServiceException?) {

                JLog.i("upload failed")
                if (clientException != null) {
                    error("网络连接失败")
                    JLog.i("${clientException.message}")
                }

                if (serviceException != null) {
                    error("上传图片失败")
                    JLog.i("${serviceException.message}")
                }
            }
        })
    }
}