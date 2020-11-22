package com.getext.repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.getext.room.dao.RecognizedTextDao;
import com.getext.room.database.GetextDatabase;
import com.getext.room.entity.RecognizedText;

import java.util.List;

public class RecognizedTextRepository {
    private final RecognizedTextDao mRecognizedTextDao;
    private final LiveData<List<RecognizedText>> mAllRecognizedTexts;

    public RecognizedTextRepository(Application application) {
        GetextDatabase database = GetextDatabase.getDatabase(application);
        mRecognizedTextDao = database.recognizedTextDao();
        mAllRecognizedTexts = mRecognizedTextDao.getAllRecognizedTexts();
    }

    public LiveData<List<RecognizedText>> getAllRecognizedTexts() {
        return mAllRecognizedTexts;
    }

    public void insertRecognizedText(@NonNull RecognizedText recognizedText) {
        GetextDatabase.databaseExecutorService.execute(() -> mRecognizedTextDao.insertRecognizedText(recognizedText));
    }

    public void updateRecognizedText(@NonNull RecognizedText recognizedText) {
        GetextDatabase.databaseExecutorService.execute(() -> mRecognizedTextDao.updateRecognizedText(recognizedText));
    }

    public void deleteRecognizedText(@NonNull RecognizedText recognizedText) {
        GetextDatabase.databaseExecutorService.execute(() -> mRecognizedTextDao.deleteRecognizedText(recognizedText));
    }

    public void deleteAllRecognizedTexts() {
        GetextDatabase.databaseExecutorService.execute(mRecognizedTextDao::deleteAllRecognizedTexts);
    }
}