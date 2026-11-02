package com.example.readoffilepdf.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readoffilepdf.pdf.PdfRendererState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    initialUri: Uri? = null,
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf(initialUri) }
    var pdfState by remember { mutableStateOf<PdfRendererState?>(null) }
    var isLoading by remember { mutableStateOf(value = false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Launcher for selecting PDF documents from storage
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it
        }
    }

    // Effect to open/close PDF file when Uri changes
    DisposableEffect(selectedUri) {
        val uri = selectedUri
        if (uri != null) {
            isLoading = true
            errorMessage = null
            val state = PdfRendererState.create(context, uri)
            if (state != null) {
                pdfState = state
            } else {
                errorMessage = "تعذر فتح ملف الـ PDF. يرجى التأكد من صحة الملف وإعادة المحاولة."
                pdfState = null
            }
            isLoading = false
        } else {
            pdfState?.close()
            pdfState = null
        }

        onDispose {
            pdfState?.close()
            pdfState = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = pdfState?.fileName ?: "قارئ ملفات PDF",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (pdfState != null) {
                            Text(
                                text = "عدد الصفحات: ${pdfState?.pageCount ?: 0}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع للرئيسية",
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedUri == null) "فتح ملف PDF" else "ملف آخر",
                            fontSize = 14.sp,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            val currentErrorMessage = errorMessage
            val currentPdfState = pdfState
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                currentErrorMessage != null -> {
                    ErrorContent(
                        message = currentErrorMessage,
                        onRetry = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                    )
                }

                currentPdfState != null -> {
                    PdfContent(pdfState = currentPdfState)
                }

                else -> {
                    EmptyStateContent(
                        onPickPdf = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateContent(onPickPdf: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "مرحباً بك في قارئ الـ PDF",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "قم باختيار ملف PDF من هاتفك لفتحه واستعراضه بسهولة وسرعة دون الحاجة لأذونات معقدة.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onPickPdf,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "فتح ملف PDF (Pick PDF File)",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("اختيار ملف آخر")
        }
    }
}

@Composable
private fun PdfContent(pdfState: PdfRendererState) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }.toInt()
    val baseTargetWidthPx = (screenWidthPx * 1.5f * zoomLevel).toInt().coerceAtLeast(400)

    // Current page indicator (1-indexed)
    val currentPage by remember {
        derivedStateOf {
            (listState.firstVisibleItemIndex + 1).coerceIn(1, pdfState.pageCount)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(
                count = pdfState.pageCount,
                key = { pageIndex -> pageIndex },
            ) { pageIndex ->
                PdfPageItem(
                    pdfState = pdfState,
                    pageIndex = pageIndex,
                    targetWidthPx = baseTargetWidthPx,
                )
            }
        }

        // Floating Navigation & Control Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                .shadow(8.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Zoom Out Button
                IconButton(
                    onClick = { zoomLevel = (zoomLevel - 0.25f).coerceAtLeast(0.75f) },
                    enabled = zoomLevel > 0.75f,
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "تصغير",
                    )
                }

                // Previous Page Button
                IconButton(
                    onClick = {
                        if (currentPage > 1) {
                            coroutineScope.launch {
                                listState.animateScrollToItem(currentPage - 2)
                            }
                        }
                    },
                    enabled = currentPage > 1,
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "الصفحة السابقة",
                    )
                }

                // Page Number Text
                Text(
                    text = "$currentPage / ${pdfState.pageCount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )

                // Next Page Button
                IconButton(
                    onClick = {
                        if (currentPage < pdfState.pageCount) {
                            coroutineScope.launch {
                                listState.animateScrollToItem(currentPage)
                            }
                        }
                    },
                    enabled = currentPage < pdfState.pageCount,
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "الصفحة التالية",
                    )
                }

                // Zoom In Button
                IconButton(
                    onClick = { zoomLevel = (zoomLevel + 0.25f).coerceAtMost(2.5f) },
                    enabled = zoomLevel < 2.5f,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "تكبير",
                    )
                }
            }
        }
    }
}

@Composable
private fun PdfPageItem(
    pdfState: PdfRendererState,
    pageIndex: Int,
    targetWidthPx: Int,
) {
    var pageBitmap by remember(pdfState, pageIndex, targetWidthPx) {
        mutableStateOf<Bitmap?>(null)
    }
    var isLoading by remember(pdfState, pageIndex, targetWidthPx) {
        mutableStateOf(value = true)
    }

    LaunchedEffect(pdfState, pageIndex, targetWidthPx) {
        isLoading = true
        pageBitmap = pdfState.renderPage(pageIndex, targetWidthPx)
        isLoading = false
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 250.dp),
                contentAlignment = Alignment.Center,
            ) {
                val bitmap = pageBitmap
                if (isLoading || bitmap == null) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(32.dp)
                            .size(36.dp),
                        strokeWidth = 3.dp,
                    )
                } else {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "الصفحة ${pageIndex + 1}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    )
                }
            }

            // Page label footer
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ) {
                Text(
                    text = "صفحة ${pageIndex + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
}
