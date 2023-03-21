package com.example.architectureexample;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Note.class}, version = 1)
public abstract class NoteDatabase extends RoomDatabase {

    private static NoteDatabase instance;

    public abstract NoteDAO noteDAO();

    public static synchronized NoteDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), NoteDatabase.class, "note_database")
                    .fallbackToDestructiveMigration()
                    // When instance is first created, it creates 3 notes
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    // For the first move, we want to make e.g. like 3 Notes in the db
    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private NoteDAO noteDAO;

        public PopulateDbAsyncTask(NoteDatabase noteDatabase) {
            this.noteDAO = noteDatabase.noteDAO();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            noteDAO.insert(new Note("Title 1", "Description 1", 1));
            noteDAO.insert(new Note("Title 2", "Description 2", 2));
            noteDAO.insert(new Note("Title 3", "Description 3", 3));
            return null;
        }
    }
}
