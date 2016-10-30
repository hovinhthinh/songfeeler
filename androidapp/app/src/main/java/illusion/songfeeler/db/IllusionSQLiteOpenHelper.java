package illusion.songfeeler.db;

import illusion.songfeeler.entity.Song;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class IllusionSQLiteOpenHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "illusion";
	private static final int VERSION = 1;

	public static final String TABLE_NAME = "DetectedSongs";

	public static final String CL_ID = "_id";
	public static final String CL_TITLE = "Title";
	public static final String CL_URL = "Url";
	public static final String CL_AUTHOR = "Author";
	public static final String CL_ARTIST = "Artist";
	public static final String CL_LYRICS = "Lyrics";
	public static final String CL_IMAGE = "Image";

	public static final String SQL_CREATE_COMMAND = "CREATE TABLE "
			+ TABLE_NAME + "(" + CL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ CL_TITLE + " TEXT," + CL_URL + " TEXT," + CL_AUTHOR + " TEXT,"
			+ CL_ARTIST + " TEXT," + CL_LYRICS + " TEXT," + CL_IMAGE + " BLOB"
			+ ");";

	public IllusionSQLiteOpenHelper(Context context) {
		super(context, DB_NAME, null, VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_COMMAND);
		Log.d("SQLCreateCommand", SQL_CREATE_COMMAND);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}

	public long insertSong(Song song) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(CL_TITLE, song.getTitle());
		cv.put(CL_URL, song.getUrl());
		cv.put(CL_AUTHOR, song.getAuthor());
		cv.put(CL_ARTIST, song.getArtist());
		cv.put(CL_LYRICS, song.getLyrics());
		cv.put(CL_IMAGE, song.getImage());
		return db.insert(TABLE_NAME, null, cv);
	}

	public Cursor getListSongs() {
		SQLiteDatabase db = getReadableDatabase();
		return db.query(TABLE_NAME, null, null, null, null, null, CL_ID
				+ " DESC");
	}

	public Song getSong(long _id) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, CL_ID + " = " + _id, null,
				null, null, null);

		if (cursor != null && cursor.moveToFirst()) {
			String title = cursor.getString(cursor.getColumnIndex(CL_TITLE));
			String url = cursor.getString(cursor.getColumnIndex(CL_URL));
			String author = cursor.getString(cursor.getColumnIndex(CL_AUTHOR));
			String artist = cursor.getString(cursor.getColumnIndex(CL_ARTIST));
			String lyrics = cursor.getString(cursor.getColumnIndex(CL_LYRICS));
			byte[] image = cursor.getBlob(cursor.getColumnIndex(CL_IMAGE));
			Song song = new Song(title, url, author, artist, lyrics, image);
			cursor.close();
			return song;
		} else {
			return null;
		}
	}
}
