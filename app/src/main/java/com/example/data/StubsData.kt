package com.example.data

object DuaData {
    val duaList: List<com.example.model.Dua> = listOf(
        com.example.model.Dua(
            id = "1",
            title = "ঘুম থেকে ওঠার দোয়া",
            arabic = "الْحَمْدُ للهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            pronunciation = "Alhamdu lillahil-ladhi ahyana ba'da ma amatana wa ilaihin-nushur.",
            translation = "সব প্রশংসা আল্লাহর জন্য, যিনি আমাদেরকে মৃত্যুর (ঘুমের) পর জীবিত করলেন এবং তাঁর দিকেই আমাদের ফিরে যেতে হবে।",
            reference = "সহীহ বুখারী: ৬৩১২",
            category = "দৈনিক দোয়া"
        ),
        com.example.model.Dua(
            id = "2",
            title = "খাবারের শুরুর দোয়া",
            arabic = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
            pronunciation = "Bismillahir Rahmanir Rahim.",
            translation = "পরম করুণাময় অসীম দয়ালু আল্লাহর নামে শুরু করছি।",
            reference = "আবু দাউদ: ৩৭৬৭",
            category = "খাবার ও পানীয়"
        ),
        com.example.model.Dua(
            id = "3",
            title = "খাবারের শেষের দোয়া",
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ",
            pronunciation = "Alhamdu lillahil-ladhi at'amana wa saqana wa ja'alana muslimin.",
            translation = "সকল প্রশংসা আল্লাহর জন্য, যিনি আমাদেরকে খাওয়ালেন, পান করালেন এবং মুসলিম বানালেন।",
            reference = "আবু দাউদ: ৩৮৫০",
            category = "খাবার ও পানীয়"
        ),
        com.example.model.Dua(
            id = "4",
            title = "পিতা-মাতার জন্য দোয়া",
            arabic = "رَّبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا",
            pronunciation = "Rabbir hamhuma kama rabbayani saghira.",
            translation = "হে আমার প্রতিপালক! তাঁদের উভয়ের প্রতি দয়া করুন, যেমন তাঁরা শৈশবে আমাকে স্নেহ-মমতা দিয়ে লালন-পালন করেছিলেন।",
            reference = "আল-কুরআন, সূরা বনী ইসরাঈল: ২৪",
            category = "পরিবার ও সন্তান"
        ),
        com.example.model.Dua(
            id = "5",
            title = "বিপদ ও দুশ্চিন্তার দোয়া",
            arabic = "لَا إِلَهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
            pronunciation = "La ilaha illa anta subhanaka inni kuntu minaz-zalimin.",
            translation = "আপনি ছাড়া কোনো ইলাহ নেই, আপনি পবিত্র ও মহান! নিশ্চয়ই আমি জালিমদের অন্তর্ভুক্ত ছিলাম।",
            reference = "তিরমিজী: ৩৫০৫",
            category = "বিপদ ও দুশ্চিন্তা"
        ),
        com.example.model.Dua(
            id = "6",
            title = "ইলম বা জ্ঞান বৃদ্ধির দোয়া",
            arabic = "رَّبِّ زِدْنِي عِلْمًا",
            pronunciation = "Rabbi zidni 'ilma.",
            translation = "হে আমার প্রতিপালক! আমার জ্ঞান বৃদ্ধি করে দিন।",
            reference = "আল-কুরআন, সূরা ত্বহা: ১১৪",
            category = "জ্ঞান ও আমল"
        )
    )

    val categories: List<String> = listOf("দৈনিক দোয়া", "খাবার ও পানীয়", "পরিবার ও সন্তান", "বিপদ ও দুশ্চিন্তা", "জ্ঞান ও আমল")
}

object HadithData {
    val hadithList: List<com.example.model.Hadith> = listOf(
        com.example.model.Hadith(
            id = "1",
            title = "নিয়তের গুরুত্ব",
            arabic = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ",
            pronunciation = "Innamal a'malu bin niyyat.",
            translation = "সকল কাজের ফলাফল নিয়তের ওপর নির্ভরশীল।",
            reference = "সহীহ বুখারী: ১",
            category = "ঈমান ও নিয়ত"
        )
    )
    val categories: List<String> = listOf("ঈমান ও নিয়ত")
}

class DuaStorage(context: android.content.Context) {
    private val prefs = context.getSharedPreferences("saved_duas_prefs", android.content.Context.MODE_PRIVATE)

    fun isDuaSaved(id: String): Boolean {
        return prefs.getBoolean(id, false)
    }

    fun toggleSavedDua(id: String) {
        val current = isDuaSaved(id)
        prefs.edit().putBoolean(id, !current).apply()
    }

    fun getAllSavedIds(): Set<String> {
        return prefs.all.filter { it.value == true }.keys
    }
}
