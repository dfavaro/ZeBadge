package de.berlindroid.zeapp

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.commit451.coiltransformations.CropTransformation
import dagger.hilt.android.AndroidEntryPoint
import de.berlindroid.zeapp.zebits.ditherFloydSteinberg
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zemodels.ZeEditor
import de.berlindroid.zeapp.zemodels.ZeSlot
import de.berlindroid.zeapp.zemodels.ZeTemplateChooser
import de.berlindroid.zeapp.zemodels.ZeToastEvent
import de.berlindroid.zeapp.zeui.BarCodeEditorDialog
import de.berlindroid.zeapp.zeui.BinaryBitmapPageProvider
import de.berlindroid.zeapp.zeui.CustomPhraseEditorDialog
import de.berlindroid.zeapp.zeui.ImageGenerationEditorDialog
import de.berlindroid.zeapp.zeui.NameEditorDialog
import de.berlindroid.zeapp.zeui.PictureEditorDialog
import de.berlindroid.zeapp.zeui.QRCodeEditorDialog
import de.berlindroid.zeapp.zeui.RandomQuotesEditorDialog
import de.berlindroid.zeapp.zeui.WeatherEditorDialog
import de.berlindroid.zeapp.zeui.ZeImageDrawEditorDialog
import de.berlindroid.zeapp.zeui.ZeNavigationPad
import de.berlindroid.zeapp.zeui.zetheme.ZeBadgeAppTheme
import de.berlindroid.zeapp.zevm.ZeBadgeViewModel
import kotlinx.coroutines.launch
import java.io.File
import android.content.res.Configuration as AndroidConfig
import androidx.compose.foundation.Image as ZeImage
import androidx.compose.foundation.layout.Arrangement as ZeArrangement
import androidx.compose.foundation.layout.Column as ZeColumn
import androidx.compose.foundation.layout.Row as ZeRow
import androidx.compose.foundation.layout.Spacer as ZeSpacer
import androidx.compose.foundation.lazy.LazyColumn as ZeLazyColumn
import androidx.compose.foundation.lazy.LazyRow as ZeLazyRow
import androidx.compose.foundation.shape.RoundedCornerShape as ZeRoundedCornerShape
import androidx.compose.material3.AlertDialog as ZeAlertDialog
import androidx.compose.material3.Button as ZeButton
import androidx.compose.material3.Card as ZeCard
import androidx.compose.material3.Icon as ZeIcon
import androidx.compose.material3.IconButton as ZeIconButton
import androidx.compose.material3.LinearProgressIndicator as ZeLinearProgressIndicator
import androidx.compose.material3.Scaffold as ZeScaffold
import androidx.compose.material3.Surface as ZeSurface
import androidx.compose.material3.Text as ZeText
import androidx.compose.material3.TopAppBar as ZeTopAppBar
import androidx.compose.ui.Alignment as ZeAlignment
import androidx.compose.ui.Modifier as ZeModifier
import androidx.compose.ui.graphics.Color as ZeColor
import androidx.compose.ui.graphics.FilterQuality as ZeFilterQuality
import androidx.compose.ui.graphics.painter.BitmapPainter as ZeBitmapPainter
import androidx.compose.ui.layout.ContentScale as ZeContentScale
import de.berlindroid.zeapp.zeui.BadgeSimulator as ZeSimulator
import de.berlindroid.zeapp.zeui.ToolButton as ZeToolButton

/**
 * Main View entrance for the app
 */
@AndroidEntryPoint
class ZeMainActivity : ComponentActivity() {

    private val viewModel: ZeBadgeViewModel by viewModels()

    /**
     * Once created, use the main view composables.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateContent()
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        updateContent()
    }

    private fun updateContent() = setContent {
        ProvideLocalActivity {
            DrawUi(viewModel)
        }
    }
}

@Composable
private fun Activity.ProvideLocalActivity(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalZeActivity provides this, content = content)
}

@Composable
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
private fun DrawUi(viewModel: ZeBadgeViewModel) {
    val activity = LocalZeActivity.current

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect {
            val duration = when (it.duration) {
                ZeToastEvent.Duration.SHORT -> Toast.LENGTH_SHORT
                ZeToastEvent.Duration.LONG -> Toast.LENGTH_LONG
            }

            Toast.makeText(activity, it.message, duration).show()
        }
    }

    val wsc = calculateWindowSizeClass(activity = LocalZeActivity.current)
    if (wsc.widthSizeClass != WindowWidthSizeClass.Expanded) {
        CompactUi(viewModel)
    } else {
        LargeScreenUi(viewModel)
    }
}

@Composable
private fun CompactUi(vm: ZeBadgeViewModel) {
    if (LocalConfiguration.current.orientation == AndroidConfig.ORIENTATION_LANDSCAPE) {
        ZeSimulator(
            page = vm.slotToBitmap(),
            onButtonPressed = vm::simulatorButtonPressed,
        )
    } else {
        ZeScreen(vm)
    }
}

@Composable
private fun LargeScreenUi(vm: ZeBadgeViewModel) {
    ZeRow {
        ZeScreen(vm, modifier = Modifier.weight(.3f))
        Spacer(modifier = Modifier.width(ZeDimen.Two))
        ZeSimulator(
            page = vm.slotToBitmap(),
            onButtonPressed = vm::simulatorButtonPressed,
            modifier = Modifier.weight(.3f)
        )
    }
}

@Composable
private fun ZeScreen(vm: ZeBadgeViewModel, modifier: Modifier = Modifier) {
    val lazyListState = rememberLazyListState()
    var isShowingAbout by remember { mutableStateOf(false) }
    ZeBadgeAppTheme(content = {
        ZeScaffold(
            modifier = modifier,
            floatingActionButton = {
                if (!isShowingAbout) {
                    ZeNavigationPad(lazyListState)
                }
            },
            topBar = {
                ZeTopBar(
                    onRandomClick = vm::sendRandomPageToDevice,
                    onSaveAllClick = vm::saveAll,
                    onAboutClick = { isShowingAbout = !isShowingAbout },
                    isShowingAbout = isShowingAbout,
                )
            },
            content = { paddingValues ->
                if (isShowingAbout) {
                    ZeAbout(paddingValues, vm)
                } else {
                    ZePages(
                        paddingValues = paddingValues,
                        lazyListState = lazyListState,
                        vm = vm,
                    )
                }
            }
        )
    })
}

@Composable
private fun ZeAbout(
    paddingValues: PaddingValues,
    vm: ZeBadgeViewModel,
) {
    val lines by vm.lines.collectAsState()

    ZeSurface(
        modifier = ZeModifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(ZeDimen.Half)
    ) {
        Column {
            ZeText(
                text = "Contributors",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 24.sp,
            )
            ZeLazyColumn {
                items(lines) { line ->
                    ZeText(
                        text = line,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 18.sp,
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ZeTopBar(
    onSaveAllClick: () -> Unit,
    onRandomClick: () -> Unit,
    onAboutClick: () -> Unit,
    isShowingAbout: Boolean,
) {
    ZeTopAppBar(
        title = {
            ZeText(
                style = MaterialTheme.typography.titleLarge,
                text = stringResource(id = R.string.app_name)
            )
        },
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.secondary,
            actionIconContentColor = MaterialTheme.colorScheme.secondary,
            navigationIconContentColor = MaterialTheme.colorScheme.secondary,
        ),
        actions = {
            ZeIconButton(onClick = onSaveAllClick) {
                ZeIcon(
                    painter = painterResource(id = R.drawable.ic_random),
                    contentDescription = "Send random page to badge"
                )
            }
            ZeIconButton(onClick = onRandomClick) {
                ZeIcon(
                    painter = painterResource(id = R.drawable.save_all),
                    contentDescription = null
                )
            }
            ZeIconButton(onClick = onAboutClick) {
                if (isShowingAbout) {
                    ZeIcon(Icons.Default.Close, contentDescription = "About")
                } else {
                    ZeIcon(Icons.Default.Info, contentDescription = "Close About screen")
                }
            }
        }
    )
}

@Composable
private fun ZePages(
    paddingValues: PaddingValues,
    vm: ZeBadgeViewModel,
    lazyListState: LazyListState
) {
    ZeSurface(
        modifier = ZeModifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(ZeDimen.Half)
    ) {
        val editor by remember { vm.currentSlotEditor }
        val templateChooser by remember { vm.currentTemplateChooser }
        val message by remember { vm.message }
        val messageProgress by remember { vm.messageProgress }
        val slots by remember { vm.slots }

        if (editor != null) {
            SelectedEditor(editor!!, vm)
        }

        if (templateChooser != null) {
            TemplateChooserDialog(vm, templateChooser)
        }

        // column surrounding a lazycolumn: so the message stays ontop.
        ZeColumn {
            if (message.isNotEmpty()) {
                InfoBar(message, messageProgress, vm::copyInfoToClipboard)
            }

            ZeLazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(
                    start = ZeDimen.One,
                    end = ZeDimen.One,
                    top = ZeDimen.Half,
                    bottom = 140.dp
                )
            ) {

                items(
                    slots.keys.toList()
                ) { slot  ->
                    var isVisible by remember { mutableStateOf(false) }
                    val alpha: Float by animateFloatAsState(
                        targetValue = if (isVisible) 1f else 0f, label = "alpha",
                        animationSpec = tween(durationMillis = 750)
                    )
                    LaunchedEffect(slot) {
                        isVisible = true
                    }

                    PagePreview(
                        modifier = Modifier.alpha(alpha = alpha),
                        name = slot::class.simpleName ?: "WTF",
                        bitmap = vm.slotToBitmap(slot),
                        customizeThisPage = if (slot.isSponsor) {
                            { vm.customizeSponsorSlot(slot) }
                        } else {
                            { vm.customizeSlot(slot) }
                        },
                        resetThisPage = if (slot.isSponsor) null else {
                            { vm.resetSlot(slot) }
                        },
                        sendToDevice = {
                            vm.sendPageToDevice(slot)
                        }
                    )

                    ZeSpacer(modifier = ZeModifier.height(ZeDimen.One))
                }
            }
        }
    }
}

/**
 * Show a message to the user with a timer ticking down.
 */
@Composable
@Preview
private fun InfoBar(
    message: String = "Very Important",
    progress: Float = 0.5f,
    copyMoreToClipboard: (() -> Unit) = {},
) {
    ZeCard(
        modifier = ZeModifier
            .padding(horizontal = ZeDimen.One, vertical = ZeDimen.One)
            .background(ZeColor.Black, ZeRoundedCornerShape(ZeDimen.One)),
    ) {
        ZeRow(
            modifier = ZeModifier.padding(horizontal = ZeDimen.Two, vertical = ZeDimen.One),
            verticalAlignment = ZeAlignment.CenterVertically
        ) {
            ZeText(
                modifier = ZeModifier.weight(1.0f),
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace,
                color = ZeColor.White,
                text = message,
            )

            ZeIconButton(onClick = copyMoreToClipboard) {
                ZeIcon(
                    painter = painterResource(
                        id = R.drawable.copy_clipboard
                    ),
                    contentDescription = "Copy info bar message"
                )
            }
        }

        ZeLinearProgressIndicator(
            modifier = ZeModifier.fillMaxWidth(),
            progress = progress
        )
    }
}

@Composable
private fun SelectedEditor(
    editor: ZeEditor,
    vm: ZeBadgeViewModel
) {
    if (editor.slot !in listOf(
            ZeSlot.Name,
            ZeSlot.FirstCustom,
            ZeSlot.SecondCustom,
            ZeSlot.QRCode,
            ZeSlot.Weather,
            ZeSlot.Quote,
            ZeSlot.BarCode
        )
    ) {
        Log.e("Slot", "This slot '${editor.slot}' is not supposed to be editable.")
    } else {
        when (val config = editor.config) {
            is ZeConfiguration.Name -> NameEditorDialog(
                config,
                dismissed = { vm.slotConfigured(editor.slot, null) }
            ) { newConfig ->
                vm.slotConfigured(editor.slot, newConfig)
            }

            is ZeConfiguration.Picture -> {
                PictureEditorDialog(
                    dismissed = {
                        vm.slotConfigured(null, null)
                    }
                ) {
                    vm.slotConfigured(editor.slot, it)
                }
            }

            is ZeConfiguration.ImageGen -> {
                ImageGenerationEditorDialog(
                    config.prompt,
                    dismissed = {
                        vm.slotConfigured(null, null)
                    }
                ) {
                    vm.slotConfigured(editor.slot, it)
                }
            }

            is ZeConfiguration.Schedule -> {
                Toast.makeText(
                    LocalZeActivity.current,
                    "Not added by you yet, please feel free to contribute this editor",
                    Toast.LENGTH_LONG
                ).show()

                vm.slotConfigured(null, null)
            }

            is ZeConfiguration.Weather -> {
                WeatherEditorDialog(
                    accepted = { vm.slotConfigured(editor.slot, it) },
                    dismissed = { vm.slotConfigured(null, null) },
                    activity = LocalZeActivity.current,
                    config = config,
                )
            }

            is ZeConfiguration.Quote -> {
                RandomQuotesEditorDialog(
                    accepted = { vm.slotConfigured(editor.slot, it) },
                    dismissed = { vm.slotConfigured(null, null) },
                    activity = LocalZeActivity.current,
                    config = config,
                )
            }

            is ZeConfiguration.QRCode -> QRCodeEditorDialog(
                config,
                dismissed = { vm.slotConfigured(editor.slot, null) }
            ) { newConfig ->
                vm.slotConfigured(editor.slot, newConfig)
            }

            is ZeConfiguration.BarCode -> BarCodeEditorDialog(
                LocalZeActivity.current,
                config,
                dismissed = { vm.slotConfigured(editor.slot, null) }
            ) { newConfig ->
                vm.slotConfigured(editor.slot, newConfig)
            }

            is ZeConfiguration.Kodee -> {
                vm.slotConfigured(editor.slot, config)
            }

            is ZeConfiguration.ImageDraw -> {
                ZeImageDrawEditorDialog(dismissed = {
                    vm.slotConfigured(
                        editor.slot,
                        null
                    )
                }) { newConfig ->
                    vm.slotConfigured(editor.slot, newConfig)
                }
            }

            is ZeConfiguration.Camera -> CameraEditor(editor, config, vm)

            is ZeConfiguration.CustomPhrase -> CustomPhraseEditorDialog(
                config = config,
                dismissed = { vm.slotConfigured(editor.slot, null) }
            ) { newConfig ->
                vm.slotConfigured(editor.slot, newConfig)
            }
        }
    }
}

@Composable
private fun CameraEditor(
    editor: ZeEditor,
    config: ZeConfiguration.Camera,
    vm: ZeBadgeViewModel
) {
    val context = LocalContext.current
    val uri = FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.files",
        File(context.cacheDir, "photo.jpg")
    )
    val coroutineScope = rememberCoroutineScope()
    val takePicture =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { pictureTaken ->
            if (pictureTaken) {
                val imageRequest = ImageRequest.Builder(context)
                    .data(uri)
                    .transformations(CropTransformation())
                    .size(PAGE_WIDTH, PAGE_HEIGHT)
                    .scale(Scale.FIT)
                    .precision(Precision.EXACT)
                    .allowHardware(false)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .build()

                coroutineScope.launch {
                    val drawable =
                        context.imageLoader.execute(imageRequest).drawable as BitmapDrawable
                    val bitmap = Bitmap.createBitmap(
                        PAGE_WIDTH,
                        PAGE_HEIGHT,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawColor(Color.WHITE)
                    canvas.drawBitmap(
                        drawable.bitmap,
                        (PAGE_WIDTH / 2f) - (drawable.bitmap.width / 2f),
                        0f,
                        null
                    )
                    vm.slotConfigured(
                        editor.slot,
                        config.copy(bitmap = bitmap.ditherFloydSteinberg())
                    )
                }
            } else {
                vm.slotConfigured(editor.slot, null)
            }
        }

    SideEffect {
        takePicture.launch(uri)
    }
}

@Composable
private fun TemplateChooserDialog(
    vm: ZeBadgeViewModel,
    templateChooser: ZeTemplateChooser?
) {
    ZeAlertDialog(
        onDismissRequest = {
            vm.templateSelected(null, null)
        },
        confirmButton = {
            ZeButton(onClick = { vm.templateSelected(null, null) }) {
                ZeText(text = stringResource(id = android.R.string.ok))
            }
        },
        title = {
            ZeText(text = "Select Content")
        },
        text = {
            ZeLazyColumn {
                items(templateChooser?.configurations.orEmpty()) { config ->
                    ZeButton(
                        modifier = ZeModifier.fillMaxWidth(),
                        onClick = {
                            vm.templateSelected(templateChooser?.slot, config)
                        },
                    ) {
                        ZeText(text = config.humanTitle)
                    }
                }
            }
        }
    )
}

@Composable
private fun PagePreview(
    modifier: ZeModifier = ZeModifier,
    @PreviewParameter(BinaryBitmapPageProvider::class, 1)
    bitmap: Bitmap,
    name: String,
    customizeThisPage: (() -> Unit)? = null,
    resetThisPage: (() -> Unit)? = null,
    sendToDevice: (() -> Unit)? = null,
) {
    ZeCard(
        modifier = modifier
            .background(ZeColor.Black, ZeRoundedCornerShape(ZeDimen.One))
            .padding(ZeDimen.Quarter),
    ) {
        ZeImage(
            modifier = ZeModifier
                .fillMaxWidth()
                .wrapContentHeight(unbounded = true)
                .padding(horizontal = ZeDimen.One, vertical = ZeDimen.Half),
            painter = ZeBitmapPainter(
                image = bitmap.asImageBitmap(),
                filterQuality = ZeFilterQuality.None,
            ),
            contentScale = ZeContentScale.FillWidth,
            contentDescription = null,
        )


        ZeRow {
            ZeText(
                text = name,
                modifier = Modifier
                    .align(ZeAlignment.CenterVertically)
                    .padding(start = 8.dp),
                color = ZeColor.Black,
            )
            if (resetThisPage != null || customizeThisPage != null || sendToDevice != null) {
                ZeLazyRow(
                    modifier = ZeModifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = ZeDimen.Quarter),
                    horizontalArrangement = ZeArrangement.End
                ) {
                    if (sendToDevice != null) {
                        item {
                            ZeToolButton(
                                imageVector = Icons.Filled.Send,
                                text = "Send",
                                onClick = sendToDevice,
                            )
                        }
                    }
                    if (resetThisPage != null) {
                        item {
                            ZeToolButton(
                                imageVector = Icons.Filled.Refresh,
                                text = "Reset",
                                onClick = resetThisPage,
                            )
                        }
                    }
                    if (customizeThisPage != null) {
                        item {
                            ZeToolButton(
                                imageVector = Icons.Filled.Edit,
                                text = "Edit",
                                onClick = customizeThisPage,
                            )
                        }
                    }
                }
            }
        }
    }
}

private val ZeSlot.isSponsor: Boolean
    get() = this is ZeSlot.FirstSponsor || this is ZeSlot.SecondSponsor
