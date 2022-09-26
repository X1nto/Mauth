package com.xinto.mauth.contracts

import android.content.Context
import android.content.Intent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

class PickVisualMediaPersistent : ActivityResultContracts.PickVisualMedia() {

    override fun createIntent(context: Context, input: PickVisualMediaRequest): Intent {
        return super.createIntent(context, input).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

}