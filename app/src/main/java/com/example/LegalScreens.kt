package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GlobalLanguage

@Composable
fun LegalScreenTemplate(titleEn: String, titleBn: String, contentEn: String, contentBn: String, onBack: () -> Unit) {
    val title = if (GlobalLanguage.isEnglish) titleEn else titleBn
    val contentText = if (GlobalLanguage.isEnglish) contentEn else contentBn

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .background(Color(0xFFF3F4F6), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1F2937),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = contentText,
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = Color(0xFF4B5563)
            )
        }
    }
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    LegalScreenTemplate(
        titleEn = "Privacy Policy",
        titleBn = "গোপনীয়তা নীতি",
        contentEn = "Privacy Policy\n\nWelcome to our App. We respect your privacy and are committed to protecting your personal data.\n\n1. Data Collection:\nWe collect data to provide better services to our users. We may collect personal information such as your name, email address, and location data when you use the app features.\n\n2. Data Usage:\nYour data is used to personalize your experience, provide essential app functionalities, and improve our services.\n\n3. Data Sharing:\nWe do not share your personal data with third-party companies for marketing purposes without your explicit consent.\n\n4. Data Security:\nWe implement various security measures to maintain the safety of your personal information.\n\n5. Permissions:\nOur app requests certain permissions (e.g., location, background location) to function properly. We only access this data when necessary.\n\nFor more details, please contact our support team.",
        contentBn = "গোপনীয়তা নীতি\n\nআমাদের অ্যাপে আপনাকে স্বাগতম। আমরা আপনার গোপনীয়তাকে সম্মান করি এবং আপনার ব্যক্তিগত ডেটা সুরক্ষিত রাখতে প্রতিশ্রুতিবদ্ধ।\n\n১. ডেটা সংগ্রহ:\nআমরা ব্যবহারকারীদের উন্নত পরিষেবা দিতে ডেটা সংগ্রহ করি। অ্যাপের ফিচার ব্যবহারের সময় আমরা আপনার নাম, ইমেইল এবং লোকেশনের মতো তথ্য সংগ্রহ করতে পারি।\n\n২. ডেটার ব্যবহার:\nআপনার ডেটা অ্যাপের অভিজ্ঞতা ব্যক্তিগতকৃত করতে, প্রয়োজনীয় ফাংশন প্রদান করতে এবং আমাদের পরিষেবা উন্নত করতে ব্যবহৃত হয়।\n\n৩. ডেটা শেয়ারিং:\nআপনার স্পষ্ট সম্মতি ছাড়া আমরা মার্কেটিংয়ের উদ্দেশ্যে তৃতীয় পক্ষের সাথে আপনার ব্যক্তিগত ডেটা শেয়ার করি না।\n\n৪. ডেটা নিরাপত্তা:\nআপনার ব্যক্তিগত তথ্যের নিরাপত্তা বজায় রাখতে আমরা বিভিন্ন পদক্ষেপ গ্রহণ করি।\n\n৫. পারমিশন:\nঅ্যাপটি সঠিকভাবে কাজ করার জন্য কিছু অনুমতির (যেমন: লোকেশন) প্রয়োজন হয়। প্রয়োজন হলেই কেবল আমরা এই ডেটা অ্যাক্সেস করি।\n\nআরও বিস্তারিত জানতে, অনুগ্রহ করে আমাদের সাপোর্ট টিমের সাথে যোগাযোগ করুন।"
        , onBack = onBack
    )
}

@Composable
fun TermsConditionsScreen(onBack: () -> Unit) {
    LegalScreenTemplate(
        titleEn = "Terms & Conditions",
        titleBn = "শর্তাবলী",
        contentEn = "Terms & Conditions\n\nBy downloading or using the app, these terms will automatically apply to you – you should make sure therefore that you read them carefully before using the app.\n\n1. License to Use:\nWe grant you a personal, non-exclusive, non-transferable license to use the app for personal and non-commercial purposes.\n\n2. User Responsibilities:\nYou agree not to misuse the app or use it for any illegal activities. You are responsible for any activities that occur under your account.\n\n3. Intellectual Property:\nAll trademarks, copyrights, database rights, and other intellectual property rights related to the app belong to us.\n\n4. Updates & Changes:\nWe may update the app at any time and may also change or stop providing the app or its services without prior notice.\n\n5. Limitation of Liability:\nWe shall not be liable for any indirect, incidental, special, or consequential damages resulting from the use or inability to use the app.",
        contentBn = "শর্তাবলী\n\nঅ্যাপটি ডাউনলোড বা ব্যবহার করার মাধ্যমে এই শর্তাবলী আপনার জন্য প্রযোজ্য হবে। তাই অ্যাপটি ব্যবহারের আগে শর্তগুলো ভালোভাবে পড়ে নেওয়া উচিত।\n\n১. ব্যবহারের লাইসেন্স:\nআমরা আপনাকে ব্যক্তিগত এবং অ-বাণিজ্যিক উদ্দেশ্যে অ্যাপটি ব্যবহারের জন্য একটি লাইসেন্স প্রদান করছি।\n\n২. ব্যবহারকারীর দায়িত্ব:\nআপনি অ্যাপটির অপব্যবহার বা বেআইনি কোনো কাজে এটি ব্যবহার না করার ব্যাপারে সম্মত হচ্ছেন।\n\n৩. বুদ্ধিবৃত্তিক সম্পত্তি:\nঅ্যাপের সাথে সম্পর্কিত সমস্ত ট্রেডমার্ক, কপিরাইট এবং অন্যান্য অধিকার আমাদের মালিকানাধীন।\n\n৪. আপডেট ও পরিবর্তন:\nআমরা যেকোনো সময় অ্যাপ আপডেট করতে পারি এবং পূর্ব নোটিশ ছাড়াই অ্যাপ বা এর পরিষেবা পরিবর্তন বা বন্ধ করতে পারি।\n\n৫. দায়বদ্ধতার সীমাবদ্ধতা:\nঅ্যাপটি ব্যবহার বা ব্যবহারে অক্ষমতার ফলে উদ্ভূত কোনো পরোক্ষ বা আনুষঙ্গিক ক্ষতির জন্য আমরা দায়ী থাকব না।"
        , onBack = onBack
    )
}

@Composable
fun DisclaimerScreen(onBack: () -> Unit) {
    LegalScreenTemplate(
        titleEn = "Disclaimer",
        titleBn = "দাবিত্যাগ",
        contentEn = "Disclaimer\n\nThe information provided by our app is for general informational purposes only. All information on the app is provided in good faith, however, we make no representation or warranty of any kind, express or implied, regarding the accuracy, adequacy, validity, reliability, availability, or completeness of any information.\n\nUnder no circumstance shall we have any liability to you for any loss or damage of any kind incurred as a result of the use of the app or reliance on any information provided on the app. Your use of the app and your reliance on any information on the app is solely at your own risk.",
        contentBn = "দাবিত্যাগ\n\nআমাদের অ্যাপের তথ্য শুধুমাত্র সাধারণ জ্ঞানমূলক উদ্দেশ্যে প্রদান করা হয়েছে। অ্যাপের সকল তথ্য সদিচ্ছায় দেওয়া হয়েছে, তবে তথ্যের নির্ভুলতা, সম্পূর্ণতা বা নির্ভরযোগ্যতার ব্যাপারে আমরা কোনো ধরনের নিশ্চয়তা প্রদান করি না।\n\nঅ্যাপ ব্যবহারের ফলে বা তথ্যের ওপর নির্ভর করার কারণে কোনো ক্ষতি বা লোকসান হলে কোনো অবস্থাতেই আমরা দায়ী থাকব না। অ্যাপটির ব্যবহার সম্পূর্ণ আপনার নিজ দায়িত্বে করতে হবে।"
        , onBack = onBack
    )
}

@Composable
fun AboutAppScreen(onBack: () -> Unit) {
    LegalScreenTemplate(
        titleEn = "About App",
        titleBn = "অ্যাপ সম্পর্কে",
        contentEn = "About App\n\nApp Name: Islamic Assistant\nVersion: 1.0.0\n\nThis app is designed to help Muslims in their daily lives with features like Prayer Times, Quran, Hadith, Qibla Compass, Tasbih, and more. Our goal is to provide a comprehensive and user-friendly platform for all your Islamic needs.\n\nThank you for using our app. May Allah bless you.",
        contentBn = "অ্যাপ সম্পর্কে\n\nঅ্যাপের নাম: ইসলামিক অ্যাসিস্ট্যান্ট\nভার্সন: 1.0.0\n\nএই অ্যাপটি মুসলমানদের দৈনন্দিন জীবনে নামাজের সময়, কুরআন, হাদিস, কিবলা কম্পাস, তাসবিহ সহ বিভিন্ন ফিচারের মাধ্যমে সহায়তা করার জন্য তৈরি করা হয়েছে। আমাদের লক্ষ্য হলো আপনার সমস্ত ইসলামিক প্রয়োজনের জন্য একটি ব্যবহারকারী-বান্ধব প্ল্যাটফর্ম প্রদান করা।\n\nআমাদের অ্যাপটি ব্যবহার করার জন্য ধন্যবাদ। আল্লাহ আপনার মঙ্গল করুন।"
        , onBack = onBack
    )
}

@Composable
fun ContactUsScreen(onBack: () -> Unit) {
    LegalScreenTemplate(
        titleEn = "Contact Us",
        titleBn = "যোগাযোগ করুন",
        contentEn = "Contact Us\n\nWe would love to hear from you! If you have any questions, feedback, or suggestions, please reach out to us.\n\nEmail: support@example.com\nWebsite: www.example.com\n\nYou can also contact us through our official social media channels available in the Support section of the app.",
        contentBn = "যোগাযোগ করুন\n\nআমরা আপনার মতামত শুনতে আগ্রহী! আপনার যদি কোনো প্রশ্ন বা পরামর্শ থাকে, তাহলে আমাদের সাথে যোগাযোগ করুন।\n\nইমেইল: support@example.com\nওয়েবসাইট: www.example.com\n\nএছাড়াও অ্যাপের সাপোর্ট সেকশনে থাকা আমাদের অফিসিয়াল সোশ্যাল মিডিয়া চ্যানেলের মাধ্যমে যোগাযোগ করতে পারেন।"
        , onBack = onBack
    )
}
