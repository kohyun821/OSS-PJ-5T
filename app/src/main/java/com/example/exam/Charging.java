package com.example.exam;

import java.io.Serializable;

public class Charging implements Serializable {
    public String addr;//주소
    public String chargeTp;//충전소 타입
    public String cpId;//충전소 ID
    public String cpNm;//충전기 명칭
    public String cpStat;//충전기 상태코드
    public String cpTp;//충전 방식
    public String csId;//충전소 ID
    public String csNm;//충전소 명칭
    public String lat;//위도
    public String longi;//경도
    public String statUpdateDatetime;//충전기 상태 갱신시각

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getChargeTp() {
        return chargeTp;
    }

    public void setChargeTp(String chargeTp) {
        this.chargeTp = chargeTp;
    }

    public String getCpId() {
        return cpId;
    }

    public void setCpId(String cpId) {
        this.cpId = cpId;
    }

    public String getCpNm() {
        return cpNm;
    }

    public void setCpNm(String cpNm) {
        this.cpNm = cpNm;
    }

    public String getCpStat() {
        return cpStat;
    }

    public void setCpStat(String cpStat) {
        this.cpStat = cpStat;
    }

    public String getCpTp() {
        return cpTp;
    }

    public void setCpTp(String cpTp) {
        this.cpTp = cpTp;
    }

    public String getCsId() {
        return csId;
    }

    public void setCsId(String csId) {
        this.csId = csId;
    }

    public String getCsNm() {
        return csNm;
    }

    public void setCsNm(String csNm) {
        this.csNm = csNm;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLongi() {
        return longi;
    }

    public void setLongi(String longi) {
        this.longi = longi;
    }

    public String getStatUpdateDatetime() {
        return statUpdateDatetime;
    }

    public void setStatUpdateDatetime(String statUpdateDatetime) {
        this.statUpdateDatetime = statUpdateDatetime;
    }
}
