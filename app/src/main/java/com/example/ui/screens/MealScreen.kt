package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.MealEntryEntity
import com.example.data.local.entities.MemberEntity
import com.example.ui.util.AppStrings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MealScreen(
    selectedDate: String,
    members: List<MemberEntity>,
    meals: List<MealEntryEntity>,
    isBangla: Boolean,
    onDateSelected: (String) -> Unit,
    onSaveMeal: (memberId: String, date: String, breakfast: Float, lunch: Float, dinner: Float, guestMeals: Float, note: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val mealsByMember = meals.associateBy { it.memberId }

    // Date navigation helpers
    fun changeDate(offsetDays: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        try {
            cal.time = sdf.parse(selectedDate) ?: Date()
        } catch (e: Exception) {
            cal.time = Date()
        }
        cal.add(Calendar.DAY_OF_YEAR, offsetDays)
        onDateSelected(sdf.format(cal.time))
    }

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val isToday = selectedDate == todayStr

    // Calculate total meals on this date
    var dayTotalMeals = 0f
    meals.forEach {
        dayTotalMeals += it.breakfast + it.lunch + it.dinner + it.guestMeals
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Date Selector Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { changeDate(-1) },
                    modifier = Modifier.testTag("prev_date_button")
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isToday) "$selectedDate (${if (isBangla) "আজ" else "Today"})" else selectedDate,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = { changeDate(1) },
                    modifier = Modifier.testTag("next_date_button")
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Total Meals for Selected Day Bar + Quick "Set Default 1-1-1"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${AppStrings.t("total_day_meals", isBangla)}: ${AppStrings.formatMeal(dayTotalMeals)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            TextButton(
                onClick = {
                    members.forEach { m ->
                        onSaveMeal(m.id, selectedDate, 1f, 1f, 1f, 0f, "")
                    }
                },
                modifier = Modifier.testTag("set_default_meals_button")
            ) {
                Text(
                    text = if (isBangla) "সবার ফুল মিল (১-১-১)" else "Set All (1-1-1)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Members List for Meal Entry
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(members) { member ->
                val entry = mealsByMember[member.id]
                val b = entry?.breakfast ?: 0f
                val l = entry?.lunch ?: 1f
                val d = entry?.dinner ?: 1f
                val g = entry?.guestMeals ?: 0f

                MemberMealCard(
                    member = member,
                    breakfast = b,
                    lunch = l,
                    dinner = d,
                    guestMeals = g,
                    isBangla = isBangla,
                    onUpdateBreakfast = { newB ->
                        onSaveMeal(member.id, selectedDate, newB, l, d, g, entry?.note ?: "")
                    },
                    onUpdateLunch = { newL ->
                        onSaveMeal(member.id, selectedDate, b, newL, d, g, entry?.note ?: "")
                    },
                    onUpdateDinner = { newD ->
                        onSaveMeal(member.id, selectedDate, b, l, newD, g, entry?.note ?: "")
                    },
                    onUpdateGuest = { newG ->
                        onSaveMeal(member.id, selectedDate, b, l, d, newG, entry?.note ?: "")
                    }
                )
            }
        }
    }
}

@Composable
fun MemberMealCard(
    member: MemberEntity,
    breakfast: Float,
    lunch: Float,
    dinner: Float,
    guestMeals: Float,
    isBangla: Boolean,
    onUpdateBreakfast: (Float) -> Unit,
    onUpdateLunch: (Float) -> Unit,
    onUpdateDinner: (Float) -> Unit,
    onUpdateGuest: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val total = breakfast + lunch + dinner + guestMeals

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("member_meal_card_${member.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Member Header & Day Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (member.type == "MANAGED") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = AppStrings.t("managed_badge", isBangla),
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${if (isBangla) "মোট" else "Total"}: ${AppStrings.formatMeal(total)}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Meal Counters Grid: Breakfast, Lunch, Dinner, Guest
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Breakfast
                MealCounterColumn(
                    label = AppStrings.t("breakfast", isBangla),
                    value = breakfast,
                    onIncrement = { onUpdateBreakfast((breakfast + 0.5f).coerceAtMost(5f)) },
                    onDecrement = { onUpdateBreakfast((breakfast - 0.5f).coerceAtLeast(0f)) },
                    testTagPrefix = "breakfast_${member.id}"
                )

                // Lunch
                MealCounterColumn(
                    label = AppStrings.t("lunch", isBangla),
                    value = lunch,
                    onIncrement = { onUpdateLunch((lunch + 0.5f).coerceAtMost(5f)) },
                    onDecrement = { onUpdateLunch((lunch - 0.5f).coerceAtLeast(0f)) },
                    testTagPrefix = "lunch_${member.id}"
                )

                // Dinner
                MealCounterColumn(
                    label = AppStrings.t("dinner", isBangla),
                    value = dinner,
                    onIncrement = { onUpdateDinner((dinner + 0.5f).coerceAtMost(5f)) },
                    onDecrement = { onUpdateDinner((dinner - 0.5f).coerceAtLeast(0f)) },
                    testTagPrefix = "dinner_${member.id}"
                )

                // Guest
                MealCounterColumn(
                    label = AppStrings.t("guest", isBangla),
                    value = guestMeals,
                    step = 1f,
                    onIncrement = { onUpdateGuest((guestMeals + 1f).coerceAtMost(10f)) },
                    onDecrement = { onUpdateGuest((guestMeals - 1f).coerceAtLeast(0f)) },
                    testTagPrefix = "guest_${member.id}"
                )
            }
        }
    }
}

@Composable
fun MealCounterColumn(
    label: String,
    value: Float,
    step: Float = 0.5f,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Decrement Button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("${testTagPrefix}_dec"),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onDecrement, modifier = Modifier.size(28.dp)) {
                    Text("-", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Text(
                text = AppStrings.formatMeal(value),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Increment Button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .testTag("${testTagPrefix}_inc"),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onIncrement, modifier = Modifier.size(28.dp)) {
                    Text("+", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}
