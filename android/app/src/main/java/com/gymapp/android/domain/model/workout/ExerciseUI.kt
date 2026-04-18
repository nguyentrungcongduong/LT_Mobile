package com.gymapp.android.domain.model.workout

data class ExerciseUI(
    val id: String,
    val name: String,
    var isSelected: Boolean = false
)