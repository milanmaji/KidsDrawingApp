package com.sitapuramargram.kidsdrawingapp

import android.R
import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.sitapuramargram.kidsdrawingapp.databinding.DialogCustomProgressBinding


class CustomProgressDialog(context: Context) : Dialog(context) {

    var message = "Loading"


    init {
        val params = window!!.attributes
        params.gravity = Gravity.CENTER_HORIZONTAL
        window!!.attributes = params
        setTitle(null)
        setCancelable(false)
        setOnCancelListener(null)
        var dialogCustomProgressBinding = DialogCustomProgressBinding.inflate(layoutInflater)
        val view: View = dialogCustomProgressBinding.root
        dialogCustomProgressBinding.textView.text = message
        setContentView(view)
    }
}