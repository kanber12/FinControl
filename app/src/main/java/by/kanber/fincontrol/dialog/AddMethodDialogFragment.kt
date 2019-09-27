package by.kanber.fincontrol.dialog

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import by.kanber.fincontrol.R
import by.kanber.fincontrol.model.PaymentMethod
import by.kanber.fincontrol.util.TextUtil
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_add_method_dialog.view.*

class AddMethodDialogFragment : androidx.fragment.app.DialogFragment() {
    private lateinit var nameEditText: TextInputEditText
    private lateinit var isDefaultCheckBox: CheckBox
    private lateinit var nameTextInputLayout: TextInputLayout
    private lateinit var positiveButton: Button
    private var listener: OnAddMethodFragmentInteractionListener? = null
    private var name = ""
    private var isEdit = false

    fun setOnAddMethodListener(listener: OnAddMethodFragmentInteractionListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            name = arguments?.getString("name", "")!!
        }

        if (name != "") {
            isEdit = true
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!)
        val view = activity?.layoutInflater?.inflate(R.layout.fragment_add_method_dialog, null)!!

        nameEditText = view.add_method_name_edit_text
        isDefaultCheckBox = view.add_method_is_default_check_box
        nameTextInputLayout = view.add_method_name_text_input_layout

        if (isEdit) {
            nameEditText.append(name)
            isDefaultCheckBox.visibility = View.GONE
        }

        nameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val str = s.toString().trim()
                val error: String

                error = if (TextUtil.isEmpty(str) || listener?.isNameUnique(str)!!) {
                    positiveButton.isEnabled = true
                    ""
                } else {
                    if (isEdit && str == name) {
                        positiveButton.isEnabled = false
                        ""
                    } else {
                        "Method with same name already exist"
                    }
                }

                nameTextInputLayout.error = error
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

        val addDialog = builder.create()

        addDialog.setOnShowListener { dialog ->
            positiveButton = addDialog.getButton(Dialog.BUTTON_POSITIVE)

            positiveButton.setOnClickListener {
                if (positiveButtonClicked()) dialog.dismiss()
            }

            if (isEdit) {
                positiveButton.isEnabled = false
            }
        }

        isCancelable = false

        return addDialog
    }

    private fun positiveButtonClicked(): Boolean {
        return if (isEdit) {
            editClicked()
        } else {
            addClicked()
        }
    }

    private fun addClicked(): Boolean {
        val name = nameEditText.text.toString().trim()
        val isDefault = isDefaultCheckBox.isChecked

        if (isCorrectName(name)) {
            listener?.addButtonClicked(PaymentMethod(name, isDefault))

            return true
        }

        return false
    }

    private fun editClicked(): Boolean {
        val name = nameEditText.text.toString().trim()

        if (isCorrectName(name)) {
            listener?.editButtonClicked(name)

            return true
        }

        return false
    }

    private fun isCorrectName(name: String): Boolean {
        if (TextUtil.isEmpty(name)) {
            val error = "Method name is required"
            nameTextInputLayout.error = error

            return false
        }

        return listener?.isNameUnique(name)!!
    }

    interface OnAddMethodFragmentInteractionListener {
        fun editButtonClicked(name: String) {}
        fun addButtonClicked(method: PaymentMethod) {}
        fun isNameUnique(name: String): Boolean
    }

    companion object {
        fun newInstance(name: String) = AddMethodDialogFragment().apply {
            arguments = Bundle().apply {
                putString("name", name)
            }
        }
    }
}