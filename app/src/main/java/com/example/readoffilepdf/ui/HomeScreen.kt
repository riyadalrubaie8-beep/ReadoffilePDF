package com.example.readoffilepdf.ui

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Hexagon
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readoffilepdf.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DocType(val label: String, val color: Color) {
    ALL("الكل", RedPrimary),
    PDF("PDF", RedPrimary),
    WORD("Word", BlueWord),
    EXCEL("Excel", GreenExcel),
    PPT("PPT", OrangePPT)
}

data class DocumentItem(
    val id: String,
    val title: String,
    val size: String,
    val date: String,
    val type: DocType,
    val uri: Uri? = null,
)

@Composable
fun HomeScreen(
    onOpenPdfUri: (Uri) -> Unit,
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(DocType.PPT) }
    var bottomNavSelected by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(value = false) }

    // System File Pickers
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        uri?.let { onOpenPdfUri(it) }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { onOpenPdfUri(it) }
    }

    // Load sample documents matching screenshot + local scanned files
    val documentList = remember(context) {
        getSampleAndLocalDocuments(context)
    }

    val filteredList = remember(documentList, selectedTab, searchQuery) {
        documentList.filter { doc ->
            val matchesTab = when (selectedTab) {
                DocType.ALL -> true
                else -> doc.type == selectedTab
            }
            val matchesSearch = searchQuery.isEmpty() || doc.title.contains(searchQuery, ignoreCase = true)
            matchesTab && matchesSearch
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                HomeTopAppBar(
                    isSearchActive = isSearchActive,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onToggleSearch = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) searchQuery = ""
                    },
                )
            },
            bottomBar = {
                HomeBottomNavigationBar(
                    selectedTab = bottomNavSelected,
                    onTabSelected = { bottomNavSelected = it },
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf", "*/*")) },
                    containerColor = RedPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 4.dp, start = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إضافة ملف",
                        modifier = Modifier.size(28.dp),
                    )
                }
            },
            containerColor = BackgroundColor,
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                when (bottomNavSelected) {
                    // Tab 0: كل الملفات (Main Dashboard)
                    0 -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // 1. Top Quick Action Cards Row (Import, Image to PDF, Folder)
                            QuickActionCardsRow(
                                onImportClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                                onImageToPdfClick = { imagePickerLauncher.launch("image/*") },
                                onFolderClick = { pdfPickerLauncher.launch(arrayOf("*/*")) },
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 2. Category Filter Tabs Row (الكل, PDF, Word, Excel, PPT)
                            CategoryTabsRow(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it },
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 3. Document Items List View / Empty state
                            if (filteredList.isEmpty()) {
                                EmptyTabContent(message = "ليس لديك أي ملفات ${selectedTab.label} حتى الآن")
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    items(
                                        items = filteredList,
                                        key = { doc -> doc.id },
                                    ) { doc ->
                                        DocumentListItem(
                                            doc = doc,
                                            onClick = {
                                                if (doc.uri != null) {
                                                    onOpenPdfUri(doc.uri)
                                                } else {
                                                    pdfPickerLauncher.launch(arrayOf("application/pdf"))
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Tab 1: الأخيرة (Recent)
                    1 -> {
                        EmptyTabContent(message = "ليس لديك أي ملفات مؤخراً حتى الآن")
                    }

                    // Tab 2: العلامات المرجعية (Bookmarks)
                    2 -> {
                        EmptyTabContent(message = "ليس لديك أي ملفات في العلامات المرجعية")
                    }

                    // Tab 3: الأدوات (Tools Dashboard)
                    3 -> {
                        ToolsScreen(
                            onToolClick = { action ->
                                when (action) {
                                    "img_to_pdf" -> imagePickerLauncher.launch("image/*")
                                    "import_file" -> pdfPickerLauncher.launch(arrayOf("application/pdf"))
                                    else -> pdfPickerLauncher.launch(arrayOf("application/pdf", "*/*"))
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTabContent(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier.size(140.dp),
            shape = CircleShape,
            color = Color(0xFFE3F2FD).copy(alpha = 0.5f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(72.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
            ),
            color = GrayText,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HomeTopAppBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BackgroundColor,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (isSearchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("ابحث عن اسم الملف...") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = onToggleSearch) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق البحث")
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                    )
                } else {
                    // Right Side Title in RTL: "All PDF Reader"
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "All PDF ",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                            ),
                            color = DarkText,
                        )
                        Text(
                            text = "Reader",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                            ),
                            color = RedPrimary,
                        )
                    }

                    // Left Side Header Icons: Crown, Search, Sort, Select, Settings
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        // Crown VIP Icon
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "VIP",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        // Search Icon
                        IconButton(onClick = onToggleSearch) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "بحث",
                                tint = DarkText,
                            )
                        }

                        // Sort Icon
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "ترتيب",
                                tint = DarkText,
                            )
                        }

                        // Checklist Icon
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = "تحديد",
                                tint = DarkText,
                            )
                        }

                        // Settings/Hexagon Icon
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.Hexagon,
                                contentDescription = "خيارات",
                                tint = DarkText,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCardsRow(
    onImportClick: () -> Unit,
    onImageToPdfClick: () -> Unit,
    onFolderClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Card 1: استيراد الملفات
        QuickActionCard(
            title = "استيراد الملفات",
            icon = Icons.AutoMirrored.Filled.Input,
            iconTint = Color(0xFF1976D2),
            iconBgColor = Color(0xFFE3F2FD),
            modifier = Modifier.weight(1f),
            onClick = onImportClick,
        )

        // Card 2: صورة إلى PDF
        QuickActionCard(
            title = "صورة إلى PDF",
            icon = Icons.Default.Image,
            iconTint = Color(0xFFE53935),
            iconBgColor = Color(0xFFFFEBEE),
            modifier = Modifier.weight(1f),
            onClick = onImageToPdfClick,
        )

        // Card 3: مجلد
        QuickActionCard(
            title = "مجلد",
            icon = Icons.Default.Folder,
            iconTint = Color(0xFFFFA000),
            iconBgColor = Color(0xFFFFF3E0),
            modifier = Modifier.weight(1f),
            onClick = onFolderClick,
        )
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    iconBgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = CardBg,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = iconBgColor,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                ),
                color = DarkText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CategoryTabsRow(
    selectedTab: DocType,
    onTabSelected: (DocType) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DocType.entries.reversed().forEach { tab ->
            val isSelected = tab == selectedTab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 16.sp,
                    ),
                    color = if (isSelected) OrangePPT else GrayText,
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Indicator Line
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isSelected) OrangePPT else Color.Transparent),
                )
            }
        }
    }
}

@Composable
private fun DocumentListItem(
    doc: DocumentItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Three dots action menu on left
        IconButton(
            onClick = { },
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "خيارات",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Title and Subtitle Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = doc.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                ),
                color = DarkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${doc.size}  •  ${doc.date}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                ),
                color = GrayText,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Document File Format Icon Badge
        DocumentFormatBadge(type = doc.type)
    }
}

@Composable
private fun DocumentFormatBadge(type: DocType) {
    Surface(
        modifier = Modifier.size(width = 44.dp, height = 50.dp),
        shape = RoundedCornerShape(8.dp),
        color = GrayLight,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            // Folded corner document icon effect
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = Color.LightGray.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxSize(),
            )

            // Format Tag Badge (PPT, PDF, DOC, XLS)
            Surface(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .wrapContentSize(),
                shape = RoundedCornerShape(4.dp),
                color = type.color,
            ) {
                Text(
                    text = type.name,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun HomeBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.White,
        shadowElevation = 16.dp,
        tonalElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Tab 0: كل الملفات (All Files)
            BottomNavItem(
                title = "كل الملفات",
                icon = Icons.Default.Home,
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
            )

            // Tab 1: الأخيرة (Recent)
            BottomNavItem(
                title = "الأخيرة",
                icon = Icons.Default.Schedule,
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
            )

            // Tab 2: العلامات المرجعية (Bookmarks)
            BottomNavItem(
                title = "العلامات المرج...",
                icon = Icons.Default.BookmarkBorder,
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) },
            )

            // Tab 3: الأدوات (Tools)
            BottomNavItem(
                title = "الأدوات",
                icon = Icons.Default.GridView,
                isSelected = selectedTab == 3,
                onClick = { onTabSelected(3) },
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val activeColor = RedPrimary
    val inactiveColor = GrayText

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(26.dp),
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp,
            ),
            color = if (isSelected) activeColor else inactiveColor,
            maxLines = 1,
        )
    }
}

private fun getSampleAndLocalDocuments(context: Context): List<DocumentItem> {
    val list = mutableListOf<DocumentItem>()

    // 1. Files directly matching the user's screenshot
    list.add(DocumentItem("1", "Demo PPT.pptx", "1,4 MB", "11/02/2026", DocType.PPT))
    list.add(DocumentItem("2", "Lecture 2- Jetpack Compose Fundamentals.pptx", "1,7 MB", "10/12/2026", DocType.PPT))
    list.add(DocumentItem("3", "Data_Structure_Lecture01.pptx", "627,6 KB", "00/9/13/2026", DocType.PPT))
    list.add(DocumentItem("4", "chinese_02.pptx", "1,7 MB", "08/04/2026", DocType.PPT))
    list.add(DocumentItem("5", "chinese 01.pptx", "8,3 MB", "08/03/2026", DocType.PPT))
    list.add(DocumentItem("6", "Detection-of-Email-Phishing-Attacks-Report.pdf", "2,1 MB", "15/01/2026", DocType.PDF))
    list.add(DocumentItem("7", "Android_Development_Architecture.pdf", "3,4 MB", "05/01/2026", DocType.PDF))
    list.add(DocumentItem("8", "Project_Financial_Report.xlsx", "450 KB", "20/12/2025", DocType.EXCEL))
    list.add(DocumentItem("9", "Research_Paper_Draft.docx", "890 KB", "18/12/2025", DocType.WORD))

    // 2. Query actual local PDF files from device storage
    try {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
        )
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.pdf'"
        val selectionArgs = arrayOf("application/pdf")
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

        val cursor = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            sortOrder,
        )

        cursor?.use { c ->
            val idColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dateColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)

            while (c.moveToNext()) {
                val id = c.getLong(idColumn)
                val name = c.getString(nameColumn) ?: "Document.pdf"
                val size = c.getLong(sizeColumn)
                val dateSec = c.getLong(dateColumn)
                val uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)

                val sizeText = formatFileSize(size)
                val dateText = formatDate(dateSec)
                val docType = if (name.endsWith(".pptx", true) || name.endsWith(".ppt", true)) {
                    DocType.PPT
                } else if (name.endsWith(".docx", true) || name.endsWith(".doc", true)) {
                    DocType.WORD
                } else if (name.endsWith(".xlsx", true) || name.endsWith(".xls", true)) {
                    DocType.EXCEL
                } else {
                    DocType.PDF
                }

                list.add(0, DocumentItem("local_$id", name, sizeText, dateText, docType, uri))
            }
        }
    } catch (_: Exception) {
    }

    return list
}

private fun formatFileSize(sizeInBytes: Long): String {
    if (sizeInBytes <= 0) return "0 B"
    val kb = sizeInBytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format(Locale.getDefault(), "%.1f MB", mb)
    } else {
        String.format(Locale.getDefault(), "%.1f KB", kb)
    }
}

private fun formatDate(seconds: Long): String {
    if (seconds <= 0) return "10/02/2026"
    val date = Date(seconds * 1000L)
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(date)
}
