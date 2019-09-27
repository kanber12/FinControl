package by.kanber.fincontrol.dialog

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ConfirmationDialogFragment : DialogFragment() {
    private var text = ""
    private var posBtnText = ""
    private var listener: (() -> Unit)? = null

    fun setOnConfirmClickListener(listener: (() -> Unit)?) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        text = arguments?.getString("text") ?: ""
        posBtnText = arguments?.getString("posBtnText") ?: "Yes"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        AlertDialog.Builder(context!!).apply {
            setTitle("Please confirm")
            setMessage(text)
            setPositiveButton(posBtnText) { _, _ -> listener?.invoke() }
            setNegativeButton("Cancel", null)
        }.create()

    companion object {
        fun newInstance(text: String) = ConfirmationDialogFragment().apply {
            arguments = Bundle().apply {
                putString("text", text)
            }
        }

        fun newInstance(text: String, posBtnText: String) = ConfirmationDialogFragment().apply {
            arguments = Bundle().apply {
                putString("text", text)
                putString("posBtnText", posBtnText)
            }
        }
    }
}