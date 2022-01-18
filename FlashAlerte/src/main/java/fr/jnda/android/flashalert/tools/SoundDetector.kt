package fr.jnda.android.flashalert.tools

import android.media.MediaRecorder
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.*
import kotlin.math.log10


class SoundDetector {
    private var mRecorder: MediaRecorder? = null
    private val REFERENCE = 0.1
    private lateinit var recorder: RecorderTask
    private lateinit var timer: Timer
    private val MAXVALUE = 32762

    init {
        mRecorder = MediaRecorder()
    }


    fun start() {
        mRecorder?.run {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile("/dev/null")
            recorder = RecorderTask(this)
            timer =  Timer()
            timer.scheduleAtFixedRate(recorder, 0, 50)

            try {
                prepare()
                start()
            } catch (e :IllegalStateException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

    fun getMaxLevel():Double = getNoiseLevel(MAXVALUE)

    fun getNoiseLevel(amplitude: Int): Double {
        val db = 20 * log10(amplitude / REFERENCE)
        return if (db > 0) {
            db
        } else {
            0.0
        }
    }

    fun timerIsRunning() = recorder.isRunning

    fun stop(){
        recorder.cancel()
        timer.cancel()
        timer.purge()
    }

    suspend fun getAverageLevel(): Double{
        while(averageList.size < 5){
            delay(50)
        }
        recorder.cancel()
        timer.cancel()

        return averageList.average()
    }
    fun getLastValue(): Double = averageList.lastOrNull()?:0.0

    private val averageList = mutableListOf<Double>()
    private inner class RecorderTask(val recorder:MediaRecorder) : TimerTask() {

        var isRunning = false

        override fun run() {
            isRunning = true
            getNoiseLevel(recorder.maxAmplitude).takeIf { it > 0.0 }?.let {
                averageList.add(it)
            }
        }

        override fun cancel(): Boolean {
            isRunning = false
            return super.cancel()
        }
    }
}