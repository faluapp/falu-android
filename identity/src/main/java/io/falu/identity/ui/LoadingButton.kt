package io.falu.identity.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.google.android.material.button.MaterialButton
import io.falu.identity.R
import io.falu.identity.databinding.ViewLoadingButtonBinding

internal class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(
    context,
    attrs,
    defStyleAttr
) {

    init {
        ViewLoadingButtonBinding.inflate(LayoutInflater.from(context), this)
    }

    private val loadingButton: MaterialButton = findViewById(R.id.button_loading)

    var text: CharSequence
        get() = loadingButton.text
        set(value) {
            loadingButton.text = value
        }

    override fun setOnClickListener(listener: OnClickListener?) {
        loadingButton.setOnClickListener(listener)
    }

    override fun setClickable(clickable: Boolean) {
        loadingButton.isClickable = clickable
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        loadingButton.isEnabled = enabled
    }

    fun showProgress() {
        findViewById<MaterialButton>(R.id.button_loading).isEnabled = false
        findViewById<ProgressBar>(R.id.progress_view).visibility = View.VISIBLE
    }

    fun hideProgress() {
        findViewById<MaterialButton>(R.id.button_loading).isEnabled = true
        findViewById<ProgressBar>(R.id.progress_view).visibility = View.GONE
    }
}