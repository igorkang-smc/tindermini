package com.example.tinderclone.swipecards

import androidx.annotation.DrawableRes
import com.example.tinderclone.R

data class MatchProfile(
    val name: String,
    @DrawableRes val drawableResId: Int,
)

val profiles = listOf<MatchProfile>()