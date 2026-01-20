package com.example.mypet;

public class HistoryItem {
    public long timestamp;   // время в миллисекундах
    public String value;     // значение веса/роста

    public HistoryItem() { }

    public HistoryItem(long timestamp, String value) {
        this.timestamp = timestamp;
        this.value = value;
    }
}
