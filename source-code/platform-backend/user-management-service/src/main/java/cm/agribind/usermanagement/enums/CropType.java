// CRITICAL FIX: CropType.java
// Replace the entire enum with this updated version

package cm.agribind.usermanagement.enums;

public enum CropType {
    // Food Crops
    MAIZE,
    RICE,
    MILLET,
    SORGHUM,
    BEANS,
    POTATOES,
    CASSAVA,
    YAMS,
    PLANTAIN,      // ✅ ADDED - was missing
    PLANTAINS,     // ✅ ADDED - plural form

    // Cash Crops
    COCOA,
    COFFEE,
    COTTON,
    BANANAS,       // ✅ Already existed
    PALM_OIL,
    RUBBER,
    TEA,
    SUGARCANE,

    // Vegetables
    TOMATOES,
    ONIONS,
    CARROTS,
    CABBAGE,
    PEPPERS,
    OKRA,
    EGGPLANT,

    // Fruits
    MANGOES,
    ORANGES,
    PINEAPPLES,
    AVOCADOS,
    PAPAYAS,

    // Other
    GROUNDNUTS,
    SOYBEANS,
    SESAME
}