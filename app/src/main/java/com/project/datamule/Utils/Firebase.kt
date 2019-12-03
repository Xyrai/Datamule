package com.project.datamule.Utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.project.datamule.Constants
import com.project.datamule.R
import java.io.File
import java.util.HashSet

object Firebase {
    private val storageRef = FirebaseStorage.getInstance().reference
    //TODO: Change this to where you want to safe it
    //Example data/test.txt creates a folder: data, in the storage with the file test.txt in it
    private var fileRef: StorageReference = storageRef.child("TEST23.json")
    //TAG for Logs
    private val TAG = "Firebase"
    private var PROGRESS_MAX = 100
    private var PROGRESS_CURRENT = 0

    private var prefs: SharedPreferences? = null


    fun uploadFile(context: Context) {
        // getSharedPreferences
        prefs = context.getSharedPreferences("com.project.datamule", AppCompatActivity.MODE_PRIVATE)

        // Retrieve & save the Set of cacheFiles
        val set = prefs!!.getStringSet("dataFiles", HashSet<String>())
        var fileName = ""

        Log.e("TESTTTTFIILESS+++", set.toString())

        if (!set.isEmpty()) {
            var sortedSet = set.sorted().toMutableSet()
            fileName = sortedSet.first()
            fileRef = storageRef.child(fileName)
            //remove from filesdir
            val bool = File(fileName).delete()
            Log.e("removed file:  " , fileName)
            sortedSet.remove(sortedSet.first())
            prefs!!.edit().putStringSet("dataFiles", sortedSet).apply()
        }

//        val networkResult = getConnectionType(this)
        val basePath = context.filesDir.toString() + "/"
//        var fileName = "/PI-data.json"
        val fileUri: Uri? = Uri.fromFile(File(basePath + fileName))

        Log.e("BASEPATH", basePath)
        Log.e("FILEuRI", fileUri.toString())


        if (!fileUri?.toFile()!!.exists() || fileName.isEmpty()) {
            Toast.makeText(context, "No file(s) found", Toast.LENGTH_LONG).show()
            return
        }
        fileRef.putFile(fileUri)
            .addOnSuccessListener { taskSnapshot ->
                Log.e(TAG, "Uri: " + taskSnapshot.uploadSessionUri)
                Log.e(TAG, "Name: " + taskSnapshot.metadata!!.name)
                uploadFinishedNotification(
                    fileUri.toFile().name,
                    context
                )
                // Uri: taskSnapshot.downloadUrl
                // Name: taskSnapshot.metadata!!.name
                // Path: taskSnapshot.metadata!!.path
                // Size: taskSnapshot.metadata!!.sizeBytes
            }
            .addOnFailureListener { exception ->
                // Handle unsuccessful uploads
            }
            .addOnProgressListener { taskSnapshot ->
                PROGRESS_MAX = taskSnapshot.totalByteCount.toInt() / 1000
                PROGRESS_CURRENT = taskSnapshot.bytesTransferred.toInt() / 1000
                makeNotification(
                    "Uploading file",
                    "$PROGRESS_CURRENT KB / $PROGRESS_MAX KB",
                    0,
                    context
                )
            }
            .addOnPausedListener { taskSnapshot ->
                // Upload is paused
            }
    }

    private fun uploadFinishedNotification(fileName: String, context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(
            context,
            Constants.CHANNEL_ID
        )
            .setSmallIcon(R.drawable.logo_no_text)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setContentTitle("File uploaded")
            .setContentText("Done.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).apply {

            builder.setContentText(fileName)
                .setProgress(0, 0, false)
            notify(0, builder.build())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(
                    Constants.CHANNEL_ID,
                    Constants.CHANNEL_NAME,
                    importance
                ).apply {
                    description = "channel"
                }
                // Register the channel with the system
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(0, builder.build())
        }
    }

    private fun makeNotification(
        title: String,
        content: String,
        notificationID: Int,
        context: Context
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(
            context,
            Constants.CHANNEL_ID
        )
            .setSmallIcon(R.drawable.logo_no_text)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).apply {
            // Issue the initial notification with zero progress
            builder.setProgress(
                PROGRESS_MAX,
                PROGRESS_CURRENT, false
            )
            notify(0, builder.build())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(
                    Constants.CHANNEL_ID,
                    Constants.CHANNEL_NAME,
                    importance
                ).apply {
                    description = "channel"
                }
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(notificationID, builder.build())
        }

    }
}