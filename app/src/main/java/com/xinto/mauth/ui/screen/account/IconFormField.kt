package com.xinto.mauth.ui.screen.account

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.xinto.mauth.R
import com.xinto.mauth.ui.component.UriImage
import com.xinto.mauth.ui.component.form.FormField
import java.io.File
import java.util.UUID

class IconFormField(initial: Uri?) : FormField<Uri?>(initial, 0) {

    @Composable
    override fun invoke(modifier: Modifier) {
        val context = LocalContext.current
        val imageSelectLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = {
                value = makePermanentUri(context, it)
            }
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(96.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                ),
                onClick = {
                    imageSelectLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
            ) {
                if (value != null) {
                    UriImage(uri = value!!)
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            modifier = Modifier.size(36.dp),
                            painter = painterResource(R.drawable.ic_add_a_photo),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }

    private fun makePermanentUri(context: Context, uri: Uri?): Uri? {
        if (uri == null) return null

        try {
            val contentResolver = context.contentResolver
            val bitmap = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, false)
            }

            val destination = File(context.filesDir, "${id}_${UUID.randomUUID()}.png").apply {
                if (exists()) {
                    delete()
                }
                createNewFile()
            }
            destination.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            value?.toFile()?.delete()

            return destination.toUri()
        } catch (e: Exception) {
            return null
        }
    }

}