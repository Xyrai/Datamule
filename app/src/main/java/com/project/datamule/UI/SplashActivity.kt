package com.project.datamule.UI

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.os.Handler
import com.project.datamule.R


class SplashActivity : AppCompatActivity() {


    private val hdlr = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        var pgsBar: ProgressBar = findViewById(R.id.pBar)

        var i = pgsBar.progress
        Thread(Runnable {
            while (i < 100) {
                i += 1
                // Update the progress bar and display the current value in text view
                hdlr.post(Runnable {
                    pgsBar.progress = i
                })
                try {
                    // Sleep for 50 milliseconds to show the progress slowly.
                    Thread.sleep(50)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }).start()

        Handler().postDelayed({
            startActivity(
                Intent(
                    this@SplashActivity,
                    HomeActivity::class.java
                )
            )
            finish()
        }, 2500)
    }
}
