package dev.book.accountbook.type;

import lombok.Getter;

@Getter
public enum Bank {
    KDB("0002"),       // 산업은행
    IBK("0003"),       // 기업은행
    KB("0004"),        // 국민은행
    SH("0007"),      // 수협은행
    NH("0011"),        // 농협은행
    WOORI("0020"),     // 우리은행
    SC("0023"),        // SC은행
    CITI("0027"),      // 씨티은행
    DAEGU("0031"),     // 대구은행
    BUSAN("0032"),     // 부산은행
    GWANGJU("0034"),   // 광주은행
    JEJU("0035"),      // 제주은행
    JB("0037"),        // 전북은행
    GYEONGNAM("0039"), // 경남은행
    SAEMAUL("0045"),   // 새마을금고
    SHINHYUP("0048"),  // 신협은행
    KOREA_POST("0071"),// 우체국
    HANA("0081"),      // KEB하나은행
    SHINHAN("0088"),   // 신한은행
    KBANK("0089");     // 케이뱅크

    private final String code;

    Bank(String code) {
        this.code = code;
    }
}
