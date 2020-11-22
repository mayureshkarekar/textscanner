package com.getext.room.dao;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.getext.keys.AppKeys;
import com.getext.room.entity.RecognizedText;

import java.util.List;

@Dao
public interface RecognizedTextDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecognizedText(@NonNull RecognizedText recognizedText);

    @Update
    void updateRecognizedText(@NonNull RecognizedText recognizedText);

    @Delete
    void deleteRecognizedText(@NonNull RecognizedText recognizedText);

    @Query("DELETE FROM " + AppKeys.RECOGNIZED_TEXT_TABLE_NAME)
    void deleteAllRecognizedTexts();

    @Query("SELECT * FROM " + AppKeys.RECOGNIZED_TEXT_TABLE_NAME + " ORDER BY " + AppKeys.ID_COLUMN_NAME + " DESC")
    LiveData<List<RecognizedText>> getAllRecognizedTexts();
}