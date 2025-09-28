package com.chatbotrag.core.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.regex.Matcher
import java.util.regex.Pattern

class PDFUtils {

    static Instant convertPDFDateToInstant(String pdfDate) {
        if (!pdfDate?.startsWith("D:")) {
            return null
        }

        // Regex: D:YYYYMMDDHHmmSS+HH'mm'
        def pattern = ~/D:(\d{14})([+-])(\d{2})'(\d{2})'/
        def matcher = pattern.matcher(pdfDate)

        if (matcher.matches()) {
            def dateTime = matcher.group(1)      // 20231212164541
            def sign = matcher.group(2)          // +
            def offsetHoursStr = matcher.group(3) // 01
            def offsetMinutesStr = matcher.group(4) // 00

            // LocalDateTime parsen
            def localDateTime = LocalDateTime.parse(dateTime,
                    DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))

            // ZoneOffset erstellen - EINFACHER WEG
            def offsetHours = offsetHoursStr as int
            def offsetMinutes = offsetMinutesStr as int

            if (sign == "-") {
                offsetHours = -offsetHours
                // offsetMinutes bleibt positiv bei ofHoursMinutes
            }

            def offset = ZoneOffset.ofHoursMinutes(offsetHours, offsetMinutes)
            return localDateTime.toInstant(offset)
        }

        return null
    }
}
