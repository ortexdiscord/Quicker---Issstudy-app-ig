package com.example.data.calendar

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Holiday(
    val name: String,
    val dateString: String, // "yyyy-MM-dd"
    val countryCode: String, // "US", "IN", "GB", "CA", "AU", "GLOBAL"
    val countryName: String,
    val category: String, // "Federal Holiday", "Public Holiday", "Cultural Celebration"
    val icon: String = "🎉",
    val country: String = countryName,
    val isPublicHoliday: Boolean = category.contains("Holiday", ignoreCase = true)
) {
    val day: Int
        get() = dateString.takeLast(2).toIntOrNull() ?: 1
}

typealias HolidayInfo = Holiday

object HolidayManager {

    private var overrideCountryCode: String? = null

    fun getDefaultCountryCode(): String {
        val detected = Locale.getDefault().country.uppercase()
        return if (supportedCountries.containsKey(detected)) detected else "US"
    }

    fun getCurrentCountryCode(): String {
        return overrideCountryCode ?: getDefaultCountryCode()
    }

    fun setOverrideCountryCode(code: String?) {
        overrideCountryCode = code
    }

    fun getCurrentCountryName(): String {
        return supportedCountries[getCurrentCountryCode()] ?: "United States"
    }

    fun getAvailableCountries(): List<Pair<String, String>> {
        return supportedCountries.map { it.key to it.value }
    }

    fun getHolidaysForMonthYear(month: Int, year: Int, countryCode: String = getCurrentCountryCode()): List<Holiday> {
        val all = getHolidaysForYearAndCountry(year, countryCode)
        val monthPrefix = String.format(Locale.US, "%04d-%02d-", year, month + 1)
        return all.filter { it.dateString.startsWith(monthPrefix) }
    }

    fun getHolidaysForDate(calendar: Calendar, countryCode: String = getCurrentCountryCode()): List<Holiday> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateStr = sdf.format(calendar.time)
        val hol = getHolidayForDate(dateStr, countryCode)
        return if (hol != null) listOf(hol) else emptyList()
    }

    val supportedCountries = mapOf(
        "US" to "United States",
        "IN" to "India",
        "GB" to "United Kingdom",
        "CA" to "Canada",
        "AU" to "Australia",
        "GLOBAL" to "International"
    )

    fun getHolidaysForYearAndCountry(year: Int, countryCode: String): List<Holiday> {
        val list = mutableListOf<Holiday>()
        val country = countryCode.uppercase()

        // Common International / Global
        list.add(Holiday("New Year's Day", "$year-01-01", country, getCountryName(country), "Public Holiday", "✨"))
        list.add(Holiday("Valentine's Day", "$year-02-14", country, getCountryName(country), "Observance", "💖"))
        list.add(Holiday("International Women's Day", "$year-03-08", country, getCountryName(country), "Observance", "🌸"))
        list.add(Holiday("Earth Day", "$year-04-22", country, getCountryName(country), "Observance", "🌍"))
        list.add(Holiday("Halloween", "$year-10-31", country, getCountryName(country), "Celebration", "🎃"))
        list.add(Holiday("Christmas Eve", "$year-12-24", country, getCountryName(country), "Public Holiday", "🎄"))
        list.add(Holiday("Christmas Day", "$year-12-25", country, getCountryName(country), "Public Holiday", "🎁"))
        list.add(Holiday("New Year's Eve", "$year-12-31", country, getCountryName(country), "Celebration", "🥂"))

        when (country) {
            "US" -> {
                // US Federal Holidays
                // MLK Day: 3rd Monday in January
                val mlkDate = getNthDayOfWeekInMonth(year, Calendar.JANUARY, Calendar.MONDAY, 3)
                list.add(Holiday("Martin Luther King Jr. Day", mlkDate, "US", "United States", "Federal Holiday", "🕊️"))

                // Presidents' Day: 3rd Monday in February
                val presDate = getNthDayOfWeekInMonth(year, Calendar.FEBRUARY, Calendar.MONDAY, 3)
                list.add(Holiday("Presidents' Day", presDate, "US", "United States", "Federal Holiday", "🏛️"))

                // Memorial Day: Last Monday in May
                val memDate = getLastDayOfWeekInMonth(year, Calendar.MAY, Calendar.MONDAY)
                list.add(Holiday("Memorial Day", memDate, "US", "United States", "Federal Holiday", "🎖️"))

                list.add(Holiday("Juneteenth National Independence Day", "$year-06-19", "US", "United States", "Federal Holiday", "✊"))
                list.add(Holiday("Independence Day (4th of July)", "$year-07-04", "US", "United States", "Federal Holiday", "🎆"))

                // Labor Day: 1st Monday in September
                val laborDate = getNthDayOfWeekInMonth(year, Calendar.SEPTEMBER, Calendar.MONDAY, 1)
                list.add(Holiday("Labor Day", laborDate, "US", "United States", "Federal Holiday", "🛠️"))

                // Columbus Day / Indigenous Peoples' Day: 2nd Monday in October
                val columbusDate = getNthDayOfWeekInMonth(year, Calendar.OCTOBER, Calendar.MONDAY, 2)
                list.add(Holiday("Indigenous Peoples' Day", columbusDate, "US", "United States", "Federal Holiday", "🌎"))

                list.add(Holiday("Veterans Day", "$year-11-11", "US", "United States", "Federal Holiday", "🎗️"))

                // Thanksgiving: 4th Thursday in November
                val thanksgivingDate = getNthDayOfWeekInMonth(year, Calendar.NOVEMBER, Calendar.THURSDAY, 4)
                list.add(Holiday("Thanksgiving Day", thanksgivingDate, "US", "United States", "Federal Holiday", "🦃"))
                val blackFriDate = addDaysToDate(thanksgivingDate, 1)
                list.add(Holiday("Day After Thanksgiving", blackFriDate, "US", "United States", "State Holiday", "🛍️"))
            }

            "IN" -> {
                // India National & Major Holidays
                list.add(Holiday("Republic Day", "$year-01-26", "IN", "India", "National Holiday", "🇮🇳"))
                list.add(Holiday("Ambedkar Jayanti", "$year-04-14", "IN", "India", "Gazetted Holiday", "📜"))
                list.add(Holiday("Independence Day", "$year-08-15", "IN", "India", "National Holiday", "🇮🇳"))
                list.add(Holiday("Gandhi Jayanti", "$year-10-02", "IN", "India", "National Holiday", "👓"))

                // Approximate festival dates for 2025/2026/2027
                if (year == 2026) {
                    list.add(Holiday("Maha Shivratri", "$year-02-16", "IN", "India", "Gazetted Holiday", "🔱"))
                    list.add(Holiday("Holi (Festival of Colors)", "$year-03-04", "IN", "India", "Gazetted Holiday", "🎨"))
                    list.add(Holiday("Eid-ul-Fitr", "$year-03-20", "IN", "India", "Gazetted Holiday", "🌙"))
                    list.add(Holiday("Raksha Bandhan", "$year-08-28", "IN", "India", "Festival", "🧵"))
                    list.add(Holiday("Ganesh Chaturthi", "$year-09-14", "IN", "India", "Festival", "🐘"))
                    list.add(Holiday("Dussehra (Vijayadashami)", "$year-10-20", "IN", "India", "Gazetted Holiday", "🏹"))
                    list.add(Holiday("Diwali (Deepavali)", "$year-11-08", "IN", "India", "Gazetted Holiday", "🪔"))
                    list.add(Holiday("Guru Nanak Jayanti", "$year-11-24", "IN", "India", "Gazetted Holiday", "🕯️"))
                } else {
                    list.add(Holiday("Holi (Festival of Colors)", "$year-03-15", "IN", "India", "Gazetted Holiday", "🎨"))
                    list.add(Holiday("Dussehra", "$year-10-12", "IN", "India", "Gazetted Holiday", "🏹"))
                    list.add(Holiday("Diwali (Deepavali)", "$year-11-01", "IN", "India", "Gazetted Holiday", "🪔"))
                }
            }

            "GB" -> {
                // UK Bank Holidays
                list.add(Holiday("Good Friday", "$year-04-03", "GB", "United Kingdom", "Bank Holiday", "✝️"))
                list.add(Holiday("Easter Monday", "$year-04-06", "GB", "United Kingdom", "Bank Holiday", "🐣"))
                val earlyMayDate = getNthDayOfWeekInMonth(year, Calendar.MAY, Calendar.MONDAY, 1)
                list.add(Holiday("Early May Bank Holiday", earlyMayDate, "GB", "United Kingdom", "Bank Holiday", "🌼"))
                val springDate = getLastDayOfWeekInMonth(year, Calendar.MAY, Calendar.MONDAY)
                list.add(Holiday("Spring Bank Holiday", springDate, "GB", "United Kingdom", "Bank Holiday", "☀️"))
                val summerDate = getLastDayOfWeekInMonth(year, Calendar.AUGUST, Calendar.MONDAY)
                list.add(Holiday("Summer Bank Holiday", summerDate, "GB", "United Kingdom", "Bank Holiday", "⛵"))
                list.add(Holiday("Boxing Day", "$year-12-26", "GB", "United Kingdom", "Bank Holiday", "📦"))
            }

            "CA" -> {
                // Canada Statutory Holidays
                val famDate = getNthDayOfWeekInMonth(year, Calendar.FEBRUARY, Calendar.MONDAY, 3)
                list.add(Holiday("Family Day", famDate, "CA", "Canada", "Statutory Holiday", "👨‍👩‍👧"))
                val vicDate = getMondayPrecedingMay25(year)
                list.add(Holiday("Victoria Day", vicDate, "CA", "Canada", "Statutory Holiday", "👑"))
                list.add(Holiday("Canada Day", "$year-07-01", "CA", "Canada", "National Holiday", "🍁"))
                val labDate = getNthDayOfWeekInMonth(year, Calendar.SEPTEMBER, Calendar.MONDAY, 1)
                list.add(Holiday("Labour Day", labDate, "CA", "Canada", "Statutory Holiday", "🛠️"))
                val thanksgivingDate = getNthDayOfWeekInMonth(year, Calendar.OCTOBER, Calendar.MONDAY, 2)
                list.add(Holiday("Thanksgiving Day", thanksgivingDate, "CA", "Canada", "Statutory Holiday", "🦃"))
                list.add(Holiday("Remembrance Day", "$year-11-11", "CA", "Canada", "Statutory Holiday", "🌺"))
                list.add(Holiday("Boxing Day", "$year-12-26", "CA", "Canada", "Statutory Holiday", "📦"))
            }

            "AU" -> {
                // Australia Public Holidays
                list.add(Holiday("Australia Day", "$year-01-26", "AU", "Australia", "National Holiday", "🦘"))
                list.add(Holiday("ANZAC Day", "$year-04-25", "AU", "Australia", "National Holiday", "🎖️"))
                val kingsBirthday = getNthDayOfWeekInMonth(year, Calendar.JUNE, Calendar.MONDAY, 2)
                list.add(Holiday("King's Birthday", kingsBirthday, "AU", "Australia", "Public Holiday", "👑"))
                list.add(Holiday("Boxing Day", "$year-12-26", "AU", "Australia", "Public Holiday", "📦"))
            }
        }

        return list.sortedBy { it.dateString }
    }

    fun getHolidayForDate(dateString: String, countryCode: String): Holiday? {
        val year = dateString.take(4).toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
        val holidays = getHolidaysForYearAndCountry(year, countryCode)
        return holidays.find { it.dateString == dateString }
    }

    private fun getCountryName(code: String): String = supportedCountries[code] ?: "International"

    private fun getNthDayOfWeekInMonth(year: Int, month: Int, dayOfWeek: Int, n: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        var count = 0
        while (cal.get(Calendar.MONTH) == month) {
            if (cal.get(Calendar.DAY_OF_WEEK) == dayOfWeek) {
                count++
                if (count == n) break
            }
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(cal.time)
    }

    private fun getLastDayOfWeekInMonth(year: Int, month: Int, dayOfWeek: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))

        while (cal.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(cal.time)
    }

    private fun getMondayPrecedingMay25(year: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, Calendar.MAY)
        cal.set(Calendar.DAY_OF_MONTH, 24)
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(cal.time)
    }

    private fun addDaysToDate(dateString: String, days: Int): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = sdf.parse(dateString) ?: Date()
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DAY_OF_MONTH, days)
        return sdf.format(cal.time)
    }
}
