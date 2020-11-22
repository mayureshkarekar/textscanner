package com.getext.room.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.getext.keys.AppKeys;

@Entity(tableName = AppKeys.RECOGNIZED_TEXT_TABLE_NAME)
public class RecognizedText {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = AppKeys.ID_COLUMN_NAME)
    private int mId;

    @ColumnInfo(name = AppKeys.MODE_COLUMN_NAME)
    private int mRecognitionMode;

    @ColumnInfo(name = AppKeys.TIMESTAMP_COLUMN_NAME)
    private long mTimestamp;

    @ColumnInfo(name = AppKeys.TEXT_COLUMN_NAME)
    private String mRecognizedText;

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public int getRecognitionMode() {
        return mRecognitionMode;
    }

    public void setRecognitionMode(int recognitionMode) {
        this.mRecognitionMode = recognitionMode;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        this.mTimestamp = timestamp;
    }

    @NonNull
    public String getRecognizedText() {
        return mRecognizedText;
    }

    public void setRecognizedText(@NonNull String recognizedText) {
        this.mRecognizedText = recognizedText;
    }
}