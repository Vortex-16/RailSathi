package com.example.data.model

enum class UserRole {
    TRAVELER,
    VENDOR,
    GUEST
}

enum class RequestStatus {
    PENDING,
    ASSIGNED,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED
}

data class FoodItem(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val nameBn: String,
    val nameMr: String,
    val defaultPrice: Int,
    val category: String,
    val isVeg: Boolean = true,
    val isJain: Boolean = false,
    val isSeniorFriendly: Boolean = false,
    val emoji: String = "🍲",
    val description: String = ""
)

object RegionalSnacksCatalog {
    val items = listOf(
        FoodItem(
            id = "jhalmuri_kol",
            nameEn = "Kolkata Jhalmuri",
            nameHi = "कोलकाता झालमुड़ी (मसाला मुड़ी)",
            nameBn = "কলকাতা খাঁটি ঝালমুড়ি",
            nameMr = "कोलकाता झालमुरी",
            defaultPrice = 20,
            category = "Snacks",
            isVeg = true,
            isJain = false,
            isSeniorFriendly = true,
            emoji = "🥣",
            description = "Fresh spicy puffed rice with mustard oil, boiled potato, coconut and crunchy mixture"
        ),
        FoodItem(
            id = "badam_roasted",
            nameEn = "Roasted Peanuts (Garam Badam)",
            nameHi = "भुनी हुई गरम मूंगफली / बादाम",
            nameBn = "ভাজা গরম বাদাম",
            nameMr = "गरम भाजलेले शेंगदाणे",
            defaultPrice = 15,
            category = "Nuts",
            isVeg = true,
            isJain = true,
            isSeniorFriendly = false,
            emoji = "🥜",
            description = "Hot sand-roasted crunchy peanuts with rock salt"
        ),
        FoodItem(
            id = "chana_jor",
            nameEn = "Spicy Chana Jor Garam",
            nameHi = "चटपटा चना जोर गरम",
            nameBn = "ঝাল চানা জোর গরম",
            nameMr = "मसालेदार चना जोर गरम",
            defaultPrice = 20,
            category = "Snacks",
            isVeg = true,
            isJain = true,
            isSeniorFriendly = false,
            emoji = "🌶️",
            description = "Flattened pressed black gram tossed with tangy lemon, green chilli and spice mix"
        ),
        FoodItem(
            id = "masala_chai",
            nameEn = "Cutting Masala Chai / Tea",
            nameHi = "कटिंग मसाला चाय (कुल्हड़/कप)",
            nameBn = "গরম মশলা চা / লাল চা",
            nameMr = "कटिंग मसाला चहा",
            defaultPrice = 10,
            category = "Beverages",
            isVeg = true,
            isJain = true,
            isSeniorFriendly = true,
            emoji = "☕",
            description = "Cardamom and ginger infused hot fresh milk tea"
        ),
        FoodItem(
            id = "vada_pav_mum",
            nameEn = "Mumbai Local Vada Pav",
            nameHi = "मुंबई गरम वड़ा पाव व चटनी",
            nameBn = "মুম্বাই গরম বড়া পাও",
            nameMr = "गरमागरम वडा पाव व लसूण चटणी",
            defaultPrice = 20,
            category = "Warm Food",
            isVeg = true,
            isJain = false,
            isSeniorFriendly = true,
            emoji = "🥪",
            description = "Crispy spiced potato dumpling in fresh pav with dry garlic chutney"
        ),
        FoodItem(
            id = "samosa_singara",
            nameEn = "Crispy Samosa / Singara (2 pcs)",
            nameHi = "गरमा-गरम समोसा (2 पीस)",
            nameBn = "মুচমুচে সিঙাড়া (২ পিস)",
            nameMr = "खमंग समोसा",
            defaultPrice = 20,
            category = "Warm Food",
            isVeg = true,
            isJain = false,
            isSeniorFriendly = true,
            emoji = "🥟",
            description = "Flaky crust stuffed with spiced potatoes, green peas and peanuts"
        ),
        FoodItem(
            id = "fresh_cut_fruits",
            nameEn = "Fresh Cut Fruits (Papaya/Guava)",
            nameHi = "ताज़े कटे फल (अमरूद / पपीता चाट)",
            nameBn = "তাজা কাটা পেঁপে ও পেয়ারা",
            nameMr = "ताजी चिरलेली फळे (पेरू/पपई)",
            defaultPrice = 30,
            category = "Healthy",
            isVeg = true,
            isJain = true,
            isSeniorFriendly = true,
            emoji = "🍉",
            description = "Hygienic sliced seasonal fruits with black salt and cumin powder (Sugar-safe)"
        ),
        FoodItem(
            id = "soft_poha",
            nameEn = "Steamed Soft Kanda Poha",
            nameHi = "भाप से बना नरम पोहा",
            nameBn = "নরম সুস্বাদু পোহা / চিঁড়ে",
            nameMr = "मऊ लुसलुशीत कांदे पोहे",
            defaultPrice = 25,
            category = "Warm Food",
            isVeg = true,
            isJain = false,
            isSeniorFriendly = true,
            emoji = "🍚",
            description = "Light, non-spicy fluffy flattened rice with curry leaves and peanuts - easy to digest"
        ),
        FoodItem(
            id = "chocolates_biscuit",
            nameEn = "Chocolates & Glucose Biscuits",
            nameHi = "चॉकलेट्स व ग्लूकोज बिस्कुट पैकेट",
            nameBn = "চকোলেট ও গ্লুকোজ বিস্কুট",
            nameMr = "चॉकलेट्स आणि ग्लुकोज बिस्किटे",
            defaultPrice = 10,
            category = "Packaged",
            isVeg = true,
            isJain = true,
            isSeniorFriendly = true,
            emoji = "🍫",
            description = "Sealed brand biscuits & milk chocolate bars for quick energy"
        ),
        FoodItem(
            id = "water_bottle",
            nameEn = "Packaged Chilled Drinking Water",
            nameHi = "ठंडा सीलबंद पीने का पानी (1L)",
            nameBn = "ঠান্ডা সিল করা পানীয় জল",
            nameMr = "थंड सीलबंद पिण्याचे पाणी",
            defaultPrice = 15,
            category = "Beverages",
            isVeg = true,
            isJain = true,
            isSeniorFriendly = true,
            emoji = "💧",
            description = "ISI certified sealed chilled mineral water bottle"
        )
    )
}
