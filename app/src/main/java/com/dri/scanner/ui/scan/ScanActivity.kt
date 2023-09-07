package com.dri.scanner.ui.scan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.dri.scanner.R
import com.dri.scanner.databinding.ActivityScanBinding
import com.dri.scanner.ui.analyzer.MLKitBarcodeAnalyzer
import com.dri.scanner.ui.analyzer.ScanningResultListener
import com.dri.scanner.ui.webview.WebviewActivity
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            val starter = Intent(context, ScanActivity::class.java)
            context.startActivity(starter)
        }
    }

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var binding: ActivityScanBinding
    private lateinit var cameraExecutor: ExecutorService
    private var flashEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))

        binding.overlay.post {
            binding.overlay.setViewFinder()
        }
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider?) {
        if (isDestroyed || isFinishing) {
            return
        }

        cameraProvider?.unbindAll()

        val preview: Preview = Preview.Builder()
            .build()

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(binding.cameraPreview.width, binding.cameraPreview.height))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val orientationEventListener = object : OrientationEventListener(this as Context) {
            override fun onOrientationChanged(orientation: Int) {
                val rotation: Int = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                imageAnalysis.targetRotation = rotation
            }
        }
        orientationEventListener.enable()

        class ScanningListener(private val context: Context) : ScanningResultListener {
            override fun onScanned(result: String) {
                runOnUiThread {
                    imageAnalysis.clearAnalyzer()
                    cameraProvider?.unbindAll()

                    // Check if the scanned result is a URL (you can customize this check)
                    if (isValidUrl(result)) {
                        // Open the WebView activity with the scanned URL
                        val intent = Intent(context, WebviewActivity::class.java)
                        intent.putExtra(WebviewActivity.EXTRA_URL, result)
                        context.startActivity(intent)
                    } else {
                        // Handle other types of QR code data as needed
                        ResultFragment.newInstance(
                            result,
                            object : ResultFragment.DialogDismissListener {
                                override fun onDismiss() {
                                    bindPreview(cameraProvider)
                                }
                            }
                        ).show(supportFragmentManager, ResultFragment::class.java.simpleName)
                    }
                }
            }

            private fun isValidUrl(url: String): Boolean {
                // You can implement a URL validation logic here based on your requirements
                return url.startsWith("http://") || url.startsWith("https://")
            }
        }


        val analyzer = MLKitBarcodeAnalyzer(ScanningListener(this))
        imageAnalysis.setAnalyzer(cameraExecutor, analyzer)

        preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)

        val camera =
            cameraProvider?.bindToLifecycle(this, cameraSelector, imageAnalysis, preview)

        if (camera?.cameraInfo?.hasFlashUnit() == true) {
            binding.ivFlashControl.visibility = View.VISIBLE

            binding.ivFlashControl.setOnClickListener {
                camera.cameraControl.enableTorch(!flashEnabled)
            }

            camera.cameraInfo.torchState.observe(this) {
                it?.let { torchState ->
                    if (torchState == TorchState.ON) {
                        flashEnabled = true
                        binding.ivFlashControl.setImageResource(R.drawable.ic_round_flash_on)
                    } else {
                        flashEnabled = false
                        binding.ivFlashControl.setImageResource(R.drawable.ic_round_flash_off)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}