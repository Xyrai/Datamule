package com.project.datamule.UI

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.project.datamule.DataClass.Pi
import com.project.datamule.R
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.dialog_transfer_question.*

const val PI_EXTRA = "PI_EXTRA"
class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        initViews()
    }

    fun initViews() {
        // Initialize Buttons
        ivBack.setOnClickListener { onClickBack() }
        btnTransferData.setOnClickListener { buildDialogTransferQuestion() }



        val pi = intent.getParcelableExtra<Pi>(PI_EXTRA)
        if (pi != null) {
            tvPiName.text = pi.name
        }
    }

    private fun buildDialogTransferQuestion() {
        var dialog = Dialog(this@DetailActivity)
        dialog.setContentView(R.layout.dialog_transfer_question)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        dialog.btnCancelTransfer.setOnClickListener {
            dialog.hide()
        }

        dialog.btnConfirmTransfer.setOnClickListener {
            dialog.hide()
        }

//        val builder = android.app.AlertDialog.Builder(this)
//        builder.setView(layoutInflater.inflate(R.layout.dialog_transfer_question, null))
//        builder.create().show()
    }

    private fun onClickBack() {
        finish()
    }
}
