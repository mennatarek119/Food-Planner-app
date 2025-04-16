package com.example.hmnm.utils

import com.example.hmnm.R

fun getFlagResId(areaName: String): Int {
    return when (areaName) {
        "American" -> R.drawable.us_flag
        "British" -> R.drawable.gb_flag
        "Canadian" -> R.drawable.ca_flag
        "Chinese" -> R.drawable.cn_flag
        "Croatian" -> R.drawable.hr_flag
        "Dutch" -> R.drawable.nl_flag
        "Egyptian" -> R.drawable.eg_flag
        "Filipino" -> R.drawable.ph_flag
        "French" -> R.drawable.fr_flag
        "Greek" -> R.drawable.gr_flag
        "Indian" -> R.drawable.in_flag
        "Irish" -> R.drawable.ie_flag
        "Italian" -> R.drawable.it_flag
        "Jamaican" -> R.drawable.jm_flag
        "Japanese" -> R.drawable.jp_flag
        "Kenyan" -> R.drawable.ke_flag
        "Malaysian" -> R.drawable.my_flag
        "Mexican" -> R.drawable.mx_flag
        "Moroccan" -> R.drawable.ma_flag
        "Polish" -> R.drawable.pl_flag
        "Portuguese" -> R.drawable.pt_flag
        "Russian" -> R.drawable.ru_flag
        "Spanish" -> R.drawable.es_flag
        "Thai" -> R.drawable.th_flag
        "Tunisian" -> R.drawable.tn_flag
        "Turkish" -> R.drawable.tr_flag
        "Ukrainian" -> R.drawable.ua_flag
        "Unknown" -> R.drawable.unknown_flag  // صورة افتراضية
        "Vietnamese" -> R.drawable.vn_flag
        else -> R.drawable.unknown_flag  // صورة افتراضية في حالة عدم وجود العلم
    }
}
