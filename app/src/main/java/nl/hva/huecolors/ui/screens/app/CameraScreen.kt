package nl.hva.huecolors.ui.screens.app

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.NavHostController
import androidx.palette.graphics.Palette
import kotlinx.coroutines.launch
import nl.hva.huecolors.R
import nl.hva.huecolors.ui.components.HueButton
import nl.hva.huecolors.utils.Utils
import nl.hva.huecolors.viewmodel.CameraViewModel
import nl.hva.huecolors.viewmodel.LightViewModel

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    navController: NavHostController,
    viewModel: CameraViewModel,
    padding: PaddingValues
) {
    val coroutineScope = rememberCoroutineScope()
    val permissions = arrayOf(
        Manifest.permission.CAMERA
    )
    val lightViewModel: LightViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    var loadingState by remember { mutableStateOf<Pair<Boolean, Boolean>>(false to false) }
    val cameraController = remember { LifecycleCameraController(context) }
    val capturedImage by viewModel.capturedImage.observeAsState()
    var showBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val areGranted = permissionsMap.values.all { it }
    }
    val isGranted = Utils.checkPermissions(context, permissions)

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.STARTED, Lifecycle.State.RESUMED -> {
                if (!isGranted) {
                    launcher.launch(permissions)
                }
            }

            else -> {
                cameraController.unbind()
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
        lightViewModel.toastMessage.observe(lifecycleOwner, observer)

        onDispose {
            viewModel.toastMessage.removeObserver(observer)
            lightViewModel.toastMessage.removeObserver(observer)
        }
    }

    Crossfade(
        targetState = isGranted,
        label = "",
        modifier = Modifier.fillMaxSize()
    ) { granted ->
        if (granted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = padding.calculateStartPadding(LayoutDirection.Ltr),
                        end = padding.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = padding.calculateBottomPadding()
                    )
            ) {

                AndroidView(modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp), factory = { ctx ->
                    PreviewView(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_START
                    }.also { previewView ->
                        previewView.controller = cameraController
                        cameraController.bindToLifecycle(lifecycleOwner)
                    }
                })

                FloatingActionButton(
                    onClick = {
                        val mainExecutor = ContextCompat.getMainExecutor(context)
                        cameraController.takePicture(mainExecutor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    viewModel.captureImage(image.toBitmap())
                                    showBottomSheet = true
                                }
                            })
                    },
                    modifier = Modifier
                        .size(124.dp)
                        .padding(24.dp)
                        .align(Alignment.BottomCenter),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = "Camera",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }


            CameraDrawer(
                onDismiss = {
                    showBottomSheet = false
                },
                image = capturedImage,
                onApply = { palette ->
                    coroutineScope.launch {
                        loadingState = loadingState.copy(false, true)
                        lightViewModel.paletteToLights(palette)
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                            loadingState = loadingState.copy(false, false)
                        }
                    }
                },
                loadingState = loadingState,
                onSaveApply = { palette ->
                    coroutineScope.launch {
                        loadingState = loadingState.copy(true)
                        lightViewModel.paletteToLights(palette)
                        viewModel.saveToStorage(capturedImage, context)
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                            loadingState = loadingState.copy(false)
                        }
                    }
                },
                isVisible = showBottomSheet,
                sheetState = sheetState,
                context = context
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                HueButton(text = stringResource(R.string.allow_camera_permission), onClick = {
                    launcher.launch(permissions)
                })
            }
        }
    }

}

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraDrawer(
    onDismiss: () -> Unit,
    image: Bitmap?,
    onApply: (Palette?) -> Unit,
    onSaveApply: (Palette?) -> Unit,
    isVisible: Boolean,
    sheetState: SheetState,
    context: Context,
    loadingState: Pair<Boolean, Boolean>
) {
    val (saveApply, apply) = loadingState
    val palette = image?.let { Utils.getPalette(it, 6) }
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss, sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
//                AsyncImage(
//                    modifier = Modifier
//                        .aspectRatio(1F / 1F)
//                        .clip(shape = RoundedCornerShape(8.dp)),
//                    contentScale = ContentScale.Crop,
//                    alignment = Alignment.Center,
//                    model = ImageRequest.Builder(context)
//                        .data(image)
//                        .crossfade(true)
//                        .scale(Scale.FIT)
//                        .size(500, 500)
//                        .memoryCachePolicy(CachePolicy.ENABLED)
//                        .build(),
//                    contentDescription = "Test"
//                )

//                Spacer(modifier = Modifier.size(48.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
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
                                        color = MaterialTheme.colorScheme.inverseSurface
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

                Spacer(modifier = Modifier.size(48.dp))

                HueButton(
                    text = stringResource(R.string.save_and_apply),
                    onClick = { onSaveApply(palette) },
                    secondary = true,
                    loading = saveApply
                )

                Spacer(modifier = Modifier.size(8.dp))

                HueButton(
                    text = stringResource(id = R.string.apply),
                    onClick = { onApply(palette) },
                    loading = apply,
                    secondary = true
                )
            }
        }
    }
}