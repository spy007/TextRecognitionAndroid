package com.tuts.prakash.simpleocr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.io.IOException


class MainActivity : AppCompatActivity() {

    lateinit var mButton: Button
    lateinit var mCameraView: SurfaceView
    lateinit var mTextView: TextView
    lateinit var mCameraSource: CameraSource
    val phoneNum = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mCameraView = findViewById(R.id.surfaceView)
        mTextView = findViewById(R.id.text_view)
        mButton = findViewById(R.id.button_phone)
        mButton.run {
            setOnClickListener { dialPhoneNumber() }
            visibility = GONE
        }

        startCameraSource()
    }

    fun dialPhoneNumber() {
        val phone = phoneNum.toString()

        if (phone.length > 0) {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${phone}")
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }

            Toast.makeText(this, "Calling ${phone} ...", Toast.LENGTH_LONG)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != requestPermissionID) {
            Log.d(TAG, "Got unexpected permission result: $requestCode")
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                mCameraSource.start(mCameraView.holder)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    private fun startCameraSource() {

        //Create the TextRecognizer
        val textRecognizer = TextRecognizer.Builder(applicationContext).build()

        if (!textRecognizer.isOperational) {
            Log.w(TAG, "Detector dependencies not loaded yet")
        } else {

            //Initialize camerasource to use high resolution and set Autofocus on.
            mCameraSource = CameraSource.Builder(applicationContext, textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(2.0f)
                    .build()

            /**
             * Add call back to SurfaceView and check if camera permission is granted.
             * If permission is granted we can start our cameraSource and pass it to surfaceView
             */
            mCameraView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    try {

                        if (ActivityCompat.checkSelfPermission(applicationContext,
                                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(this@MainActivity,
                                    arrayOf(Manifest.permission.CAMERA),
                                    requestPermissionID)
                            return
                        }
                        mCameraSource.start(mCameraView.holder)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }

                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    mCameraSource.stop()
                }
            })

            //Set the TextRecognizer's Processor.
            textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
                override fun release() {}

                /**
                 * Detect all the text from camera using TextBlock and the values into a stringBuilder
                 * which will then be set to the textView.
                 */
                override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
                    val textRecognized = detections.detectedItems.valueAt(0).value
                    Log.d("kiv007", textRecognized + "kiv007");
                    val plus = '+'
                    if (textRecognized.length != 0) {

                        if (textRecognized.get(0) != plus || phoneNum.contains(textRecognized)) {
                            return
                        }

                        mTextView.post {
                            phoneNum.append(plus)
                            var ch: Char
                            for (i in 1..textRecognized.length-1) {
                                ch = textRecognized.get(i)
                                if (ch >= '0' && ch <= '9') {
                                    phoneNum.append(ch)
                                } else {
                                    return@post
                                }
                            }
                            mTextView.text = phoneNum.toString()
                            mButton.run {
                                visibility = VISIBLE
                                text = mButton.text.toString() + phoneNum.toString()
                            }
//                            textRecognizer.release()
                        }
                    }
                }
            })
        }
    }

    companion object {
        private val TAG = "MainActivity"
        private val requestPermissionID = 101
    }
}
