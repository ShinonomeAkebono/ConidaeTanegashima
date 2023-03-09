package com.example.kotlindae2nd

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.model.Marker

class SendGoalDialog(markerList:MutableList<Marker>):DialogFragment() {
    private val markers = markerList
    var listSelectedListener:ListSelectedListener? = null
    private var userNames = mutableListOf<String>()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreate(savedInstanceState)
        for(marker in markers){
            userNames.add(marker.title!!)
        }
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("誰のゴールに設定しますか？")
            .setItems(userNames.toTypedArray()
            ) { _, i -> listSelectedListener?.onListSelected(userNames[i]) }
        return builder.create()
    }
    interface ListSelectedListener {
        fun onListSelected(userName: String) {}
    }
}