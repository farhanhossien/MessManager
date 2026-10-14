package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.BazarExpenseEntity
import com.example.data.local.entities.MessEntity
import com.example.domain.model.MessCalculationSummary
import com.example.ui.util.AppStrings

@Composable
fun ReportsScreen(
    currentMess: MessEntity?,
    summary: MessCalculationSummary,
    expenses: List<BazarExpenseEntity>,
    isBangla: Boolean,
    onBack: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Aggregate expenses by category
    val expensesByCategory = expenses.groupBy { it.category }
    val totalExpense = expenses.sumOf { it.amount }

    val categoryColorMap = mapOf(
        "rice" to Color(0xFFEAB308),
        "meat" to Color(0xFFEF4444),
        "fish" to Color(0xFF06B6D4),
        "vegetables" to Color(0xFF22C55E),
        "dal" to Color(0xFFF97316),
        "oil" to Color(0xFF8B5CF6),
        "spices" to Color(0xFFA855F7),
        "others" to Color(0xFF64748B)
    )

    // Build Formatted Share Slip
    fun generateShareStatement(): String {
        val messTitle = currentMess?.name ?: "মেস হিসাব"
        val sb = StringBuilder()
        sb.appendLine("📋 $messTitle - ${AppStrings.t("monthly_summary_title", isBangla)}")
        sb.appendLine("মাস: ${summary.month}")
        sb.appendLine("--------------------------------")
        sb.appendLine("${AppStrings.t("total_bazar", isBangla)}: ${AppStrings.formatTaka(summary.totalBazarCost)}")
        sb.appendLine("${AppStrings.t("total_meals", isBangla)}: ${AppStrings.formatMeal(summary.totalMeals)}")
        sb.appendLine("${AppStrings.t("current_meal_rate", isBangla)}: ${AppStrings.formatTaka(summary.mealRate)}")
        sb.appendLine("${AppStrings.t("fixed_cost_title", isBangla)}: ${AppStrings.formatTaka(summary.totalFixedCosts)}")
        sb.appendLine("${AppStrings.t("nav_deposits", isBangla)}: ${AppStrings.formatTaka(summary.totalDeposits)}")
        sb.appendLine("--------------------------------")
        sb.appendLine("সদস্যদের হিসাব বিবরণী:")

        summary.memberCalculations.forEachIndexed { i, m ->
            val balText = if (m.netBalance >= 0) {
                "${AppStrings.t("will_receive", isBangla)}: ${AppStrings.formatTaka(m.netBalance)}"
            } else {
                "${AppStrings.t("will_give", isBangla)}: ${AppStrings.formatTaka(kotlin.math.abs(m.netBalance))}"
            }
            sb.appendLine("${i + 1}. ${m.name} | মিল: ${AppStrings.formatMeal(m.totalMeals)} | খরচ: ${AppStrings.formatTaka(m.totalCost)} | জমা: ${AppStrings.formatTaka(m.totalDeposits)} | $balText")
        }
        sb.appendLine("--------------------------------")
        sb.appendLine("হিসাব প্রস্তুতকারক: মেস ম্যানেজার (Meal Manager)")
        return sb.toString()
    }

    // Build CSV Content
    fun generateCsvContent(): String {
        val sb = StringBuilder()
        sb.appendLine("Member Name,Total Meals,Meal Cost,Fixed Cost Share,Total Deposits,Opening Balance,Net Balance")
        summary.memberCalculations.forEach { m ->
            sb.appendLine("\"${m.name}\",${m.totalMeals},${m.mealCost},${m.fixedCostShare},${m.totalDeposits},${m.openingBalance},${m.netBalance}")
        }
        return sb.toString()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = AppStrings.t("reports_title", isBangla),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Share & Export Actions Row (Text Slip, Real PDF, Real CSV)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val text = generateShareStatement()
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, text)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Mess Statement"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("share_statement_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBangla) "হোয়াটসঅ্যাপ / মেসেজ বিবরণী শেয়ার" else "Share WhatsApp / Text Slip",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        com.example.ui.util.ReportExporter.exportAndSharePdf(context, currentMess, summary)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("export_pdf_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBangla) "পিডিএফ (PDF) এক্সপোর্ট" else "Export PDF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        com.example.ui.util.ReportExporter.exportAndShareCsv(context, currentMess, summary)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("export_csv_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBangla) "সিএসভি (CSV) এক্সপোর্ট" else "Export CSV",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bazar Category Breakdown Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = AppStrings.t("category_share", isBangla),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (expenses.isEmpty()) {
                    Text(
                        text = AppStrings.t("no_expenses", isBangla),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    expensesByCategory.forEach { (catKey, catExpenses) ->
                        val catTotal = catExpenses.sumOf { it.amount }
                        val percentage = if (totalExpense > 0) (catTotal / totalExpense).toFloat() else 0f
                        val color = categoryColorMap[catKey] ?: MaterialTheme.colorScheme.primary
                        val catLabel = AppStrings.t("cat_$catKey", isBangla)

                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = catLabel, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                                Text(
                                    text = "${AppStrings.formatTaka(catTotal)} (${(percentage * 100).toInt()}%)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { percentage },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = color,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Statement Preview Slip Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "হিসাব বিবরণী প্রিভিউ" else "Statement Slip Preview",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    IconButton(
                        onClick = {
                            val slip = generateShareStatement()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Statement Slip", slip))
                            onShowSnackbar(if (isBangla) "বিবরণী ক্লিপবোর্ডে কপি করা হয়েছে!" else "Statement slip copied!")
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Slip", modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = generateShareStatement(),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
