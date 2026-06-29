package com.example.model

class Country(
    val code: String = "",
    val name: String = "",
    val flag: String = "",
    val language: String = ""
)
class CountryData {
    companion object {
        val countries: List<Country> = emptyList()
    }
}

class CircleAlert(
    val id: String = "",
    val docId: String = "",
    val title: String = "",
    val description: String = "",
    val mediaUri: String = "",
    val mediaType: String = "",
    val contactNumber: String = "",
    val country: String = "",
    val location: String = "",
    val timestamp: Long = 0L,
    val status: String = ""
)
class UserProfile(
    val id: String = "",
    val queue: Int = 0,
    val data1: String = ""
)
class Dua(val id: String = "", val title: String = "", val arabic: String = "", val pronunciation: String = "", val translation: String = "", val reference: String = "", val category: String = "")
class Hadith(val id: String = "", val title: String = "", val arabic: String = "", val pronunciation: String = "", val translation: String = "", val reference: String = "", val category: String = "")
