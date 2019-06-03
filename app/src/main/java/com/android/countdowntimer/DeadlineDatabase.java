package com.android.countdowntimer;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Event.class}, version = 2, exportSchema = false)
public abstract class DeadlineDatabase extends RoomDatabase {

    private static DeadlineDatabase INSTANCE;

    public abstract EventsDao eventDao();

    public static DeadlineDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DeadlineDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            DeadlineDatabase.class, "event_database.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
