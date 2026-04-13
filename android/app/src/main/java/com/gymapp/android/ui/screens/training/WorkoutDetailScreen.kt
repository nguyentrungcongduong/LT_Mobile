package com.gymapp.android.ui.screens.training

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.gymapp.android.R
import com.gymapp.android.data.remote.response.WorkoutPlanResponse

@Composable
fun WorkoutDetailScreen(
    plan: WorkoutPlanResponse,
    onBack: () -> Unit = {},
    onComplete: () -> Unit = {}
) {
    val context = LocalContext.current

    val checked = remember { mutableStateListOf<Int>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // nền trắng xám nhẹ
    ) {

        // HEADER IMAGE
        Box {
            Image(
                painter = painterResource(id = R.drawable.banner1),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
            }
        }

        //  CARD MAIN
        Card(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {

            Column(modifier = Modifier.padding(16.dp)) {

                Text(plan.name ?: "Chưa có tên", fontSize = 26.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    plan.planType?.replace("_", " ") ?: "GENERAL",
                    color = Color(0xFF2979FF),
                    modifier = Modifier
                        .background(Color(0xFFE3F2FD), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // INFO BOX
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Cấp độ", color = Color.Gray, fontSize = 12.sp)
                        Text(plan.targetLevel ?: "Mọi cấp độ", fontWeight = FontWeight.Medium)
                    }

                    Column {
                        Text("Thời lượng", color = Color.Gray, fontSize = 12.sp)
                        Text("${plan.exercises.size * 5} phút", fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("${plan.exercises.size} exercises", fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(10.dp))

                // LIST EXERCISE
                LazyColumn {
                    itemsIndexed(plan.exercises) { index, ex ->
                        ExerciseItem(
                            exercise = ex,
                            isChecked = checked.contains(index),
                            onCheck = {
                                if (checked.contains(index)) {
                                    checked.remove(index)
                                } else {
                                    checked.add(index)
                                }
                            }
                        )
                    }
                }


                Spacer(modifier = Modifier.height(12.dp))

                // BUTTON START / COMPLETE
                Button(
                    onClick = {
                        if (checked.isEmpty()) {
                            Toast.makeText(context, "Hãy đánh dấu ít nhất 1 bài tập!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onComplete()
                        Toast.makeText(context, "Tuyệt vời! Đã lưu tiến độ tập.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                ) {
                    val text = if (checked.size == plan.exercises.size) "🎉 HOÀN THÀNH BÀI TẬP" else "💾 LƯU TIẾN ĐỘ (${checked.size}/${plan.exercises.size})"
                    Text(text, fontSize = 16.sp)
                }
            }
        }
    }
}