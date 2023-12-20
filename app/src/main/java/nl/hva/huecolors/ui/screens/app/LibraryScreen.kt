package nl.hva.huecolors.ui.screens.app

import android.Manifest
import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Scale
import kotlinx.coroutines.launch
import nl.hva.huecolors.ui.components.HueButton
import nl.hva.huecolors.ui.components.HueHeader
import nl.hva.huecolors.ui.components.HueInfoCard
import nl.hva.huecolors.utils.Utils
import nl.hva.huecolors.viewmodel.LightViewModel

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavHostController, viewModel: LightViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val images by viewModel.images.observeAsState()
    val coroutineScope = rememberCoroutineScope()
    var showBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }
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
        } else {
            // TODO: HANDLE PERMISSION DENIAL
        }
    }

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> {
                val isGranted =
                    Utils.checkPermissions(context, arrayOf(Manifest.permission.READ_MEDIA_IMAGES))

                if (isGranted) {
                    coroutineScope.launch {
                        viewModel.getImagesFromMedia(context)
                    }
                } else {
                    launcher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
                }
            }
            Lifecycle.State.DESTROYED -> {
                viewModel.clearImages()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(48.dp),
            columns = GridCells.Fixed(2),
            content = {
                item(span = { GridItemSpan(2) }) { }
                item(span = { GridItemSpan(2) }) {
                    HueHeader(text = "Library")
                }
                item(span = { GridItemSpan(2) }) {
                    HueInfoCard(
                        headline = "What is this?",
                        body = "The Library lets you extract vibrant color palettes from your phone's images and applies them to Philips Hue lights, instantly transforming your space into a personalized and dynamic environment."
                    )
                }
                if (images?.data != null) {
                    items(images?.data!!) { image ->
                        ImageItem(
                            context = context,
                            image = image
                        ) {
                            showBottomSheet = true
                            selectedImage = image
                        }
                    }
                }
            })

    }

    PaletteDrawer(onDismiss = {
        showBottomSheet = false
    }, image = selectedImage, onApply = { palette ->
        coroutineScope.launch {
            viewModel.initShade()
            viewModel.paletteToLights(palette)
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                showBottomSheet = false
            }


        }
    }, isVisible = showBottomSheet, sheetState = sheetState, context = context)
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
    context: Context
) {
    val source = image?.let { ImageDecoder.createSource(context.contentResolver, it) }
    val bitmap = source?.let { ImageDecoder.decodeBitmap(it).asShared() }
    val palette = bitmap?.let { Utils.getPalette(it, 5) }
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss, sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.padding(48.dp),
                verticalArrangement = Arrangement.spacedBy(48.dp)
            ) {
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
                                .height(40.dp)
                                .width(40.dp)
                                .clip(CircleShape)
                                .background(color = Color(color.rgb))
                        )
                    }
                }
                HueButton(text = "Apply", onClick = { onApply(palette) })
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
                .size(500, 500)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = "Test"
        )
    }
}