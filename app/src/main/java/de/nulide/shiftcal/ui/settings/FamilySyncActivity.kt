package de.nulide.shiftcal.ui.settings

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.NoConnectionError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.android.material.appbar.MaterialToolbar
import com.google.zxing.BarcodeFormat
import de.nulide.shiftcal.R
import de.nulide.shiftcal.crypt.PasswordGenerator
import de.nulide.shiftcal.data.migration.DBMigrator
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.databinding.ActivityFamilySyncBinding
import de.nulide.shiftcal.net.ShiftSwiftServerRestApi
import de.nulide.shiftcal.net.dto.SpectatorList
import de.nulide.shiftcal.net.listener.AccountDeleteListener
import de.nulide.shiftcal.net.listener.ActiveSpectatorListListener
import de.nulide.shiftcal.net.listener.OTTAcquiredListener
import de.nulide.shiftcal.net.listener.RegistrationListener
import de.nulide.shiftcal.net.listener.SpectatorAcquiredListener
import de.nulide.shiftcal.net.listener.VersionListener
import de.nulide.shiftcal.ui.helper.NameDialog
import de.nulide.shiftcal.ui.helper.WarningDialog
import de.nulide.shiftcal.ui.settings.list.SpectatorAdapter
import de.nulide.shiftcal.ui.settings.list.SpectatorDeletePressedListener
import de.nulide.shiftcal.ui.settings.usecase.SpectateUseCase
import de.nulide.shiftcal.utils.QRCodeHelper
import de.nulide.shiftcal.utils.Snack
import de.nulide.shiftcal.utils.permission.Perm
import de.nulide.shiftcal.utils.permission.PermissionGrantedListener
import de.nulide.shiftcal.utils.permission.PermissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FamilySyncActivity : AppCompatActivity(), View.OnClickListener, Response.ErrorListener,
    RegistrationListener, ActiveSpectatorListListener,
    SpectatorDeletePressedListener,
    AccountDeleteListener, PermissionGrantedListener, OTTAcquiredListener, VersionListener {

    lateinit var binding: ActivityFamilySyncBinding
    lateinit var codeScanner: CodeScanner
    lateinit var serverApi: ShiftSwiftServerRestApi
    lateinit var settings: SettingsRepository
    lateinit var sc: SCRepoManager

    lateinit var permissionManager: PermissionManager
    lateinit var spectateUseCase: SpectateUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFamilySyncBinding.inflate(layoutInflater)
        setContentView(binding.familySync)

        settings = SettingsRepository.getInstance(this)
        sc = SCRepoManager.getInstance(this)
        serverApi = ShiftSwiftServerRestApi(this)
        spectateUseCase = SpectateUseCase(this, this, this)

        permissionManager = PermissionManager(this)

        DBMigrator(this).migrate()

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { _: View? ->
            onBackPressedDispatcher.onBackPressed()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.familySync)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        codeScanner = CodeScanner(this, binding.scannerView)
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = listOf(BarcodeFormat.QR_CODE)
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                spectateUseCase.registerAsSpectator(it.text)
            }
        }

        binding.connectButton.setOnClickListener(this)
        binding.shareButton.setOnClickListener(this)
        binding.disconnectButton.setOnClickListener(this)
        binding.nameSettingsButton.setOnClickListener(this)
        binding.deleteAccountButton.setOnClickListener(this)

        updateViews()
        serverApi.getVersion(this, null)
    }

    fun updateViews() {
        if (sc.fromLocal { sc.users.getName() }.isEmpty()) {
            binding.nameContainer.visibility = View.GONE
        } else {
            binding.nameContainer.visibility = View.VISIBLE
            binding.nameText.text = sc.fromLocal { sc.users.getName() }
        }
        if (sc.users.getSubscribed() == null) {
            binding.connectDescText.text = getText(R.string.family_sync_connect_desc)
            binding.disconnectButton.visibility = View.GONE
            binding.connectButton.visibility = View.VISIBLE
        } else {
            var name = sc.users.getSubscribed()!!.name
            if (name.isEmpty()) {
                name = getString(R.string.name_someone)
            }
            binding.connectDescText.text =
                getString(R.string.family_sync_currently_connected, name)
            binding.disconnectButton.text = getString(R.string.settings_family_sync_disconnect_btn)
            binding.connectButton.visibility = View.GONE
        }
        if (sc.users.getLocal().netUuid == null) {
            binding.deleteAccountButton.visibility = View.GONE
            binding.connectedDevicesContainer.visibility = View.GONE
        } else {
            binding.deleteAccountButton.visibility = View.VISIBLE
            serverApi.getSpectators(this, this)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.connectButton -> {
                if (sc.fromLocal { sc.users.getName() }.isEmpty()) {
                    NameDialog(this) {
                        updateViews()
                        startScanForQR()
                    }
                } else {
                    startScanForQR()
                }
            }

            binding.shareButton -> {
                if (sc.fromLocal { sc.users.getName() }.isEmpty()) {
                    NameDialog(this) {
                        updateViews()
                        registerOnServer()
                    }
                } else {
                    registerOnServer()
                }
            }

            binding.disconnectButton -> {
                sc.users.removeSubscriptions()
                sc.switchToLocal()
            }

            binding.deleteAccountButton -> {
                val warningDialog =
                    WarningDialog(this, getString(R.string.warning_family_sync_delete_account))
                warningDialog.setPositiveButton(R.string.yes, { _, _ ->
                    serverApi.deleteData(this, this)
                })
                warningDialog.setNegativeButton(R.string.no, { dialog, _ ->
                    dialog.dismiss()
                })
                warningDialog.show()
            }

            binding.nameSettingsButton -> {
                NameDialog(this, null)
            }
        }
        updateViews()
    }

    override fun onResume() {
        super.onResume()
        updateViews()
    }

    fun showQRCode(dataString: String) {
        val drawable = QRCodeHelper.generateQRCode(this, dataString)
        binding.qrCode.setImageDrawable(drawable)
        binding.qrCode.visibility = View.VISIBLE
        binding.shareButton.visibility = View.GONE
        binding.deleteAccountButton.visibility = View.GONE
        binding.connectContainer.visibility = View.GONE
    }

    fun revertUIAfterAcquisition() {
        runOnUiThread {
            binding.qrCode.visibility = View.GONE
            binding.shareButton.visibility = View.VISIBLE
            binding.deleteAccountButton.visibility = View.VISIBLE
            binding.connectContainer.visibility = View.VISIBLE
            updateViews()
        }
    }

    private fun startScanForQR() {
        if (permissionManager.check(Perm.CAMERA)) {
            showQRCodeScanner()
        } else {
            permissionManager.request(Perm.CAMERA)
        }
    }

    private fun showQRCodeScanner() {
        binding.scannerView.visibility = View.VISIBLE
        codeScanner.startPreview()
        binding.shareContainer.visibility = View.GONE
        binding.connectButton.visibility = View.GONE
    }

    private fun registerOnServer() {
        binding.qrCodeProgressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            val user = sc.users.getLocal()
            if (user.netUuid == null) {
                val pw = PasswordGenerator.genNewPW()
                val name = sc.fromLocal { sc.users.getName() }
                user.password = pw
                sc.users.update(user)
                val serverApi = ShiftSwiftServerRestApi(this@FamilySyncActivity)
                serverApi.register(name, pw, this@FamilySyncActivity, this@FamilySyncActivity)
            } else {
                requestNewSpectator()
            }
        }
    }

    private fun requestNewSpectator() {
        serverApi.getNewSpectatorOTT(this, this)
    }

    override fun onErrorResponse(error: VolleyError?) {
        if (error is NoConnectionError) {
            Snack.not(binding.root, getString(R.string.net_error_no_connection))
        } else {
            println(error?.toString())
            Snack.not(binding.root, getString(R.string.net_error_retry_later))
        }
    }

    override fun onRegistrationSucceeds(res: String) {
        val user = sc.users.getLocal()
        user.netUuid = res
        sc.users.update(user)
        requestNewSpectator()
    }

    fun packageCalShareString(id: String, pwd: String): String {
        return "$id@@@$pwd"
    }

    override fun onActiveSpectatorListenerReceived(spectators: SpectatorList) {
        if (spectators.isNotEmpty()) {
            binding.connectedDevicesContainer.visibility = View.VISIBLE
            binding.connectedDevices.layoutManager = LinearLayoutManager(this)
            binding.connectedDevices.isNestedScrollingEnabled = false
            binding.connectedDevices.setHasFixedSize(true)
            binding.connectedDevices.adapter = SpectatorAdapter(this, spectators, this)
        } else {
            binding.connectedDevicesContainer.visibility = View.GONE
        }
    }

    override fun onSpectatorDeletePressed(name: String, uuid: String) {
        val warningDialog =
            WarningDialog(this, getString(R.string.warning_family_sync_delete_spectator, name))
        warningDialog.setPositiveButton(R.string.yes, { _, _ ->
            serverApi.removeSpectator(uuid, {
                sc.users.removeByUuid(uuid)
                updateViews()
            }, this)
        })
        warningDialog.setNegativeButton(R.string.no, { dialog, _ ->
            dialog.dismiss()
        })
        warningDialog.show()
    }

    override fun onAccountDelete() {
        sc.users.removeShared()
        val user = sc.users.getLocal()
        user.netUuid = null
        user.password = ""
        sc.users.update(user)
        settings.resetSharingAccount()
        serverApi.setAuthToken("")
        updateViews()
    }

    override fun onPermissionGranted(perm: String) {
        if (perm == Perm.CAMERA) {
            showQRCodeScanner()
        }
    }

    override fun onPermissionNotGranted(perm: String) {
        Snack.not(binding.root, getString(R.string.settings_permission_manual))
    }

    override fun onOTTAcquired(ott: String) {
        val pwd = PasswordGenerator.genNewPW()
        val calShareString = packageCalShareString(ott, pwd)
        binding.qrCodeProgressBar.visibility = View.GONE
        showQRCode(calShareString)
        val spectatorAcquiredListener = object : SpectatorAcquiredListener {
            override fun onSpectatorAcquired(msg: String) {
                runOnUiThread {
                    revertUIAfterAcquisition()
                    sc.users.addSharing(msg, pwd)
                    serverApi.updateCalendar()
                }
            }
        }
        serverApi.waitForAcquisition(spectatorAcquiredListener, this)
    }

    override fun onVersionAcquired(version: String) {
        if (version != ShiftSwiftServerRestApi.CURRENT_SUPPORTED_API_VERSION) {
            val msg = getString(R.string.family_sync_not_supported_api_version)
            val dialog = WarningDialog(this, WarningDialog.ICON_WARNING, msg)
            dialog.enableNeutralButton()
            dialog.show()
        }
    }

}