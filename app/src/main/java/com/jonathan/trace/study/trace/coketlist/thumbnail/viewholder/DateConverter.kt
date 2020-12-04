package com.jonathan.trace.study.trace.coketlist.thumbnail.viewholder

import java.text.SimpleDateFormat
import java.util.*

class DateConverter {
    companion object{
        private const val PATTERN_RAW = "yyyy-MM-dd HH:mm:ss"
        const val PATTERN_DATE_ONLY = "MMM dd, yyyy"
        const val PATTERN_TIME_ONLY = "hh:mm a"

        //TODO("check if val date can be null")
        fun convertDateRawString(target: String, pattern: String): String{
            val decoder = SimpleDateFormat(PATTERN_RAW, Locale.getDefault())
            val encoder = SimpleDateFormat(pattern, Locale.getDefault())

            val date = decoder.parse(target)
            return encoder.format(date)
        }

        fun getDateTime(): String{
            val date = Calendar.getInstance().time
            val decoder = SimpleDateFormat(PATTERN_RAW, Locale.getDefault())
            return decoder.format(date)
        }
    }
}