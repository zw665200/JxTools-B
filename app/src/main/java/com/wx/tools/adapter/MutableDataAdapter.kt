package com.wx.tools.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MutableDataAdapter<T> private constructor() : RecyclerView.Adapter<MutableDataAdapter<T>.MyViewHolder>() {

    //数据
    private var mDataList: MutableList<T>? = null

    //布局id
    private var mLayoutId1: Int? = null
    private var mLayoutId2: Int? = null


    private var mType = -1
    private var defaultType = 0

    //绑定事件的lambda放发
    private var addBindView: ((itemView: View, itemData: T) -> Unit)? = null

    private var setViewType: ((position: Int) -> Int)? = null


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
        if (mType == defaultType) {
            val view = LayoutInflater.from(p0.context).inflate(mLayoutId1!!, p0, false)
            return MyViewHolder(view)
        } else {
            val view = LayoutInflater.from(p0.context).inflate(mLayoutId2!!, p0, false)
            return MyViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        //左侧为null时返回-1
        return mDataList?.size ?: -1
    }

    override fun getItemViewType(position: Int): Int {
        mType = setViewType?.invoke(position)!!
        return mType
    }

    override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
        addBindView?.invoke(p0.itemView, mDataList?.get(p1)!!)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * 建造者，用来完成adapter的数据组合
     */
    class Builder<B> {

        private var adapter: MutableDataAdapter<B> = MutableDataAdapter()

        /**
         * 设置数据
         */
        fun setData(lists: MutableList<B>): Builder<B> {
            adapter.mDataList = lists
            return this
        }

        /**
         * 设置布局id
         */
        fun setLayoutId(layoutId1: Int, layoutId2: Int): Builder<B> {
            adapter.mLayoutId1 = layoutId1
            adapter.mLayoutId2 = layoutId2
            return this
        }

        /**
         * 设置多布局类型
         */
        fun setViewType(type: (position: Int) -> Int): Builder<B> {
            adapter.setViewType = type
            return this
        }

        /**
         * 绑定View和数据
         */
        fun addBindView(itemBind: ((itemView: View, itemData: B) -> Unit)): Builder<B> {
            adapter.addBindView = itemBind
            return this
        }


        fun create(): MutableDataAdapter<B> {
            return adapter
        }
    }
}