package com.app.usbsendframe

import android.content.Context
import android.graphics.Bitmap
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.app.usbsendframe.databinding.ActivitySenderBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.ByteArrayOutputStream
import java.io.IOException

class Sender : AppCompatActivity() {

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var binding: ActivitySenderBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySenderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpCamera()


    }


    private fun setUpCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.preview.surfaceProvider)
        }

        imageAnalysis = ImageAnalysis.Builder().build()
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) {
            processImage(it)
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
        } catch (exc: Exception) {
            Log.e("TAG", "Use case binding failed", exc)
        }
    }


    private fun processImage(imageProxy: ImageProxy) {
        val bitmap = imageProxy.toBitmap()
        if (isUsbConnected()) {
           sendBitmapToUsb(bitmap)
        }
        imageProxy.close()
    }


    private fun isUsbConnected(): Boolean {
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = usbManager.deviceList
        return deviceList.isNotEmpty()
    }

    private fun sendBitmapToUsb(bitmap: Bitmap) {
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        // Find the USB device
        val usbDevice = usbManager.deviceList.values.firstOrNull { device ->
            device.vendorId == YOUR_VENDOR_ID && device.productId == YOUR_PRODUCT_ID
        }

        usbDevice?.let { device ->
            val usbConnection = usbManager.openDevice(device)

            usbConnection?.let { connection ->
                try {
                    // Claim interface before communication
                    connection.claimInterface(device.getInterface(0), true)

                    // Compress bitmap to byte array
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    val byteArray = outputStream.toByteArray()

                    // Get endpoint for output data
                    val endPointOut = device.getInterface(0).getEndpoint(0)

                    // Write data to USB device using bulk transfer
                    val transferredBytes = connection.bulkTransfer(endPointOut, byteArray, byteArray.size, 10000)

                    if (transferredBytes < 0) {
                        Log.e("Sender", "Error transferring data to USB device")
                    } else {
                        Log.d("Sender", "Data transferred successfully to USB device")
                    }
                } catch (e: IOException) {
                    Log.e("Sender", "Error communicating with USB device", e)
                } finally {
                    // Release interface after communication
                    connection.releaseInterface(device.getInterface(0))
                    // Close USB connection
                    connection.close()
                }
            }
        }
    }


    companion object {
        private const val YOUR_VENDOR_ID = 1234 // Replace with your vendor ID
        private const val YOUR_PRODUCT_ID = 5678 // Replace with your product ID
    }
}

