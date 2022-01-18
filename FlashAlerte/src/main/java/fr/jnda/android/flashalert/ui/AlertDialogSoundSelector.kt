package fr.jnda.android.flashalert.ui

import android.app.Dialog
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import fr.jnda.android.flashalert.R
import fr.jnda.android.flashalert.tools.SoundDetector
import kotlinx.coroutines.*
import java.text.DecimalFormat


class AlertDialogSoundSelector : DialogFragment() {
    private val df2: DecimalFormat = DecimalFormat("#.##")
    private val soundListener = SoundDetector()
    private lateinit var dispatcherJob: Job
    private var listener: OnDialogCloseListener? = null

    override fun onDetach() {
        soundListener.stop()
        dispatcherJob.cancel()
        dismiss()
        super.onDetach()
    }

    override fun onPause() {
        super.onPause()
        onDetach()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {mActivity->
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
            val builder = AlertDialog.Builder(mActivity)
            builder.apply {
                val customView = mActivity.layoutInflater.inflate(R.layout.sound_alert_layout,null)
                val selectedText = customView.findViewById<TextView>(R.id.selectedValue)
                selectedText.text = String.format(getString(R.string.sound_selected_value),sharedPreferences.getInt("event_noiselimit", 0))
                val currentText = customView.findViewById<TextView>(R.id.currentValue)
                val soundSlider = customView.findViewById<SeekBar>(R.id.soundSelectedBar)

                soundListener.start()
                soundSlider.max = soundListener.getMaxLevel().toInt()
                dispatcherJob = GlobalScope.launch(Dispatchers.Main) {
                    while(soundListener.timerIsRunning()){
                        if(this@AlertDialogSoundSelector.isAdded)
                            currentText.text =  String.format(getString(R.string.sound_current_value), df2.format(soundListener.getLastValue()))
                        delay(1000)
                    }
                }
                soundSlider.progress = sharedPreferences.getInt("event_noiselimit", 0)
                soundSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        selectedText.text = String.format(getString(R.string.sound_selected_value),progress)
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) { }
                    override fun onStopTrackingTouch(seekBar: SeekBar?) { }
                })
                setView(customView)
                setPositiveButton(android.R.string.ok ) { _, _ ->
                    soundListener.stop()
                    sharedPreferences.edit {
                        putInt("event_noiselimit",soundSlider.progress)
                    }
                    listener?.onDialogClose()
                }
            }
            builder.setCancelable(false)
            builder.create()
        }?: throw IllegalStateException("Activity cannot be null")
    }


    fun addDialogCloseListener(listerner: OnDialogCloseListener){
        this.listener = listerner
    }
}

interface OnDialogCloseListener{
    fun onDialogClose()
}