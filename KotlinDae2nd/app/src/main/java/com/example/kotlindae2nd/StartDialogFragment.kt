package com.example.kotlindae2nd

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class StartDialogFragment(goalLat:Double,goalLong:Double,deviceName:String,context:MapsActivity) : DialogFragment() {
    private val gLat = goalLat
    private val gLong = goalLong
    private val bluetoothName = deviceName
    private val mapContext = context
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage("Do you want to set here as Goal?")
                .setPositiveButton("SET"
                ) { dialog, _ ->
                    // START THE GAME!
                    val inductionConidaeIntent = Intent(mapContext,InductionConidaeFaceActivity::class.java)
                    inductionConidaeIntent.putExtra("GOAL_LAT",gLat)
                    inductionConidaeIntent.putExtra("GOAL_LONG",gLong)
                    inductionConidaeIntent.putExtra("DEVICE_NAME",bluetoothName)
                    startActivity(inductionConidaeIntent)
                    print(dialog)
                }
                .setNegativeButton("CANCEL"
                ) { dialog, _ ->
                    // User cancelled the dialog
                    print(dialog)
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}