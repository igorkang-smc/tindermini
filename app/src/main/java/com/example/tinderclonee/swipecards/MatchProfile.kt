package com.example.tinderclonee.swipecards

import androidx.annotation.DrawableRes
import com.example.tinderclonee.R

data class MatchProfile(
    val name: String,
    @DrawableRes val drawableResId: Int,
)

val profiles = listOf<MatchProfile>()