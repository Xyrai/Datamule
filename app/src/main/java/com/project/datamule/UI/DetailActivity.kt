package com.project.datamule.UI

import android.animation.AnimatorInflater
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.project.datamule.DataClass.Pi
import com.project.datamule.R
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.dialog_transfer_question.*
import java.io.File
import com.google.firebase.internal.FirebaseAppHelper.getUid
import com.google.firebase.auth.FirebaseUser
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T


const val PI_EXTRA = "PI_EXTRA"

class DetailActivity : AppCompatActivity() {
    // Create a storage reference from our app
    private val storageRef = FirebaseStorage.getInstance().reference
    //TODO: Do not make this hardcoded
    private val fileUri: Uri? =
        Uri.fromFile(File("/data/user/0/com.project.datamule/cache/PI-data.json"))
    //TODO: Change this to where you want to safe it
    //Example data/test.txt creates a folder: data, in the storage with the file test.txt in it
    private var fileRef: StorageReference = storageRef.child("test.txt")
    //TAG for Logs
    private val TAG = "DetailActivity"
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        //TODO: Anonymous authentication to Firebase, maybe change this later on?
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInAnonymously:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInAnonymously:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }

                // ...
            }
        initViews()
    }

//    public override fun onStart() {
//        super.onStart()
//        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.currentUser
//        updateUI(currentUser)
//    }

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
            dialog.cancel()
        }

        dialog.btnConfirmTransfer.setOnClickListener {
            buildTransferDialog()

            //TODO: DO NOT DELETE THIS CODE! Puts file into the storage
//            fileRef.putFile(fileUri!!)
//                .addOnSuccessListener { taskSnapshot ->
//                    Log.e(TAG, "Uri: " + taskSnapshot.uploadSessionUri)
//                    Log.e(TAG, "Name: " + taskSnapshot.metadata!!.name)
//                    Toast.makeText(this, "File Uploaded ", Toast.LENGTH_LONG).show()
//                    // Uri: taskSnapshot.downloadUrl
//                    // Name: taskSnapshot.metadata!!.name
//                    // Path: taskSnapshot.metadata!!.path
//                    // Size: taskSnapshot.metadata!!.sizeBytes
//                }
//                .addOnFailureListener { exception ->
//                    // Handle unsuccessful uploads
//                }
//                .addOnProgressListener { taskSnapshot ->
//                    // taskSnapshot.bytesTransferred
//                    // taskSnapshot.totalByteCount
//                }
//                .addOnPausedListener { taskSnapshot ->
//                    // Upload is paused
//                }
            dialog.cancel()

        }

//        val builder = android.app.AlertDialog.Builder(this)
//        builder.setView(layoutInflater.inflate(R.layout.dialog_transfer_question, null))
//        builder.create().show()
    }

    private fun buildTransferDialog() {
        var dialog = Dialog(this@DetailActivity)
        dialog.setContentView(R.layout.dialog_transfer)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        var animatorSet = AnimatorInflater.loadAnimator(
            this@DetailActivity,
            R.animator.loading_animator
        )
        animatorSet.setTarget(dialog.ivTransferLoader)
        animatorSet.start()

        dialog.show()
    }

    private fun onClickBack() {
        finish()
    }

    private fun updateUI(user: FirebaseUser?) {
        //TODO: do something if the user is authenticated || not
        if (user != null) {
        } else {
            //TODO: do something if the user isn't authenticated
        }
    }
}
