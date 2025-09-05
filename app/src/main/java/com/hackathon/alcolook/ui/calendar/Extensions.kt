package com.hackathon.alcolook.ui.calendar

import java.text.DecimalFormat

fun Float.format(digits: Int): String {
    val df = DecimalFormat()
    df.maximumFractionDigits = digits
    df.minimumFractionDigits = 0
    return df.format(this)
}