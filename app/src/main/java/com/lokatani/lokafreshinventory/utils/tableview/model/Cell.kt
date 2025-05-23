package com.lokatani.lokafreshinventory.utils.tableview.model

import com.evrencoskun.tableview.filter.IFilterableModel
import com.evrencoskun.tableview.sort.ISortableModel

open class Cell(
    private val mId: String,
    private val mData: Any?
) : ISortableModel, IFilterableModel {

    override fun getId(): String {
        return mId
    }

    override fun getContent(): Any? {
        return mData
    }

    fun getData(): Any? {
        return mData
    }

    override fun getFilterableKeyword(): String {
        return mData.toString()
    }
}

