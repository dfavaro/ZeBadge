@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package de.berlindroid.zeapp.zeui

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import de.berlindroid.zeapp.LocalZeActivity
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zebits.isBinary
import de.berlindroid.zeapp.zebits.qrComposableToBitmap
import de.berlindroid.zeapp.zemodels.ZeConfiguration

/**
 * Editor dialog for changing the name of the participant badge.
 *
 * @param config configuration of the slot, containing details to be displayed
 * @param dismissed callback called when dialog is dismissed / cancelled
 * @param accepted callback called with the new configuration configured.
 */
@Composable
fun QRCodeEditorDialog(
    config: ZeConfiguration.QRCode,
    dismissed: () -> Unit = {},
    accepted: (config: ZeConfiguration.QRCode) -> Unit
) {
    var title by remember { mutableStateOf(config.title) }
    var text by remember { mutableStateOf(config.text) }
    var url by remember { mutableStateOf(config.url) }
    var image by remember { mutableStateOf(config.bitmap) }
    val activity = LocalZeActivity.current

    fun redrawComposableImage() {
        qrComposableToBitmap(
            activity = activity,
            title = title,
            text = text,
            url = url,
        ) {
            image = it
        }
    }

    AlertDialog(
        onDismissRequest = dismissed,
        confirmButton = {
            Button(
                onClick = {
                    if (image.isBinary()) {
                        accepted(ZeConfiguration.QRCode(title, text, url, image))
                    } else {
                        Toast.makeText(
                            activity, R.string.image_needed,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        title = { Text(text = stringResource(id = R.string.add_qr_url)) },
        properties = DialogProperties(),
        text = {
            LazyColumn {
                item {
                    BinaryImageEditor(
                        bitmap = image,
                        bitmapUpdated = { image = it }
                    )
                }

                item {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = title,
                        maxLines = 1,
                        label = { Text(text = stringResource(id = R.string.qr_code_title)) },
                        onValueChange = { newValue ->
                            title = newValue
                            redrawComposableImage()
                        }
                    )
                }

                item {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = text,
                        maxLines = 1,
                        label = { Text(text = stringResource(id = R.string.qr_code_text)) },
                        onValueChange = { newValue ->
                            text = newValue
                            redrawComposableImage()
                        }
                    )
                }

                item {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = url,
                        maxLines = 1,
                        label = { Text(text = stringResource(id = R.string.url)) },
                        onValueChange = { newValue ->
                            url = newValue
                            redrawComposableImage()
                        }
                    )
                }
            }
        }
    )
}
