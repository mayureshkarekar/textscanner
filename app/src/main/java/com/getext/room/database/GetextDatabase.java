package com.getext.room.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.getext.keys.AppKeys;
import com.getext.room.dao.RecognizedTextDao;
import com.getext.room.entity.RecognizedText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {RecognizedText.class}, version = 1, exportSchema = false)
public abstract class GetextDatabase extends RoomDatabase {
    private static volatile GetextDatabase INSTANCE;
    public static final ExecutorService databaseExecutorService = Executors.newFixedThreadPool(4);

    public abstract RecognizedTextDao recognizedTextDao();

    public static GetextDatabase getDatabase(final Context context) {
        synchronized (GetextDatabase.class) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(), GetextDatabase.class, AppKeys.GETEXT_DATABASE_NAME).build();
            }
        }

        return INSTANCE;
    }
}