package org.example.eventplatform.event.entity;

public enum EventType {
    LUNAR_NEW_YEAR("Tết Nguyên Đán"),
    MID_AUTUMN("Tết Trung Thu"),
    LONGEVITY_WISH("Mừng Thọ"),
    WEDDING("Đám Cưới"),
    HOUSEWARMING("Tân Gia"),
    GRAND_OPENING("Khai Trương"),
    GROUNDBREAKING("Động Thổ"),
    CORPORATE_PARTY("Tiệc Công Ty"),
    PRODUCT_LAUNCH("Ra Mắt Sản Phẩm"),
    FESTIVAL("Lễ Hội"),
    TEMPLE_CEREMONY("Cúng Đình/Miếu"),
    COMPETITION("Thi Đấu");

    private final String displayName;

    EventType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
