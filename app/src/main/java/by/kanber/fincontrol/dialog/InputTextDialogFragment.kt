package by.kanber.fincontrol.dialog

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import by.kanber.fincontrol.R
import by.kanber.fincontrol.util.TextUtil
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.dialog_input_text.view.*

class InputTextDialogFragment : DialogFragment() {
    private var hint = ""
    private var text = ""
    private var isEdit = false
    private var isCurr = false
    private var listener: OnInputTextDialogInteractionListener? = null
    private lateinit var editText: TextInputEditText
    private lateinit var inputLayout: TextInputLayout
    private lateinit var positiveButton: Button

    fun setOnInputTextDialogInteractionListener(listener: OnInputTextDialogInteractionListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hint = arguments?.getString("hint")!!
        text = arguments?.getString("text", "")!!
        isEdit = arguments?.getBoolean("isEdit", false)!!
        isCurr = arguments?.getBoolean("isCurr", false)!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_input_text, null)

        editText = view.input_text_edit_text
        inputLayout = view.input_text_input_layout

        inputLayout.hint = hint

        if (isEdit) {
            editText.append(text)
        }

        if (isCurr) {
            editText.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val str = s.toString().trim()
                val error: String

                error = if (TextUtil.isEmpty(str) || listener?.isUnique(str)!!) {
                    positiveButton.isEnabled = true
                    ""
                } else {
                    if (isEdit && str == text) {
                        positiveButton.isEnabled = false
                        ""
                    } else {
                        "$hint already exist"
                    }
                }

                inputLayout.error = error
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        val posBtnText = if (isEdit) {
            "Edit"
        } else {
            "Add"
        }

        builder.apply {
            setView(view)
            setPositiveButton(posBtnText, null)
            setNegativeButton("Cancel", null)
        }

        val inputDialog = builder.create()

        inputDialog.setOnShowListener { dialog ->
            positiveButton = inputDialog.getButton(AlertDialog.BUTTON_POSITIVE)

            positiveButton.setOnClickListener {
                if (positiveButtonClicked()) {
                    dialog.dismiss()
                }
            }

            if (isEdit) {
                positiveButton.isEnabled = false
            }
        }

        isCancelable = false

        return inputDialog
    }

    private fun positiveButtonClicked(): Boolean {
        val text = editText.text.toString()

        if (isCorrectName(text)) {
            listener?.onPositiveButtonClick(text)

            return true
        }

        return false
    }

    private fun isCorrectName(name: String): Boolean {
        if (TextUtil.isEmpty(name)) {
            inputLayout.error = "$hint name is required"

            return false
        }

        return listener?.isUnique(name)!!
    }

    interface OnInputTextDialogInteractionListener {
        fun onPositiveButtonClick(text: String)
        fun isUnique(text: String): Boolean
    }

    companion object {
        fun newInstance(hint: String) = InputTextDialogFragment().apply {
            arguments = Bundle().apply {
                putString("hint", hint)
            }
        }

        fun newInstance(hint: String, text: String) = InputTextDialogFragment().apply {
            arguments = Bundle().apply {
                putString("hint", hint)
                putString("text", text)
                putBoolean("isEdit", true)
            }
        }

        fun newInstance(hint: String, isCurr: Boolean) = InputTextDialogFragment().apply {
            arguments = Bundle().apply {
                putString("hint", hint)
                putBoolean("isCurr", isCurr)
            }
        }

        fun newInstance(hint: String, text: String, isCurr: Boolean) = InputTextDialogFragment().apply {
            arguments = Bundle().apply {
                putString("hint", hint)
                putString("text", text)
                putBoolean("isEdit", true)
                putBoolean("isCurr", isCurr)
            }
        }
    }
}