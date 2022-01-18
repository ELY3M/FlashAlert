package fr.jnda.android.flashalert.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import com.jpardogo.android.googleprogressbar.library.ChromeFloatingCirclesDrawable
import fr.jnda.android.flashalert.R

class WaitDialogFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.waiting_layout, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCancelable(false)
        view.findViewById<ProgressBar>(R.id.google_progress)?.apply {
            indeterminateDrawable = ChromeFloatingCirclesDrawable.Builder(context).build()
        }
        return view
    }
}