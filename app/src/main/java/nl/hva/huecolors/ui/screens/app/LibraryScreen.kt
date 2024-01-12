package nl.hva.huecolors.ui.screens.app

import android.Manifest
import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.NavHostController
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Scale
import kotlinx.coroutines.launch
import nl.hva.huecolors.R
import nl.hva.huecolors.data.Resource
import nl.hva.huecolors.ui.components.HueButton
import nl.hva.huecolors.ui.components.HueHeader
import nl.hva.huecolors.ui.components.HueInfoCard
import nl.hva.huecolors.ui.components.HueSubHeader
import nl.hva.huecolors.utils.Utils
import nl.hva.huecolors.viewmodel.LightViewModel

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navController: NavHostController,
    viewModel: LightViewModel,
    padding: PaddingValues
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val images by viewModel.images.observeAsState()
    val coroutineScope = rememberCoroutineScope()
    var loadingState by remember { mutableStateOf(false) }
    var showBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }
    val permissions = arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    val sheetState = rememberModalBottomSheetState()
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val areGranted = permissionsMap.values.all { it }
        if (areGranted) {
            coroutineScope.launch {
                viewModel.getImagesFromMedia(context)
            }
        }
    }
    val isGranted =
        Utils.checkPermissions(context, permissions)

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> {
                if (isGranted) {
                    coroutineScope.launch {
                        viewModel.getImagesFromMedia(context)
                    }
                } else {
                    launcher.launch(permissions)
                }
            }

            else -> {
                viewModel.clearImages()
            }
        }
    }

    DisposableEffect(Unit) {
        val observer = Observer<String?> { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.toastMessage.observe(lifecycleOwner, observer)

        onDispose {
            viewModel.clearImages()
            viewModel.toastMessage.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        LazyVerticalGrid(contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(48.dp),
            columns = GridCells.Fixed(2),
            content = {
                item(span = { GridItemSpan(2) }) { }
                item(span = { GridItemSpan(2) }) {
                    HueHeader(text = stringResource(R.string.library))
                }
                item(span = { GridItemSpan(2) }) {
                    HueInfoCard(
                        headline = stringResource(id = R.string.what_is_this),
                        body = stringResource(R.string.library_description)
                    )
                }
                when (images) {
                    is Resource.Success -> {
                        items(images?.data!!, key = { it }) { image ->
                            ImageItem(
                                context = context,
                                image = image
                            ) {
                                showBottomSheet = true
                                selectedImage = image
                            }
                        }
                    }

                    is Resource.Error -> {
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                text = stringResource(R.string.something_happened),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    else -> {
                        item(span = { GridItemSpan(2) }) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .height(2.dp)
                                    .fillMaxWidth(),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                item(span = { GridItemSpan(2) }) { }
            })

    }

    PaletteDrawer(
        onDismiss = {
            showBottomSheet = false
        },
        loadingState = loadingState,
        image = selectedImage,
        onApply = { palette ->
            coroutineScope.launch {
                loadingState = true
                viewModel.paletteToLights(palette)
                sheetState.hide()
            }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    showBottomSheet = false
                    loadingState = false
                }


            }
        },
        isVisible = showBottomSheet,
        sheetState = sheetState,
        context = context
    )
}

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaletteDrawer(
    onDismiss: () -> Unit,
    image: Uri?,
    onApply: (Palette?) -> Unit,
    isVisible: Boolean,
    sheetState: SheetState,
    context: Context,
    loadingState: Boolean
) {
    val source = image?.let { ImageDecoder.createSource(context.contentResolver, it) }
    val bitmap = source?.let { ImageDecoder.decodeBitmap(it).asShared() }
    val palette = bitmap?.let { Utils.getPalette(it, 6) }
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss, sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.padding(48.dp),
                verticalArrangement = Arrangement.spacedBy(48.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HueSubHeader(text = stringResource(R.string.swatches))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        palette?.swatches?.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .shadow(
                                        elevation = 4.dp,
                                        spotColor = Color.Black,
                                        shape = CircleShape,
                                        clip = true
                                    )
                                    .border(
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.primary.copy(0.3F)
                                        ),
                                        shape = CircleShape
                                    )
                                    .height(36.dp)
                                    .width(36.dp)
                                    .clip(CircleShape)
                                    .background(color = Color(color.rgb))
                            )
                        }
                    }
                }
                HueButton(
                    secondary = true,
                    text = stringResource(R.string.apply),
                    onClick = { onApply(palette) },
                    loading = loadingState
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun ImageItem(context: Context, image: Uri?, onClick: () -> Unit) {
    Card(onClick = { onClick() }) {
        AsyncImage(
            modifier = Modifier.aspectRatio(1F / 1.3F),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            model = ImageRequest.Builder(context)
                .data(image)
                .crossfade(true)
                .scale(Scale.FIT)
                .size(500, 300)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = "Test"
        )
    }
}