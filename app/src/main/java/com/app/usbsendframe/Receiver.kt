package com.app.usbsendframe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.app.usbsendframe.databinding.ActivityReceiverBinding
import java.io.IOException

class Receiver : AppCompatActivity() {
    private lateinit var binding: ActivityReceiverBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiverBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (isUsbConnected()) {
            receiveBitmapFromUsb()?.let { bitmap ->
                binding.imageView.setImageBitmap(bitmap)
            }
        }
    }

    private fun isUsbConnected(): Boolean {
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        return usbManager.deviceList.isNotEmpty()
    }

    private fun receiveBitmapFromUsb(): Bitmap? {
        var bitmap: Bitmap? = null
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        val usbDevice = usbManager.deviceList.values.firstOrNull { device ->
            device.vendorId == YOUR_VENDOR_ID && device.productId == YOUR_PRODUCT_ID
        }

        usbDevice?.let { device ->
            val usbConnection = usbManager.openDevice(device)

            usbConnection?.let { connection ->
                try {
                    val endPointIn = device.getInterface(0).getEndpoint(0)
                    val bufferSize = endPointIn.maxPacketSize
                    val buffer = ByteArray(bufferSize)

                    val transferredBytes = connection.bulkTransfer(endPointIn, buffer, buffer.size, 10000)

                    if (transferredBytes > 0) {
                        bitmap = BitmapFactory.decodeByteArray(buffer, 0, transferredBytes)
                    } else {
                        Log.e("Receiver", "Error receiving data from USB device: No data received")
                    }
                } catch (e: IOException) {
                    Log.e("Receiver", "Error communicating with USB device", e)
                } finally {
                    connection.close()
                }
            }
        }
        return bitmap
    }


    companion object {
        private const val YOUR_VENDOR_ID = 1234 // Replace with your vendor ID
        private const val YOUR_PRODUCT_ID = 5678 // Replace with your product ID
    }
}