package com.rnarcore

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.rnarcore.databinding.ActivityStickerBinding
import com.rnarcore.MainActivity


class StickerActivity : AppCompatActivity() {

    private lateinit var viewBinding : ActivityStickerBinding

    private var imageCapture : ImageCapture? = null
    private lateinit var cameraExecutor : ExecutorService

    private lateinit var imageView : ImageView
    private lateinit var seekBar : SeekBar

    var dx: Float = 0.0f
    var dy: Float = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityStickerBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        imageView = viewBinding.imageView
        seekBar= viewBinding.slider

        imageView.setOnTouchListener(OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dx = imageView.x - event.rawX
                    dy = imageView.y - event.rawY
                }

                MotionEvent.ACTION_MOVE -> imageView.animate()
                    .x(event.rawX + dx)
                    .y(event.rawY + dy)
                    .setDuration(0)
                    .start()

                else -> return@OnTouchListener false
            }
            true
        });

        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val scale = progress / 100f // Convert progress to scale factor
                imageView.scaleX = scale
                imageView.scaleY = scale
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // pick image from gallery and set it to imageView
        viewBinding.pickImageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        // return to react native activity
        viewBinding.returnBtn.setOnClickListener {
            returnToReactActivity()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            viewBinding.viewFinder.post { startCamera() }
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, 10)
        }
    }

    private fun takePhoto(){}

    // set the image to imageView on result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 100) {
            imageView.setImageURI(data?.data)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

        fun returnToReactActivity() {
        val intent = Intent(this, MainActivity::class.java)
        
        // put last x and y coordinates of the sticker in intent in READING and send it back to react native
        val lastCompData = "x: ${imageView.x}, y: ${imageView.y}"
        READING = lastCompData


        intent.putExtra("lastCompData", lastCompData)
        Log.d("results", lastCompData.toString())
        this.startActivity(intent)
    }

    companion object {
        @JvmField
        var READING: String = ""
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
            ).apply {
            }.toTypedArray()
    }
}