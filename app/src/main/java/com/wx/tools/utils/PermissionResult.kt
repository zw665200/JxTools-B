package com.wx.tools.utils

/**
@author ZW
@description:
@date : 2020/11/24 15:46
 */
sealed class PermissionResult {
    object Grant : PermissionResult()
    class Deny(val permissions: Array<String>) : PermissionResult()
    class Rationale(val permissions: Array<String>) : PermissionResult()
}