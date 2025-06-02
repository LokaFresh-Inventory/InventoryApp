package com.lokatani.lokafreshinventory.utils.tableview

import com.lokatani.lokafreshinventory.data.remote.firebase.ScanResult
import com.lokatani.lokafreshinventory.utils.tableview.model.Cell
import com.lokatani.lokafreshinventory.utils.tableview.model.ColumnHeader
import com.lokatani.lokafreshinventory.utils.tableview.model.RowHeader

class TableViewModel(scanResults: List<ScanResult>) {

    private val mColumnHeaderList: MutableList<ColumnHeader> = ArrayList()
    private val mRowHeaderList: MutableList<RowHeader> = ArrayList()
    private val mCellList: MutableList<MutableList<Cell>> = ArrayList()

    init {
        createColumnHeaderList()
        createCellList(scanResults)
        createRowHeaderList(scanResults.size)
    }

    private fun createColumnHeaderList() {
        mColumnHeaderList.add(ColumnHeader("1", "User"))
        mColumnHeaderList.add(ColumnHeader("2", "Vegetable"))
        mColumnHeaderList.add(ColumnHeader("3", "Weight"))
        mColumnHeaderList.add(ColumnHeader("4", "Date"))
    }

    private fun createCellList(scanResults: List<ScanResult>) {
        for (i in scanResults.indices) {
            val cellList: MutableList<Cell> = ArrayList()
            val result = scanResults[i]

            cellList.add(Cell("1-$i", result.user))
            cellList.add(Cell("2-$i", result.vegResult))
            cellList.add(Cell("3-$i", result.vegWeight))
            cellList.add(Cell("4-$i", result.date))
            mCellList.add(cellList)
        }
    }

    private fun createRowHeaderList(size: Int) {
        for (i in 0 until size) {
            mRowHeaderList.add(RowHeader(i.toString(), (i + 1).toString()))
        }
    }

    fun getColumnHeaderList(): List<ColumnHeader> {
        return mColumnHeaderList
    }

    fun getRowHeaderList(): List<RowHeader> {
        return mRowHeaderList
    }

    fun getCellList(): List<List<Cell>> {
        return mCellList
    }
}