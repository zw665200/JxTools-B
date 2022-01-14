package com.wx.tools.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import com.wx.tools.view.base.BaseFragmentActivity
import com.wx.tools.view.fragment.FLive

/**
@author ZW
@description:
@date : 2020/11/24 15:46
 */
class LivePermissions {

    companion object {
        const val TAG = "permissions"
    }

    constructor(activity: BaseFragmentActivity) {
        fLive = getInstance(activity.supportFragmentManager)
    }

    constructor(fragment: Fragment) {
        fLive = getInstance(fragment.childFragmentManager)
    }

    @Volatile
    private var fLive: FLive? = null

    private fun getInstance(fragmentManager: FragmentManager) =
        fLive ?: synchronized(this) {
            fLive ?: if (fragmentManager.findFragmentByTag(TAG) == null) FLive()
                .run {
                    fragmentManager.beginTransaction().add(
                        this,
                        TAG
                    ).commitNow()
                    this
                } else fragmentManager.findFragmentByTag(TAG) as FLive
        }

    fun request(vararg permissions: String): MutableLiveData<PermissionResult> {
        return this.requestArray(permissions)
    }

    private fun requestArray(permissions: Array<out String>): MutableLiveData<PermissionResult> {
        fLive!!.requestPermissions(permissions)
        return fLive!!.liveData
    }

}
