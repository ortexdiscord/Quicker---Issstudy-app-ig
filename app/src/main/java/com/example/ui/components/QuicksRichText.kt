package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * Builds an AnnotatedString parsing Markdown and custom syntax:
 * - **bold**
 * - *italic*
 * - _line_ (italic + underline)
 * - [Source: ...] (source badge)
 * - (13) or [13] (citations / footnote reference badges)
 * - `code` (monospace inline code)
 * - ~~strikethrough~~
 */
fun parseMarkdownToAnnotatedString(
    text: String,
    baseColor: Color,
    primaryColor: Color,
    codeBgColor: Color = Color.Black.copy(alpha = 0.08f)
): AnnotatedString {
    return buildAnnotatedString {
        val pattern = Pattern.compile(
            "(\\*\\*([^*]+)\\*\\*)|" +                     // 1, 2: **bold**
            "(\\*([^*]+)\\*)|" +                           // 3, 4: *italic*
            "(_([^_]+)_)|" +                               // 5, 6: _line_
            "(`([^`]+)`)|" +                               // 7, 8: `code`
            "(~~([^~]+)~~)|" +                             // 9, 10: ~~strike~~
            "(\\[Source:\\s*([^]]+)\\])|" +                // 11, 12: [Source: file]
            "(\\(([0-9]+)\\))|" +                          // 13, 14: (13)
            "(\\[([0-9]+)\\])"                             // 15, 16: [13]
        )

        val matcher = pattern.matcher(text)
        var lastIndex = 0

        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()

            if (start > lastIndex) {
                append(text.substring(lastIndex, start))
            }

            when {
                // **bold**
                matcher.group(1) != null -> {
                    val content = matcher.group(2) ?: ""
                    val spanStart = length
                    append(content)
                    addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold),
                        spanStart,
                        length
                    )
                }
                // *italic*
                matcher.group(3) != null -> {
                    val content = matcher.group(4) ?: ""
                    val spanStart = length
                    append(content)
                    addStyle(
                        SpanStyle(fontStyle = FontStyle.Italic),
                        spanStart,
                        length
                    )
                }
                // _line_ -> italic with underline
                matcher.group(5) != null -> {
                    val content = matcher.group(6) ?: ""
                    val spanStart = length
                    append(content)
                    addStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic,
                            textDecoration = TextDecoration.Underline
                        ),
                        spanStart,
                        length
                    )
                }
                // `code`
                matcher.group(7) != null -> {
                    val content = matcher.group(8) ?: ""
                    val spanStart = length
                    append(" $content ")
                    addStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = codeBgColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp
                        ),
                        spanStart,
                        length
                    )
                }
                // ~~strikethrough~~
                matcher.group(9) != null -> {
                    val content = matcher.group(10) ?: ""
                    val spanStart = length
                    append(content)
                    addStyle(
                        SpanStyle(textDecoration = TextDecoration.LineThrough),
                        spanStart,
                        length
                    )
                }
                // [Source: filename] badge
                matcher.group(11) != null -> {
                    val srcName = matcher.group(12) ?: ""
                    val spanStart = length
                    append(" 📄 $srcName ")
                    addStyle(
                        SpanStyle(
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            background = primaryColor.copy(alpha = 0.12f),
                            fontSize = 11.sp
                        ),
                        spanStart,
                        length
                    )
                }
                // (13) citation badge
                matcher.group(13) != null -> {
                    val num = matcher.group(14) ?: ""
                    val spanStart = length
                    append("($num)")
                    addStyle(
                        SpanStyle(
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            baselineShift = BaselineShift.Superscript
                        ),
                        spanStart,
                        length
                    )
                }
                // [13] citation badge
                matcher.group(15) != null -> {
                    val num = matcher.group(16) ?: ""
                    val spanStart = length
                    append("[$num]")
                    addStyle(
                        SpanStyle(
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            baselineShift = BaselineShift.Superscript
                        ),
                        spanStart,
                        length
                    )
                }
            }

            lastIndex = end
        }

        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

private sealed interface MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock
    data class Bullet(val text: String) : MarkdownBlock
    data class Numbered(val number: String, val text: String) : MarkdownBlock
    data class Blockquote(val text: String) : MarkdownBlock
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    object Blank : MarkdownBlock
}

private fun parseMarkdownBlocks(rawText: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = rawText.split("\n")
    var inCodeBlock = false
    var codeLanguage = ""
    val codeBuffer = StringBuilder()

    for (line in lines) {
        val trimmed = line.trim()

        if (trimmed.startsWith("```")) {
            if (inCodeBlock) {
                // End code block
                blocks.add(MarkdownBlock.CodeBlock(codeLanguage, codeBuffer.toString().trimEnd()))
                codeBuffer.clear()
                codeLanguage = ""
                inCodeBlock = false
            } else {
                // Start code block
                inCodeBlock = true
                codeLanguage = trimmed.removePrefix("```").trim()
            }
            continue
        }

        if (inCodeBlock) {
            codeBuffer.appendLine(line)
            continue
        }

        when {
            trimmed.isBlank() -> {
                blocks.add(MarkdownBlock.Blank)
            }
            trimmed.startsWith("### ") -> {
                blocks.add(MarkdownBlock.Header(3, trimmed.removePrefix("### ")))
            }
            trimmed.startsWith("## ") -> {
                blocks.add(MarkdownBlock.Header(2, trimmed.removePrefix("## ")))
            }
            trimmed.startsWith("# ") -> {
                blocks.add(MarkdownBlock.Header(1, trimmed.removePrefix("# ")))
            }
            trimmed.startsWith("> ") -> {
                blocks.add(MarkdownBlock.Blockquote(trimmed.removePrefix("> ")))
            }
            trimmed.startsWith("- ") || trimmed.startsWith("• ") || trimmed.startsWith("* ") -> {
                val bulletText = when {
                    trimmed.startsWith("- ") -> trimmed.removePrefix("- ")
                    trimmed.startsWith("• ") -> trimmed.removePrefix("• ")
                    else -> trimmed.removePrefix("* ")
                }
                blocks.add(MarkdownBlock.Bullet(bulletText))
            }
            trimmed.matches("^([0-9]+)\\.\\s+.*".toRegex()) -> {
                val match = "^([0-9]+)\\.\\s+(.*)".toRegex().find(trimmed)
                val num = match?.groupValues?.getOrNull(1) ?: "1"
                val item = match?.groupValues?.getOrNull(2) ?: trimmed
                blocks.add(MarkdownBlock.Numbered(num, item))
            }
            else -> {
                blocks.add(MarkdownBlock.Paragraph(line))
            }
        }
    }

    if (inCodeBlock && codeBuffer.isNotEmpty()) {
        blocks.add(MarkdownBlock.CodeBlock(codeLanguage, codeBuffer.toString().trimEnd()))
    }

    return blocks
}

/**
 * First-class Rich Markdown component supporting:
 * - Code blocks with copy actions
 * - Academic Math/Proof blocks
 * - Headers (H1, H2, H3)
 * - Blockquotes
 * - Numbered and bulleted lists
 * - Inline bold, italic, strikethrough, underline, and citations
 */
@Composable
fun QuicksRichText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = 14.sp,
    lineHeight: TextUnit = 20.sp,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val codeBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val blocks = remember(text) { parseMarkdownBlocks(text) }

    Column(modifier = modifier) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    val (addedSize, weight) = when (block.level) {
                        1 -> Pair(4.sp, FontWeight.ExtraBold)
                        2 -> Pair(2.5.sp, FontWeight.Bold)
                        else -> Pair(1.sp, FontWeight.SemiBold)
                    }
                    val annotated = remember(block.text, color, primaryColor) {
                        parseMarkdownToAnnotatedString(block.text, color, primaryColor, codeBg)
                    }
                    Text(
                        text = annotated,
                        style = style.copy(
                            fontSize = (fontSize.value + addedSize.value).sp,
                            fontWeight = weight,
                            color = color
                        ),
                        modifier = Modifier.padding(
                            top = if (block.level == 1) 8.dp else 5.dp,
                            bottom = 2.dp
                        )
                    )
                }

                is MarkdownBlock.Bullet -> {
                    val annotated = remember(block.text, color, primaryColor) {
                        parseMarkdownToAnnotatedString(block.text, color, primaryColor, codeBg)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 7.dp, start = 4.dp, end = 8.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(primaryColor)
                        )
                        Text(
                            text = annotated,
                            style = style.copy(
                                fontSize = fontSize,
                                lineHeight = lineHeight,
                                color = color
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownBlock.Numbered -> {
                    val annotated = remember(block.text, color, primaryColor) {
                        parseMarkdownToAnnotatedString(block.text, color, primaryColor, codeBg)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}.",
                            fontWeight = FontWeight.Bold,
                            fontSize = fontSize,
                            color = primaryColor,
                            modifier = Modifier.padding(end = 6.dp, start = 2.dp)
                        )
                        Text(
                            text = annotated,
                            style = style.copy(
                                fontSize = fontSize,
                                lineHeight = lineHeight,
                                color = color
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownBlock.Blockquote -> {
                    val annotated = remember(block.text, color, primaryColor) {
                        parseMarkdownToAnnotatedString(block.text, color, primaryColor, codeBg)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(primaryColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = annotated,
                            style = style.copy(
                                fontSize = fontSize,
                                lineHeight = lineHeight,
                                fontStyle = FontStyle.Italic,
                                color = color
                            )
                        )
                    }
                }

                is MarkdownBlock.CodeBlock -> {
                    var copied by remember { mutableStateOf(false) }
                    val label = block.language.ifBlank { "code" }.uppercase()

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = Color(0xFF1E1E24)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9CA3AF),
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(block.code))
                                        copied = true
                                        coroutineScope.launch {
                                            delay(2000)
                                            copied = false
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (copied) Icons.Default.Done else Icons.Default.ContentCopy,
                                        contentDescription = "Copy Code",
                                        tint = if (copied) Color(0xFF10B981) else Color(0xFF9CA3AF),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = block.code,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = Color(0xFFF3F4F6),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }

                is MarkdownBlock.Paragraph -> {
                    val annotated = remember(block.text, color, primaryColor) {
                        parseMarkdownToAnnotatedString(block.text, color, primaryColor, codeBg)
                    }
                    Text(
                        text = annotated,
                        style = style.copy(
                            fontSize = fontSize,
                            lineHeight = lineHeight,
                            color = color
                        ),
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }

                is MarkdownBlock.Blank -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
