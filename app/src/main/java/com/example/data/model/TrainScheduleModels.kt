package com.example.data.model

enum class StationConfidence {
    HIGH,      // Within tight geofence with high accuracy GPS (<350m, accuracy <= 60m)
    MEDIUM,    // In vicinity (<1000m, accuracy <= 150m)
    LOW,       // Marginal (<1500m, accuracy <= 250m)
    UNKNOWN,   // Stale or low accuracy
    NONE       // User at home / off-track (>1500m)
}

enum class TrainConfidence {
    HIGH,
    MEDIUM,
    LOW,
    UNKNOWN
}

enum class TrainStatus {
    UPCOMING,
    BOARDING_SOON,
    DELAYED,
    DEPARTED,
    CANCELLED,
    UNKNOWN
}

data class Station(
    val code: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val zone: String = "",
    val state: String = ""
)

data class StationDeparture(
    val stationCode: String,
    val trainNumber: String,
    val trainName: String,
    val scheduledDeparture: String,
    val estimatedDeparture: String = "",
    val actualDeparture: String = "",
    val status: TrainStatus = TrainStatus.UPCOMING,
    val destination: String,
    val destinationCode: String = "",
    val platform: String = "PF 1",
    val delayMinutes: Int = 0,
    val sourceTimestamp: Long = System.currentTimeMillis()
)

data class LocationDiagnosticsInfo(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float = 0f,
    val altitudeMeters: Double = 0.0,
    val speedMps: Float = 0f,
    val bearingDegrees: Float = 0f,
    val timestampEpochMs: Long = 0L,
    val ageSeconds: Long = 0L,
    val provider: String = "none",
    val isGpsEnabled: Boolean = false,
    val isNetworkEnabled: Boolean = false,
    val permissionType: String = "NONE",
    val isMockLocation: Boolean = false,
    val qualityGatePass: Boolean = false
)

enum class CoachType {
    CAB_DIVYANG,      // Front / Rear Motor Cab + Divyangjan Compartment
    LADIES_SPECIAL,   // Reserved for Ladies (মহিলা স্পেশাল)
    VENDOR_LUGGAGE,   // Vendor / Hawker & Heavy Luggage Coach (সब्जी / फेरीवाला डिब्बा)
    GENERAL           // General Unreserved Passenger Coach (सामान्य)
}

data class EmuCoach(
    val coachIndex: Int,
    val coachCode: String,
    val nameEn: String,
    val nameHi: String,
    val nameBn: String,
    val type: CoachType,
    val isVendorAllowed: Boolean = true,
    val description: String = ""
)

data class StationStopTime(
    val stationCode: String,
    val stationName: String,
    val platform: String,
    val arrivalTime: String,
    val departureTime: String,
    val haltSeconds: Int = 45,
    val distanceKm: Double = 0.0
)

data class RailwayStation(
    val code: String,
    val nameEn: String,
    val nameHi: String,
    val nameBn: String,
    val division: String,
    val latitude: Double,
    val longitude: Double,
    val platforms: List<String> = listOf("PF 1", "PF 2", "PF 3", "PF 4")
)

data class LocalTrainSchedule(
    val trainNumber: String,
    val trainName: String,
    val originStationCode: String,
    val destStationCode: String,
    val zone: String,
    val stops: List<StationStopTime>,
    val coaches: List<EmuCoach>,
    val frequencyNote: String = "Runs Daily (Every 15-20 mins)",
    val rakeType: String = "9-Car / 12-Car Suburban EMU"
)

object AuthenticEmuFormations {
    // Exact West Bengal / Eastern Railway (Sealdah & Howrah) 9-Coach EMU Formation:
    // Engine+Divyang -> Ladies -> Vendor -> GS-1 -> GS-2 -> GS-3 -> Vendor -> Ladies -> Engine+Divyang
    val easternRailway9CarRake = listOf(
        EmuCoach(
            coachIndex = 1,
            coachCode = "CAB-1",
            nameEn = "Front Engine + Divyangjan",
            nameHi = "आगे का इंजन + दिव्यांग कम्पार्टमेंट",
            nameBn = "সামনের ইঞ্জিন + প্রতিবন্ধী কামরা",
            type = CoachType.CAB_DIVYANG,
            isVendorAllowed = false,
            description = "Driving Cab with reserved section for Persons with Disabilities"
        ),
        EmuCoach(
            coachIndex = 2,
            coachCode = "LD-1",
            nameEn = "Front Ladies Coach",
            nameHi = "महिला स्पेशल डिब्बा",
            nameBn = "মহিলা স্পেশাল কামরা",
            type = CoachType.LADIES_SPECIAL,
            isVendorAllowed = true,
            description = "Dedicated Coach reserved for Women Commuters"
        ),
        EmuCoach(
            coachIndex = 3,
            coachCode = "VND-1",
            nameEn = "Vendor & Luggage Coach",
            nameHi = "फेरीवाला व सब्जी/सामान डिब्बा",
            nameBn = "হকার ও সবজি/ভারী মালপত্র কামরা",
            type = CoachType.VENDOR_LUGGAGE,
            isVendorAllowed = true,
            description = "Prime Hawker compartment for vegetable, chai & regional food vendors"
        ),
        EmuCoach(
            coachIndex = 4,
            coachCode = "GS-1",
            nameEn = "General Coach 1",
            nameHi = "सामान्य डिब्बा १",
            nameBn = "সাধারণ কামরা ১",
            type = CoachType.GENERAL,
            isVendorAllowed = true,
            description = "General Passenger Compartment"
        ),
        EmuCoach(
            coachIndex = 5,
            coachCode = "GS-2",
            nameEn = "General Coach 2 (Middle)",
            nameHi = "सामान्य डिब्बा २ (मध्य)",
            nameBn = "সাধারণ কামরা ২ (মাঝের)",
            type = CoachType.GENERAL,
            isVendorAllowed = true,
            description = "High capacity mid-rake commuter coach"
        ),
        EmuCoach(
            coachIndex = 6,
            coachCode = "GS-3",
            nameEn = "General Coach 3",
            nameHi = "सामान्य डिब्बा ३",
            nameBn = "সাধারণ কামরা ৩",
            type = CoachType.GENERAL,
            isVendorAllowed = true,
            description = "General Passenger Compartment"
        ),
        EmuCoach(
            coachIndex = 7,
            coachCode = "VND-2",
            nameEn = "Rear Vendor & Luggage Coach",
            nameHi = "पीछे का फेरीवाला/सब्जी डिब्बा",
            nameBn = "পেছনের হকার ও মালপত্র কামরা",
            type = CoachType.VENDOR_LUGGAGE,
            isVendorAllowed = true,
            description = "Secondary Hawker & Goods carriage"
        ),
        EmuCoach(
            coachIndex = 8,
            coachCode = "LD-2",
            nameEn = "Rear Ladies Coach",
            nameHi = "पीछे का महिला स्पेशल डिब्बा",
            nameBn = "পেছনের মহিলা স্পেশাল কামরা",
            type = CoachType.LADIES_SPECIAL,
            isVendorAllowed = true,
            description = "Rear Ladies reserved coach"
        ),
        EmuCoach(
            coachIndex = 9,
            coachCode = "CAB-2",
            nameEn = "Rear Engine + Divyangjan",
            nameHi = "पीछे का इंजन + दिव्यांग कम्पार्टमेंट",
            nameBn = "পেছনের ইঞ্জিন + প্রতিবন্ধী কামরা",
            type = CoachType.CAB_DIVYANG,
            isVendorAllowed = false,
            description = "Rear Driving Motor Coach with Divyangjan section"
        )
    )

    // 12-Car Suburban EMU Rake (Mumbai / Kolkata high-density routes)
    val standard12CarRake = listOf(
        EmuCoach(1, "CAB-1", "Front Engine + Divyang", "आगे का इंजन + दिव्यांग", "সামনের ইঞ্জিন + প্রতিবন্ধী", CoachType.CAB_DIVYANG, false),
        EmuCoach(2, "LD-1", "Front Ladies", "महिला डिब्बा", "মহিলা কামরা", CoachType.LADIES_SPECIAL, true),
        EmuCoach(3, "VND-1", "Front Vendor Luggage", "फेरीवाला डिब्बा", "হকার কামরা", CoachType.VENDOR_LUGGAGE, true),
        EmuCoach(4, "GS-1", "General Coach 1", "सामान्य डिब्बा १", "সাধারণ কামরা ১", CoachType.GENERAL, true),
        EmuCoach(5, "GS-2", "General Coach 2", "सामान्य डिब्बा २", "সাধারণ কামরা ২", CoachType.GENERAL, true),
        EmuCoach(6, "VND-MID", "Mid Vendor Coach", "मध्य फेरीवाला डिब्बा", "মাঝের হকার কামরা", CoachType.VENDOR_LUGGAGE, true),
        EmuCoach(7, "GS-3", "General Coach 3", "सामान्य डिब्बा ३", "সাধারণ কামরা ३", CoachType.GENERAL, true),
        EmuCoach(8, "GS-4", "General Coach 4", "सामान्य डिब्बा ४", "সাধারণ কামরা ৪", CoachType.GENERAL, true),
        EmuCoach(9, "GS-5", "General Coach 5", "सामान्य डिब्बा ५", "সাধারণ কামরা ৫", CoachType.GENERAL, true),
        EmuCoach(10, "VND-2", "Rear Vendor Coach", "पीछे का फेरीवाला डिब्बा", "পেছনের হকার কামরা", CoachType.VENDOR_LUGGAGE, true),
        EmuCoach(11, "LD-2", "Rear Ladies Coach", "पीछे का महिला डिब्बा", "পেছনের মহিলা কামরা", CoachType.LADIES_SPECIAL, true),
        EmuCoach(12, "CAB-2", "Rear Engine + Divyang", "पीछे का इंजन + दिव्यांग", "পেছনের ইঞ্জিন + প্রতিবন্ধী", CoachType.CAB_DIVYANG, false)
    )
}

object IndianLocalRailwayDatabase {
    // Major Local Stations in West Bengal (Sealdah & Howrah Divisions) and major suburban lines
    val allStations = listOf(
        // Eastern Railway & Kolkata Suburban
        RailwayStation("SDAH", "Sealdah", "सियालदह", "শিয়ালদহ", "Eastern Railway (Sealdah)", 22.5697, 88.3713, listOf("PF 1A", "PF 1B", "PF 2", "PF 3", "PF 4A", "PF 4B")),
        RailwayStation("DDJ", "Dum Dum Jn", "दमदम जंक्शन", "দমদম জংশন", "Eastern Railway (Sealdah)", 22.6222, 88.3934, listOf("PF 1", "PF 2", "PF 3", "PF 4")),
        RailwayStation("BLH", "Belgharia", "बेलघरिया", "বেলঘরিয়া", "Eastern Railway (Sealdah)", 22.6617, 88.3846, listOf("PF 1", "PF 2", "PF 3", "PF 4")),
        RailwayStation("AGP", "Agarpara", "अगरपारा", "আগরপাড়া", "Eastern Railway (Sealdah)", 22.6806, 88.3817, listOf("PF 1", "PF 2", "PF 3")),
        RailwayStation("SEP", "Sodpur", "सोदपुर", "সোদপুর", "Eastern Railway (Sealdah)", 22.6983, 88.3800, listOf("PF 1", "PF 2", "PF 3", "PF 4")),
        RailwayStation("KDH", "Khardaha", "खरदह", "খড়দহ", "Eastern Railway (Sealdah)", 22.7214, 88.3789, listOf("PF 1", "PF 2")),
        RailwayStation("TGH", "Titagarh", "टिटागढ़", "টিটাগড়", "Eastern Railway (Sealdah)", 22.7410, 88.3776, listOf("PF 1", "PF 2")),
        RailwayStation("BP", "Barrackpore", "बैरकपुर", "ব্যারাকপুর", "Eastern Railway (Sealdah)", 22.7634, 88.3771, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),
        RailwayStation("PTF", "Palta", "पलटा", "পাল্টা", "Eastern Railway (Sealdah)", 22.7842, 88.3791, listOf("PF 1", "PF 2")),
        RailwayStation("IP", "Ichhapur", "इच्छापुर", "ইছাপুর", "Eastern Railway (Sealdah)", 22.8051, 88.3820, listOf("PF 1", "PF 2")),
        RailwayStation("SNR", "Shyamnagar", "श्यामनगर", "শ্যামনগর", "Eastern Railway (Sealdah)", 22.8258, 88.3975, listOf("PF 1", "PF 2", "PF 3", "PF 4")),
        RailwayStation("NH", "Naihati Jn", "नैहाटी जंक्शन", "নৈহাটি জংশন", "Eastern Railway (Sealdah)", 22.8912, 88.4239, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),
        RailwayStation("KPA", "Kanchrapara", "काँचरापाड़ा", "কাঁচরাপাড়া", "Eastern Railway (Sealdah)", 22.9431, 88.4385, listOf("PF 1", "PF 2")),
        RailwayStation("KYI", "Kalyani", "कल्याणी", "কল্যাণী", "Eastern Railway (Sealdah)", 22.9751, 88.4344, listOf("PF 1", "PF 2", "PF 3")),
        RailwayStation("RHA", "Ranaghat Jn", "रानाघाट जंक्शन", "রানাঘাট জংশন", "Eastern Railway (Sealdah)", 23.1804, 88.5670, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),
        RailwayStation("STB", "Shantipur Jn", "शांतिपुर", "শান্তিপুর", "Eastern Railway (Sealdah)", 23.2458, 88.4339, listOf("PF 1", "PF 2", "PF 3")),
        RailwayStation("KNJ", "Krishnanagar City", "कृष्णनगर सिटी", "কৃষ্ণনগর সিটি", "Eastern Railway (Sealdah)", 23.4013, 88.4983, listOf("PF 1", "PF 2", "PF 3")),
        RailwayStation("BNJ", "Bongaon Jn", "बनगाँव जंक्शन", "বনগাঁ জংশন", "Eastern Railway (Sealdah)", 23.0450, 88.8250, listOf("PF 1", "PF 2", "PF 3")),
        RailwayStation("HWH", "Howrah Jn", "हावड़ा जंक्शन", "হাওড়া জংশন", "Eastern Railway (Howrah)", 22.5850, 88.3426, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6", "PF 7", "PF 8")),
        RailwayStation("KOAA", "Kolkata Chitpur", "कोलकाता चितपुर", "কলকাতা চিৎপুর", "Eastern Railway (Sealdah)", 22.6022, 88.3735, listOf("PF 1", "PF 2", "PF 3", "PF 4")),
        RailwayStation("BDC", "Bandel Jn", "बैंडेल जंक्शन", "ব্যান্ডেল জংশন", "Eastern Railway (Howrah)", 22.9234, 88.3789, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),
        RailwayStation("BWN", "Barddhaman Jn", "बर्द्धमान जंक्शन", "বর্ধমান জংশন", "Eastern Railway (Howrah)", 23.2324, 87.8615, listOf("PF 1", "PF 2", "PF 3", "PF 4")),
        RailwayStation("KGP", "Kharagpur Jn", "खड़गपुर जंक्शन", "খড়গপুর জংশন", "South Eastern Railway", 22.3297, 87.3213, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),
        RailwayStation("ASN", "Asansol Jn", "आसनसोल जंक्शन", "আসানসোল জংশন", "Eastern Railway", 23.6889, 86.9661, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),
        RailwayStation("DHN", "Dhanbad Jn", "धनबाद जंक्शन", "ধানবাদ জংশন", "East Central Railway", 23.7957, 86.4304, listOf("PF 1", "PF 2", "PF 3", "PF 4")),
        
        // Bihar & East Central Railway
        RailwayStation("PNBE", "Patna Jn", "पटना जंक्शन", "পাটনা জংশন", "East Central Railway (Danapur)", 25.6023, 85.1376, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6", "PF 7", "PF 8")),
        RailwayStation("RJPB", "Rajendra Nagar", "राजेन्द्र नगर", "রাজেন্দ্র নগর", "East Central Railway (Danapur)", 25.5975, 85.1633, listOf("PF 1", "PF 2", "PF 3", "PF 4")),
        RailwayStation("DNR", "Danapur", "दानापुर", "দানাপুর", "East Central Railway (Danapur)", 25.6267, 85.0442, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),
        RailwayStation("PPTA", "Patliputra Jn", "पाटलिपुत्र जंक्शन", "পাটলিপুত্র জংশন", "East Central Railway (Danapur)", 25.6375, 85.0934, listOf("PF 1", "PF 2", "PF 3")),
        RailwayStation("ARA", "Ara Jn", "आरा जंक्शन", "আরা জংশন", "East Central Railway (Danapur)", 25.5562, 84.6644, listOf("PF 1", "PF 2", "PF 3", "PF 4")),
        RailwayStation("BXR", "Buxar", "बक्सर", "বক্সার", "East Central Railway (Danapur)", 25.5701, 83.9785, listOf("PF 1", "PF 2", "PF 3")),
        RailwayStation("GAYA", "Gaya Jn", "गया जंक्शन", "গয়া জংশন", "East Central Railway", 24.8055, 84.9995, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6")),
        RailwayStation("MFP", "Muzaffarpur Jn", "मुजफ्फरपुर जंक्शन", "মুজাফফরপুর জংশন", "East Central Railway", 26.1209, 85.3853, listOf("PF 1", "PF 2", "PF 3", "PF 4")),
        RailwayStation("DBG", "Darbhanga Jn", "दरभंगा जंक्शन", "দ্বারভাঙা জংশন", "East Central Railway", 26.1542, 85.8918, listOf("PF 1", "PF 2", "PF 3", "PF 4")),
        RailwayStation("SPJ", "Samastipur Jn", "समस्तीपुर जंक्शन", "সমস্তিপুর জংশন", "East Central Railway", 25.8628, 85.7811, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),
        RailwayStation("BJU", "Barauni Jn", "बरौनी जंक्शन", "বড়ৌনি জংশন", "East Central Railway", 25.4746, 85.9863, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),
        RailwayStation("KIR", "Katihar Jn", "कटिहार जंक्शन", "কাটিহার জংশন", "Northeast Frontier Railway", 25.5539, 87.5719, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),

        // Uttar Pradesh & Northern / North Central Railway
        RailwayStation("DDU", "Pt Deen Dayal Upadhyaya", "पंडित दीन दयाल उपाध्याय जं", "পণ্ডিত দীনদয়াল উপাধ্যায়", "East Central Railway", 25.2818, 83.1186, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6", "PF 7")),
        RailwayStation("BSB", "Varanasi Jn", "वाराणसी जंक्शन", "বারাণসী জংশন", "Northern Railway (Lucknow)", 25.3283, 82.9868, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6", "PF 7", "PF 8")),
        RailwayStation("PRYJ", "Prayagraj Jn (Allahabad)", "प्रयागराज जंक्शन", "প্রয়াগরাজ জংশন", "North Central Railway", 25.4484, 81.8286, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6", "PF 7", "PF 8")),
        RailwayStation("CNB", "Kanpur Central", "कानपुर सेंट्रल", "কানপুর সেন্ট্রাল", "North Central Railway", 26.4547, 80.3507, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6", "PF 7", "PF 8", "PF 9", "PF 10")),
        RailwayStation("LKO", "Lucknow Charbagh", "लखनऊ चारबाग", "লখনউ চারবাগ", "Northern Railway", 26.8322, 80.9192, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6")),
        RailwayStation("LJN", "Lucknow Jn NER", "लखनऊ जंक्शन", "লখনউ জংশন", "North Eastern Railway", 26.8310, 80.9205, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),
        RailwayStation("GKP", "Gorakhpur Jn", "गोरखपुर जंक्शन", "গোরখপুর জংশন", "North Eastern Railway", 26.7588, 83.3818, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6")),
        RailwayStation("AYC", "Ayodhya Cantt", "अयोध्या कैंट", "অযোধ্যা ক্যান্টনমেন্ট", "Northern Railway", 26.7769, 82.1388, listOf("PF 1", "PF 2", "PF 3")),
        RailwayStation("AGC", "Agra Cantt", "आगरा कैंट", "আগ্রা ক্যান্টনমেন্ট", "North Central Railway", 27.1578, 77.9904, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),
        RailwayStation("MB", "Moradabad Jn", "मुरादाबाद जंक्शन", "মুরাদাবাদ জংশন", "Northern Railway", 28.8312, 78.7766, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),

        // Delhi NCR
        RailwayStation("NDLS", "New Delhi", "नई दिल्ली", "নতুন দিল্লি", "Northern Railway (Delhi)", 28.6430, 77.2195, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6", "PF 7", "PF 8", "PF 9", "PF 10", "PF 11", "PF 12", "PF 13", "PF 14", "PF 15", "PF 16")),
        RailwayStation("DLI", "Old Delhi", "पुरानी दिल्ली", "পুরনো দিল্লি", "Northern Railway (Delhi)", 28.6606, 77.2285, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6", "PF 7", "PF 8")),
        RailwayStation("NZM", "Hazrat Nizamuddin", "हज़रत निज़ामुद्दीन", "হজরত নিজামুদ্দিন", "Northern Railway (Delhi)", 28.5888, 77.2536, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6", "PF 7")),
        RailwayStation("ANVT", "Anand Vihar Terminal", "आनंद विहार टर्मिनल", "আনন্দ বিহার টার্মিনাল", "Northern Railway (Delhi)", 28.6506, 77.3153, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6", "PF 7")),
        RailwayStation("GZB", "Ghaziabad Jn", "गाजियाबाद जंक्शन", "গাজিয়াবাদ জংশন", "Northern Railway (Delhi)", 28.6678, 77.4287, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6")),
        RailwayStation("DEC", "Delhi Cantt", "दिल्ली कैंट", "দিল্লি ক্যান্টনমেন্ট", "Northern Railway (Delhi)", 28.5912, 77.1264, listOf("PF 1", "PF 2", "PF 3", "PF 4")),
        RailwayStation("GGN", "Gurgaon", "गुरुग्राम", "গুরুগ্রাম", "Northern Railway (Delhi)", 28.4722, 77.0135, listOf("PF 1", "PF 2", "PF 3")),
        RailwayStation("FDB", "Faridabad", "फरीदाबाद", "ফরিদাবাদ", "Northern Railway (Delhi)", 28.4069, 77.3149, listOf("PF 1", "PF 2", "PF 3")),

        // Mumbai Suburban & Maharashtra
        RailwayStation("CSMT", "CSMT Mumbai", "सीएसएमटी मुंबई", "সিএসএমটি মুম্বাই", "Central Railway (Mumbai)", 18.9400, 72.8353, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6", "PF 7")),
        RailwayStation("DR", "Dadar", "दादर", "দাদার", "Central Railway (Mumbai)", 19.0178, 72.8437, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6", "PF 7")),
        RailwayStation("BVI", "Borivali", "बोरीवली", "বোরিবলি", "Western Railway (Mumbai)", 19.2290, 72.8573, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6", "PF 7")),
        RailwayStation("ADH", "Andheri", "अंधेरी", "অন্ধেরী", "Western Railway (Mumbai)", 19.1197, 72.8464, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),
        RailwayStation("TNA", "Thane", "ठाणे", "থানে", "Central Railway (Mumbai)", 19.1860, 72.9758, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6")),
        RailwayStation("KYN", "Kalyan Jn", "कल्याण जंक्शन", "কল্যাণ জংশন", "Central Railway (Mumbai)", 19.2437, 73.1355, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6", "PF 7")),
        RailwayStation("PNVL", "Panvel Jn", "पनवेल जंक्शन", "পানভেল জংশন", "Central Railway (Mumbai)", 18.9886, 73.1118, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),
        RailwayStation("PUNE", "Pune Jn", "पुणे जंक्शन", "পুনে জংশন", "Central Railway (Pune)", 18.5284, 73.8739, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6")),
        RailwayStation("NGP", "Nagpur Jn", "नागपुर जंक्शन", "নাগপুর জংশন", "Central Railway", 21.1524, 79.0888, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6")),

        // Gujarat & Rajasthan
        RailwayStation("ADI", "Ahmedabad Jn", "अहमदाबाद जंक्शन", "আহমেদাবাদ জংশন", "Western Railway", 23.0238, 72.6006, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6", "PF 7")),
        RailwayStation("ST", "Surat", "सूरत", "সুরাট", "Western Railway", 21.2049, 72.8407, listOf("PF 1", "PF 2", "PF 3", "PF 4")),
        RailwayStation("BRC", "Vadodara Jn", "वडोदरा जंक्शन", "ভাদোদরা জংশন", "Western Railway", 22.3106, 73.1812, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),
        RailwayStation("JP", "Jaipur Jn", "जयपुर जंक्शन", "জয়পুর জংশন", "North Western Railway", 26.9196, 75.7878, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),

        // South India
        RailwayStation("MAS", "Chennai Central", "चेन्नई सेंट्रल", "চেন্নাই সেন্ট্রাল", "Southern Railway (Chennai)", 13.0827, 80.2755, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6", "PF 7")),
        RailwayStation("MS", "Chennai Egmore", "चेन्नई एग्मोर", "চেন্নাই এগমোর", "Southern Railway (Chennai)", 13.0784, 80.2612, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),
        RailwayStation("MSB", "Chennai Beach", "चेन्नई बीच", "চেন্নাই বিচ", "Southern Railway (Chennai)", 13.0931, 80.2929, listOf("PF 1", "PF 2", "PF 3")),
        RailwayStation("TBM", "Tambaram", "तांबरम", "তাম্বরম", "Southern Railway (Chennai)", 12.9249, 80.1280, listOf("PF 1", "PF 2", "PF 3", "PF 4")),
        RailwayStation("SBC", "KSR Bengaluru City", "केएसआर बेंगलुरु सिटी", "কেএসআর বেঙ্গালুরু", "South Western Railway", 12.9781, 77.5696, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6", "PF 7")),
        RailwayStation("YPR", "Yesvantpur Jn", "यशवंतपुर जंक्शन", "যশবন্তপুর জংশন", "South Western Railway", 13.0238, 77.5503, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6")),
        RailwayStation("SC", "Secunderabad Jn", "सिकंदराबाद जंक्शन", "সেকেন্দ্রাবাদ জংশন", "South Central Railway", 17.4344, 78.5015, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6", "PF 7", "PF 8")),
        RailwayStation("HYB", "Hyderabad Deccan", "हैदराबाद दक्कन", "হায়দরাবাদ ডেকান", "South Central Railway", 17.3923, 78.4682, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),
        RailwayStation("KCG", "Kacheguda", "काचीगुड़ा", "কাচেগুড়া", "South Central Railway", 17.3876, 78.4984, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5")),
        RailwayStation("BBS", "Bhubaneswar", "भुवनेश्वर", "ভুবনেশ্বর", "East Coast Railway", 20.2666, 85.8436, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6")),
        RailwayStation("GHY", "Guwahati", "गुवाहाटी", "গুয়াহাটি", "Northeast Frontier Railway", 26.1827, 91.7508, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6")),
        RailwayStation("BPL", "Bhopal Jn", "भोपाल जंक्शन", "ভোপাল জংশন", "West Central Railway", 23.2662, 77.4144, listOf("PF 1", "PF 2", "PF 3", "PF 4", "PF 5", "PF 6"))
    )

    val allSchedules = listOf(
        LocalTrainSchedule(
            trainNumber = "31821",
            trainName = "Sealdah - Ranaghat Local",
            originStationCode = "SDAH",
            destStationCode = "RHA",
            zone = "Eastern Railway (Sealdah Main)",
            stops = listOf(
                StationStopTime("SDAH", "Sealdah", "PF 4B", "08:10", "08:10", 0, 0.0),
                StationStopTime("DDJ", "Dum Dum Jn", "PF 3", "08:22", "08:23", 60, 6.8),
                StationStopTime("BLH", "Belgharia", "PF 2", "08:28", "08:29", 45, 11.2),
                StationStopTime("SEP", "Sodpur", "PF 2", "08:33", "08:34", 45, 15.6),
                StationStopTime("BP", "Barrackpore", "PF 3", "08:42", "08:44", 90, 22.4),
                StationStopTime("SNR", "Shyamnagar", "PF 2", "08:52", "08:53", 45, 30.1),
                StationStopTime("NH", "Naihati Jn", "PF 4", "09:05", "09:07", 90, 37.8),
                StationStopTime("KPA", "Kanchrapara", "PF 2", "09:14", "09:15", 45, 44.9),
                StationStopTime("KYI", "Kalyani", "PF 2", "09:20", "09:21", 45, 48.2),
                StationStopTime("RHA", "Ranaghat Jn", "PF 2", "09:55", "09:55", 0, 73.5)
            ),
            coaches = AuthenticEmuFormations.easternRailway9CarRake,
            frequencyNote = "Every 15-20 Mins (Daily Commuter)"
        ),
        LocalTrainSchedule(
            trainNumber = "31317",
            trainName = "Sealdah - Kalyani Simanta Local",
            originStationCode = "SDAH",
            destStationCode = "KYI",
            zone = "Eastern Railway (Sealdah North)",
            stops = listOf(
                StationStopTime("SDAH", "Sealdah", "PF 1B", "09:00", "09:00", 0, 0.0),
                StationStopTime("DDJ", "Dum Dum Jn", "PF 3", "09:12", "09:13", 60, 6.8),
                StationStopTime("BP", "Barrackpore", "PF 2", "09:30", "09:31", 60, 22.4),
                StationStopTime("NH", "Naihati Jn", "PF 3", "09:50", "09:52", 90, 37.8),
                StationStopTime("KYI", "Kalyani", "PF 1", "10:08", "10:08", 0, 48.2)
            ),
            coaches = AuthenticEmuFormations.easternRailway9CarRake,
            frequencyNote = "Runs every 30 mins"
        ),
        LocalTrainSchedule(
            trainNumber = "37213",
            trainName = "Howrah - Bandel Local",
            originStationCode = "HWH",
            destStationCode = "BDC",
            zone = "Eastern Railway (Howrah Main)",
            stops = listOf(
                StationStopTime("HWH", "Howrah Jn", "PF 3", "07:45", "07:45", 0, 0.0),
                StationStopTime("BDC", "Bandel Jn", "PF 2", "08:42", "08:42", 0, 39.5)
            ),
            coaches = AuthenticEmuFormations.easternRailway9CarRake,
            frequencyNote = "Every 10-15 mins"
        ),
        LocalTrainSchedule(
            trainNumber = "37815",
            trainName = "Howrah - Barddhaman Main Local",
            originStationCode = "HWH",
            destStationCode = "BWN",
            zone = "Eastern Railway (Howrah Main)",
            stops = listOf(
                StationStopTime("HWH", "Howrah Jn", "PF 5", "08:20", "08:20", 0, 0.0),
                StationStopTime("BDC", "Bandel Jn", "PF 1", "09:15", "09:17", 90, 39.5),
                StationStopTime("BWN", "Barddhaman Jn", "PF 4", "10:40", "10:40", 0, 107.0)
            ),
            coaches = AuthenticEmuFormations.standard12CarRake,
            frequencyNote = "Every 25 mins"
        ),
        LocalTrainSchedule(
            trainNumber = "97051",
            trainName = "CSMT - Kalyan Slow Local",
            originStationCode = "CSMT",
            destStationCode = "KYN",
            zone = "Central Railway (Mumbai Main)",
            stops = listOf(
                StationStopTime("CSMT", "CSMT Mumbai", "PF 1", "08:30", "08:30", 0, 0.0),
                StationStopTime("TNA", "Thane", "PF 3", "09:20", "09:21", 60, 34.0),
                StationStopTime("KYN", "Kalyan Jn", "PF 4", "09:50", "09:50", 0, 54.0)
            ),
            coaches = AuthenticEmuFormations.standard12CarRake,
            frequencyNote = "Every 5-8 mins (Mumbai Peak)"
        ),
        LocalTrainSchedule(
            trainNumber = "40012",
            trainName = "Chennai Beach - Tambaram EMU",
            originStationCode = "MSB",
            destStationCode = "TBM",
            zone = "Southern Railway (Chennai Suburban)",
            stops = listOf(
                StationStopTime("MSB", "Chennai Beach", "PF 2", "08:15", "08:15", 0, 0.0),
                StationStopTime("TBM", "Tambaram", "PF 1", "09:05", "09:05", 0, 29.0)
            ),
            coaches = AuthenticEmuFormations.easternRailway9CarRake,
            frequencyNote = "Every 10 mins"
        ),
        LocalTrainSchedule(
            trainNumber = "64411",
            trainName = "New Delhi - Ghaziabad EMU",
            originStationCode = "NDLS",
            destStationCode = "GZB",
            zone = "Northern Railway (Delhi EMU)",
            stops = listOf(
                StationStopTime("NDLS", "New Delhi", "PF 7", "08:40", "08:40", 0, 0.0),
                StationStopTime("GZB", "Ghaziabad Jn", "PF 3", "09:35", "09:35", 0, 25.0)
            ),
            coaches = AuthenticEmuFormations.easternRailway9CarRake,
            frequencyNote = "Daily Suburban"
        )
    )
}
