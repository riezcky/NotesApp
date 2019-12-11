package com.example.consumerapp.db;

import android.net.Uri;
import android.provider.BaseColumns;

public class DatabaseContract {

    public static final String AUTHORITY = "com.dicoding.picodiploma.mynotesapp";
    public static String TABLE_NAME = "note";
    private static final String SCEME = "content";

    private DatabaseContract(){}

    public static final class NoteColumns implements BaseColumns{

       public static String TITLE = "title";
       public static String DESCRIPTION = "description";
       public static String DATE = "date";

        // untuk membuat URI content://com.dicoding.picodiploma.mynotesapp/note
       public static final Uri CONTENT_URI = new Uri.Builder().scheme(SCEME)
               .authority(AUTHORITY)
               .appendPath(TABLE_NAME)
               .build();
    }
}
