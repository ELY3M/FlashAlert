@file:Suppress("DEPRECATION")

package fr.jnda.android.flashalert.impl

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Handler
import android.util.Log
import fr.jnda.android.flashalert.R
import fr.jnda.android.flashalert.tools.isMarshmallowPlus
import fr.jnda.android.flashalert.tools.isNougatPlus
import fr.jnda.android.flashalert.tools.toast
import java.io.IOException



class CameraImpl(private val mContext: Context) {
    private var stroboFrequency = 53L ///53L  //was 50L  higher = slow / lower = faster

    companion object {
        var isFlashlightOn = false

        private var camera: Camera? = null
        private var params: Camera.Parameters? = null
        private var isMarshmallow = false
        private var shouldEnableFlashlight = false
        @SuppressLint("StaticFieldLeak")
        private var marshmallowCamera: MarshmallowCamera? = null
        @Volatile
        private var shouldStroboscopeStop = false
        @Volatile
        private var isStroboscopeRunning = false

        fun newInstance(context: Context) = CameraImpl(context)





    }

    init {
        isMarshmallow = isMarshmallowPlus()
        handleCameraSetup()
    }

    private fun handleCameraSetup() {
        if (isMarshmallow) {
            setupMarshmallowCamera()
        } else {
            setupCamera()
        }
    }

    private fun setupMarshmallowCamera() {
        if (marshmallowCamera == null) {
            marshmallowCamera = MarshmallowCamera(mContext)
        }
    }

    private fun setupCamera() {
        if (isMarshmallow)
            return

        if (camera == null) {
            initCamera()
        }
    }


    private fun initCamera() {
        try {
            camera = Camera.open()
            params = camera!!.parameters
            params!!.flashMode = Camera.Parameters.FLASH_MODE_OFF
            camera!!.parameters = params
        } catch (e: Exception) {
            Log.e(TAG,"Error initialisation",e)
        }

    }



    private fun enableFlashlight() {
        shouldStroboscopeStop = true
        if (isStroboscopeRunning) {
            shouldEnableFlashlight = true
            return
        }

        if (isMarshmallow) {
            toggleMarshmallowFlashlight(true)
        } else {
            if (camera == null || params == null) {
                return
            }

            params!!.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            camera!!.parameters = params
            camera!!.startPreview()
        }

        val mainRunnable = Runnable { stateChanged(true) }
        Handler(mContext.mainLooper).post(mainRunnable)

    }

    private fun disableFlashlight() {
        if (isStroboscopeRunning) {
            return
        }

        if (isMarshmallow) {
            toggleMarshmallowFlashlight(false)
        } else {
            if (camera == null || params == null) {
                return
            }

            params!!.flashMode = Camera.Parameters.FLASH_MODE_OFF
            camera!!.parameters = params
        }
        stateChanged(false)
        releaseCamera()
    }

    private fun toggleMarshmallowFlashlight(enable: Boolean) {
        marshmallowCamera!!.toggleMarshmallowFlashlight(enable)
    }

    private fun stateChanged(isEnabled: Boolean) {
        isFlashlightOn = isEnabled
    }


    private fun releaseCamera() {
        if (isFlashlightOn) {
            disableFlashlight()
        }

        camera?.release()
        camera = null

        isFlashlightOn = false
        shouldStroboscopeStop = true
    }

    fun toggleStroboscope(): Boolean {
        if (!isStroboscopeRunning) {
            disableFlashlight()
        }

        if (!isNougatPlus()) {
            if (camera == null) {
                initCamera()
            }

            if (camera == null) {
                mContext.toast(R.string.camera_error)
                return false
            }
        }

        if (isStroboscopeRunning) {
            stopStroboscope()
        } else {
            Thread(stroboscope).start()
        }
        return true
    }

    fun stopStroboscope() {
        shouldStroboscopeStop = true
    }


    private val stroboscope = Runnable {
        if (isStroboscopeRunning) {
            return@Runnable
        }

        shouldStroboscopeStop = false
        isStroboscopeRunning = true

        if (isNougatPlus()) {
            while (!shouldStroboscopeStop) {
                try {
                    marshmallowCamera!!.toggleMarshmallowFlashlight(true)
                    Thread.sleep(stroboFrequency)
                    marshmallowCamera!!.toggleMarshmallowFlashlight(false)
                    Thread.sleep(stroboFrequency)
                } catch (e: Exception) {
                    shouldStroboscopeStop = true
                }
            }
        } else {
            if (camera == null) {
                initCamera()
            }

            val torchOn = camera!!.parameters
            val torchOff = camera!!.parameters
            torchOn.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            torchOff.flashMode = Camera.Parameters.FLASH_MODE_OFF

            val dummy = SurfaceTexture(1)
            try {
                camera!!.setPreviewTexture(dummy)
            } catch (e: IOException) {
            }

            camera!!.startPreview()

            while (!shouldStroboscopeStop) {
                try {
                    camera!!.parameters = torchOn
                    Thread.sleep(stroboFrequency)
                    camera!!.parameters = torchOff
                    Thread.sleep(stroboFrequency)
                } catch (e: Exception) {
                }
            }

            try {
                if (camera != null) {
                    camera!!.parameters = torchOff
                    if (!shouldEnableFlashlight || isMarshmallow) {
                        camera!!.release()
                        camera = null
                    }
                }
            } catch (e: RuntimeException) {
            }
        }

        isStroboscopeRunning = false
        shouldStroboscopeStop = false

        if (shouldEnableFlashlight) {
            enableFlashlight()
            shouldEnableFlashlight = false
        }
    }
}


