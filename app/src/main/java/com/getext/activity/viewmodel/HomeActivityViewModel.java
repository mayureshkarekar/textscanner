package com.getext.activity.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.getext.repository.RecognizedTextRepository;
import com.getext.room.entity.RecognizedText;

import java.util.List;

public class HomeActivityViewModel extends AndroidViewModel {
    private final RecognizedTextRepository mRepository;
    private final LiveData<List<RecognizedText>> mAllRecognizedTexts;

    public HomeActivityViewModel(Application application) {
        super(application);
        mRepository = new RecognizedTextRepository(application);
        mAllRecognizedTexts = mRepository.getAllRecognizedTexts();
    }

    public LiveData<List<RecognizedText>> getAllRecognizedTexts() {
        return mAllRecognizedTexts;
    }

    public void deleteRecognizedText(@NonNull RecognizedText recognizedText) {
        mRepository.deleteRecognizedText(recognizedText);
    }

    public void deleteAllRecognizedTexts() {
        mRepository.deleteAllRecognizedTexts();
    }
}