package fr.jnda.android.flashalert.tools

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import kotlin.math.*


class CompassInfo(private val callback: DeviceController.DeviceControllerCallBack) : SensorEventListener2 {
    private var pitch = 0.0
    private var tilt:Double = 0.0
    private val degreeTolerance: Double = 10.0

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    override fun onFlushCompleted(sensor: Sensor?) { }

    override fun onSensorChanged(event: SensorEvent?) {

        val g = convertFloatsToDoubles(event?.values?.clone())

        g?.let {
            val norm = sqrt(g[0] * g[0] + g[1] * g[1] + g[2] * g[2] + g[3] * g[3])
            g[0] /= norm
            g[1] /= norm
            g[2] /= norm
            g[3] /= norm

            //Set values to commonly known quaternion letter representatives
            val x = g[0]
            val y = g[1]
            val z = g[2]
            val w = g[3]

            //Calculate Pitch in degrees (-180 to 180)
            val sinP = 2.0 * (w * x + y * z)
            val cosP = 1.0 - 2.0 * (x * x + y * y)
            pitch = atan2(sinP, cosP) * (180 / Math.PI)

            //Calculate Tilt in degrees (-90 to 90)
            val sinT = 2.0 * (w * y - z * x)
            tilt = if (abs(sinT) >= 1)
                (Math.PI / 2).withSign(sinT) * (180 / Math.PI)
            else
                asin(sinT) * (180 / Math.PI)
        }
    }

    fun flatEnough(): Boolean {
        callback.onSensorEvent()
        return isTilt() && ((isPitch()) || (isReversePitch()))
    }

    private fun isTilt():Boolean = tilt <= degreeTolerance && tilt >= -degreeTolerance
    private fun isPitch():Boolean = pitch <= degreeTolerance && pitch >= -degreeTolerance
    private fun isReversePitch():Boolean = inversePitch(pitch) <= degreeTolerance && inversePitch(pitch) >= -degreeTolerance

    private fun inversePitch(value:Double): Double{
        var reverseValue = value
        if (value > 0) {
            reverseValue -= 180
        } else
            reverseValue += 180
        return reverseValue
    }

    private fun convertFloatsToDoubles(input: FloatArray?): DoubleArray? {
        if (input == null) return null
        val output = DoubleArray(input.size)
        for (i in input.indices) output[i] = input[i].toDouble()
        return output
    }
}