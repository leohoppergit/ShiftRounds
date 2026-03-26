package de.nulide.shiftcal.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikepenz.aboutlibraries.util.parseData
import de.nulide.shiftcal.R
import de.nulide.shiftcal.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding
    private var libraries = emptyList<com.mikepenz.aboutlibraries.entity.Library>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.aboutRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val json = resources.openRawResource(R.raw.aboutlibraries).bufferedReader().use {
            it.readText()
        }
        libraries = parseData(json).libraries.sortedBy { it.name.lowercase() }

        binding.aboutList.layoutManager = LinearLayoutManager(this)
        binding.aboutList.adapter = AboutLibraryAdapter(libraries) { library ->
            showLibraryDialog(library)
        }
    }

    private fun showLibraryDialog(library: com.mikepenz.aboutlibraries.entity.Library) {
        val message = buildString {
            appendLine(library.uniqueId)
            appendLine()
            val description = library.description.orEmpty()
            if (description.isNotBlank()) {
                appendLine(description)
                appendLine()
            }
            appendLine(getString(R.string.about_library_version, library.artifactVersion))
            val licenseNames = library.licenses.map { it.name }.sorted()
            if (licenseNames.isNotEmpty()) {
                appendLine(getString(R.string.about_library_license, licenseNames.joinToString()))
            }
            val website = library.website.orEmpty()
            if (website.isNotBlank()) {
                appendLine(getString(R.string.about_library_website, website))
            }
        }.trim()

        MaterialAlertDialogBuilder(this)
            .setTitle(library.name)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private class AboutLibraryAdapter(
        private val libraries: List<com.mikepenz.aboutlibraries.entity.Library>,
        private val onClick: (com.mikepenz.aboutlibraries.entity.Library) -> Unit
    ) : RecyclerView.Adapter<AboutLibraryAdapter.AboutLibraryViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutLibraryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_about_library, parent, false)
            return AboutLibraryViewHolder(view as TextView, onClick)
        }

        override fun onBindViewHolder(holder: AboutLibraryViewHolder, position: Int) {
            holder.bind(libraries[position])
        }

        override fun getItemCount(): Int = libraries.size

        class AboutLibraryViewHolder(
            private val titleView: TextView,
            private val onClick: (com.mikepenz.aboutlibraries.entity.Library) -> Unit
        ) : RecyclerView.ViewHolder(titleView) {

            fun bind(library: com.mikepenz.aboutlibraries.entity.Library) {
                titleView.text = "${library.name} ${library.artifactVersion}"
                titleView.setOnClickListener { onClick(library) }
            }
        }
    }
}
