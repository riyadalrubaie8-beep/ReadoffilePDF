package com.example.readoffilepdf.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.BrandingWatermark
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readoffilepdf.ui.theme.*

data class ToolItem(
    val title: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBgColor: Color,
    val onClickAction: String = "",
)

@Composable
fun ToolsScreen(
    onToolClick: (String) -> Unit,
) {
    val aiTools = listOf(
        ToolItem("مساعد الذكاء الاصطناعي", Icons.Default.SmartToy, Color(0xFF3F51B5), Color(0xFFE8EAF6), "ai_assistant"),
        ToolItem("لَخِّص", Icons.Default.AutoAwesome, Color(0xFF7E57C2), Color(0xFFEDE7F6), "summarize"),
        ToolItem("ملخص", Icons.Default.Description, Color(0xFF0288D1), Color(0xFFE0F7FA), "summary"),
        ToolItem("ترجمة PDF", Icons.Default.Translate, Color(0xFF1E88E5), Color(0xFFE3F2FD), "translate"),
    )

    val conversionTools = listOf(
        ToolItem("صورة إلى PDF", Icons.Default.Image, Color(0xFFE53935), Color(0xFFFFEBEE), "img_to_pdf"),
        ToolItem("المسح للحفظ بصيغة PDF", Icons.Default.QrCodeScanner, Color(0xFF00ACC1), Color(0xFFE0F7FA), "scan_pdf"),
        ToolItem("بطاقة الهوية", Icons.Default.Badge, Color(0xFFFB8C00), Color(0xFFFFF3E0), "id_card"),
        ToolItem("Word إلى PDF", Icons.AutoMirrored.Filled.Article, Color(0xFF1E88E5), Color(0xFFE3F2FD), "word_to_pdf"),
        ToolItem("PDF إلى Word", Icons.Default.Description, Color(0xFF5E35B1), Color(0xFFEDE7F6), "pdf_to_word"),
        ToolItem("PDF إلى PPT", Icons.Default.Slideshow, Color(0xFFF57C00), Color(0xFFFFF3E0), "pdf_to_ppt"),
        ToolItem("إلى صورة PDF", Icons.Default.Collections, Color(0xFFE53935), Color(0xFFFFEBEE), "pdf_to_img"),
        ToolItem("إلى صورة PDF طويلة", Icons.Default.InsertPhoto, Color(0xFFFB8C00), Color(0xFFFFF3E0), "pdf_to_long_img"),
        ToolItem("استخراج النص", Icons.Default.DocumentScanner, Color(0xFF43A047), Color(0xFFE8F5E9), "ocr_text"),
    )

    val editTools = listOf(
        ToolItem("تحرير النص", Icons.Default.Edit, Color(0xFF1E88E5), Color(0xFFE3F2FD), "edit_text"),
        ToolItem("أضف ملاحظة", Icons.AutoMirrored.Filled.NoteAdd, Color(0xFF8E24AA), Color(0xFFF3E5F5), "add_note"),
        ToolItem("وقّع", Icons.Default.Draw, Color(0xFF43A047), Color(0xFFE8F5E9), "sign_pdf"),
        ToolItem("دمج ملفات PDF", Icons.AutoMirrored.Filled.MergeType, Color(0xFFFB8C00), Color(0xFFFFF3E0), "merge_pdf"),
        ToolItem("تقسيم ملفات PDF", Icons.AutoMirrored.Filled.CallSplit, Color(0xFFE53935), Color(0xFFFFEBEE), "split_pdf"),
        ToolItem("ضغط PDF", Icons.Default.Compress, Color(0xFF00ACC1), Color(0xFFE0F7FA), "compress_pdf"),
        ToolItem("إضافة نص", Icons.Default.TextFields, Color(0xFF009688), Color(0xFFE0F2F1), "add_text"),
        ToolItem("إضافة علامة مائية", Icons.AutoMirrored.Filled.BrandingWatermark, Color(0xFF7E57C2), Color(0xFFEDE7F6), "watermark"),
    )

    val manageTools = listOf(
        ToolItem("استيراد الملفات", Icons.Default.FolderOpen, Color(0xFF1E88E5), Color(0xFFE3F2FD), "import_file"),
        ToolItem("إنشاء ملف PDF", Icons.Default.AddCard, Color(0xFFE53935), Color(0xFFFFEBEE), "create_pdf"),
        ToolItem("طباعة PDF", Icons.Default.Print, Color(0xFFFB8C00), Color(0xFFFFF3E0), "print_pdf"),
        ToolItem("قفل ملف PDF", Icons.Default.Lock, Color(0xFF00ACC1), Color(0xFFE0F7FA), "lock_pdf"),
        ToolItem("فتح قفل ملف PDF", Icons.Default.LockOpen, Color(0xFF7E57C2), Color(0xFFEDE7F6), "unlock_pdf"),
        ToolItem("سلة المهملات", Icons.Default.Delete, Color(0xFF43A047), Color(0xFFE8F5E9), "trash"),
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Section 1: أدوات الذكاء الاصطناعي
        item(span = { GridItemSpan(4) }) {
            ToolsSectionHeader(title = "أدوات الذكاء الاصطناعي")
        }
        items(aiTools.size) { index ->
            ToolGridCard(tool = aiTools[index], onClick = { onToolClick(aiTools[index].onClickAction) })
        }

        // Section 2: تحويل
        item(span = { GridItemSpan(4) }) {
            ToolsSectionHeader(title = "تحويل")
        }
        items(conversionTools.size) { index ->
            ToolGridCard(tool = conversionTools[index], onClick = { onToolClick(conversionTools[index].onClickAction) })
        }

        // Section 3: تعديل
        item(span = { GridItemSpan(4) }) {
            ToolsSectionHeader(title = "تعديل")
        }
        items(editTools.size) { index ->
            ToolGridCard(tool = editTools[index], onClick = { onToolClick(editTools[index].onClickAction) })
        }

        // Section 4: إدارة
        item(span = { GridItemSpan(4) }) {
            ToolsSectionHeader(title = "إدارة")
        }
        items(manageTools.size) { index ->
            ToolGridCard(tool = manageTools[index], onClick = { onToolClick(manageTools[index].onClickAction) })
        }
    }
}

@Composable
private fun ToolsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        ),
        color = DarkText,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp),
    )
}

@Composable
private fun ToolGridCard(
    tool: ToolItem,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp),
            color = tool.iconBgColor,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.title,
                    tint = tool.iconTint,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = tool.title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
            ),
            color = DarkText,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 15.sp,
        )
    }
}
