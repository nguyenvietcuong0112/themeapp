package com.themes.diy.widgets.keyboard.controlcenter.feature_widget

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.themes.diy.widgets.keyboard.controlcenter.databinding.FragmentSetupSucceedDialogBinding

class SetupSucceedDialogFragment : DialogFragment() {

    private var _binding: FragmentSetupSucceedDialogBinding? = null
    private val binding get() = _binding!!

    private var customTitle: String? = null
    private var customMessage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customTitle = arguments?.getString(ARG_TITLE)
        customMessage = arguments?.getString(ARG_MESSAGE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            attributes?.windowAnimations = android.R.style.Animation_Dialog
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val dm = resources.displayMetrics
            val width = (dm.widthPixels * 0.88).toInt().coerceAtMost((360 * dm.density).toInt())
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupSucceedDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.adContainer.visibility = View.GONE

        customTitle?.let { binding.tvTitle.text = it }
        customMessage?.let { binding.tvMessage.text = it }

        binding.ivClose.setOnClickListener {
            dismiss()
        }
        binding.tvOk.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_MESSAGE = "arg_message"

        fun newInstance(title: String? = null, message: String? = null): SetupSucceedDialogFragment {
            return SetupSucceedDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_MESSAGE, message)
                }
            }
        }
    }
}
