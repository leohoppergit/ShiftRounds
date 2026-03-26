package de.nulide.shiftcal.ui.settings

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import de.nulide.shiftcal.R
import de.nulide.shiftcal.databinding.ActivityPrivacyBinding
import de.nulide.shiftcal.ui.helper.OnCloseListener

class PrivacyActivity : AppCompatActivity(), OnCloseListener {

    lateinit var binding: ActivityPrivacyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPrivacyBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { _: View? ->
            onBackPressedDispatcher.onBackPressed()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.privacySettings)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val privacySettingsHandler = PrivacySettingsHandler(
            this,
            binding.includedPrivacySettingsContent.privacySettingsContent,
            true
        )
        privacySettingsHandler.onCloseListener = this
    }

    override fun onClose() {
        onBackPressedDispatcher.onBackPressed()
    }
}