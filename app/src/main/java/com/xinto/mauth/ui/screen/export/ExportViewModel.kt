package com.xinto.mauth.ui.screen.export

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.os.PersistableBundle
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.Mauth
import com.xinto.mauth.R
import com.xinto.mauth.domain.account.AccountRepository
import com.xinto.mauth.util.catchMap
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

class ExportViewModel(
    application: Application,

    private val accounts: List<UUID>,
    private val accountRepository: AccountRepository,
) : AndroidViewModel(application) {

    val state = accountRepository.getAccounts()
        .map { accounts ->
            val filteredAccounts = if (this.accounts.isEmpty()) {
                accounts
            } else {
                accounts.filter { this.accounts.contains(it.id) }
            }

            val exportAccounts = filteredAccounts.map {
                with (accountRepository) {
                    it.toExportAccount()
                }
            }
            ExportScreenState.Success(exportAccounts)
        }.catchMap {
            ExportScreenState.Error
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExportScreenState.Loading
        )

    fun copyUrlToClipboard(label: String, url: String) {
        val application = getApplication<Mauth>()
        val clipboardService = application.getSystemService<ClipboardManager>() ?: return
        val clipData = ClipData.newPlainText(label, url).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                description.extras = PersistableBundle().apply {
                    putBoolean("android.content.extra.IS_SENSITIVE", true)
                }
            }
        }
        clipboardService.setPrimaryClip(clipData)
        Toast.makeText(application, R.string.export_url_copy_success, Toast.LENGTH_LONG).show()
    }

}