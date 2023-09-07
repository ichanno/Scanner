package com.dri.scanner.ui.scan

import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil.isValidUrl
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.dri.scanner.databinding.FragmentResultBinding
import com.dri.scanner.ui.webview.WebviewActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

const val ARG_SCANNING_RESULT = "scanning_result"

class ResultFragment(private val listener: DialogDismissListener) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentResultBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentResultBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val scannedResult = arguments?.getString(ARG_SCANNING_RESULT)
        binding.edtResult.setText(scannedResult)
        binding.btnCopy.setOnClickListener {
            val clipboard =
                ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
            val clip = ClipData.newPlainText("label", scannedResult)
            clipboard?.setPrimaryClip(clip)

            // Check if the scanned result is a URL and open the WebView activity
            if (isValidUrl(scannedResult)) {
                val intent = Intent(requireContext(), WebviewActivity::class.java)
                intent.putExtra(WebviewActivity.EXTRA_URL, scannedResult)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show()
                dismissAllowingStateLoss()
            }
        }
    }

    companion object {

        fun newInstance(
            scanningResult: String,
            listener: DialogDismissListener
        ): ResultFragment =
            ResultFragment(listener).apply {
                arguments = Bundle().apply {
                    putString(ARG_SCANNING_RESULT, scanningResult)
                }
            }

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener.onDismiss()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener.onDismiss()
    }

    interface DialogDismissListener {
        fun onDismiss()
    }
}