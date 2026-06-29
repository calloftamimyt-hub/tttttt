package com.example.data

object DuaData {
    val duaList: List<com.example.model.Dua> = emptyList()
    val categories: List<String> = emptyList()
}
object HadithData {
    val hadithList: List<com.example.model.Hadith> = emptyList()
    val categories: List<String> = emptyList()
}
class DuaStorage(context: android.content.Context) {
    fun isDuaSaved(id: String): Boolean = false
    fun toggleSavedDua(id: String) {}
}
