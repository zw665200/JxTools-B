package com.wx.tools.bean

data class HuaWeiDB(var index: Int, var path: String?, var data: Any?) {

//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as HuaWeiDB
//
//        if (index != other.index) return false
//        if (path != other.path) return false
//        if (data != null) {
//            if (other.data == null) return false
//            if (!data!!.contentEquals(other.data!!)) return false
//        } else if (other.data != null) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = index
//        result = 31 * result + (path?.hashCode() ?: 0)
//        result = 31 * result + (data?.contentHashCode() ?: 0)
//        return result
//    }
}
