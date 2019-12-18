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
import kotlin.math.ln
import kotlin.math.pow

object Firebase {
    private val storageRef = FirebaseStorage.getInstance().reference
    //Example data/test.txt creates a folder: data, in the storage with the file test.txt in it
    private var fileRef: StorageReference = storageRef.child("TEST.json")
    //TAG for Logs
    private val TAG = "Firebase"
    private var prefs: SharedPreferences? = null

    fun uploadFile(context: Context) {
        // getSharedPreferences
        prefs = context.getSharedPreferences("com.project.datamule", AppCompatActivity.MODE_PRIVATE)

        // Retrieve & save the Set of cacheFiles
        val set = prefs!!.getStringSet("dataFiles", HashSet<String>())
        var fileName = ""

        Log.e("TESTTTTFIILESS+++", set.toString())

        val basePath = context.filesDir.toString() + "/"

        if (set.isNotEmpty()) {
            var sortedSet = set.sorted().toMutableSet()
            fileName = sortedSet.first()
            fileRef = storageRef.child(fileName)
            sortedSet.remove(sortedSet.first())
            prefs!!.edit().putStringSet("dataFiles", sortedSet).apply()
        }

        //Full path name to a file
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
            }
            .addOnFailureListener { exception ->
                // Handle unsuccessful uploads
            }
            .addOnProgressListener { taskSnapshot ->
                var minSize = humanReadableByteCount(taskSnapshot.bytesTransferred)
                var maxSize = humanReadableByteCount(taskSnapshot.totalByteCount)
                var minSizeInt = humanReadableByteCountToInt(taskSnapshot.bytesTransferred)
                var maxSizeInt = humanReadableByteCountToInt(taskSnapshot.totalByteCount)

                makeNotification(
                    "Uploading file",
                    "$minSize / $maxSize ",
                    minSizeInt,
                    maxSizeInt,
                    0,
                    context
                )
            }
            .addOnPausedListener { taskSnapshot ->
                // Upload is paused
            }

        //remove from filesdir
        File(basePath + fileName).delete()
    }

    /**
     * Notification used for successful file uploads
     */
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

    /**
     * Notification used for file uploads
     */
    private fun makeNotification(
        title: String,
        content: String,
        minSize: Int,
        maxSize: Int,
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
                maxSize,
                minSize, false
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

    /**
     * Method to get dynamic values of bytes (e.g. 1000 Bytes == 1 kB)
     */
    private fun humanReadableByteCount(bytes: Long, si: Boolean = true): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
        return String.format("%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }

    private fun humanReadableByteCountToInt(bytes: Long, si: Boolean = true): Int {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return bytes.toInt()
        return (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
    }
}