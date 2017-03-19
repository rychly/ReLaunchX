package com.gacode.relaunchx;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.regex.Pattern;
import ebook.EBook;
import ebook.Person;
import ebook.parser.InstantParser;
import ebook.parser.Parser;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BooksBase {
	Context context;
	DbHelper dbHelper;
	public static SQLiteDatabase db;

	private class DbHelper extends SQLiteOpenHelper {
		final static int VERSION = 1;

		public DbHelper(Context context) {
			super(context, "library.db", null, VERSION);
		}

		public DbHelper(Context context, String name) {
			super(context, name, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table if not exists BOOKS ("
					+ "ID integer primary key autoincrement, "
					+ "FILE text unique, " + "TITLE text default '', "
					+ "FIRSTNAME text default '', "
					+ "LASTNAME text default '', " + "SERIES text default '', "
					+ "NUMBER text default '')");
			db.execSQL("create table if not exists COVERS ("
					+ "ID integer primary key autoincrement, "
					+ "BOOK integer unique, " + "COVER blob)");
			db.execSQL("create index if not exists INDEX1 on BOOKS(FILE)");
			db.execSQL("create index if not exists INDEX3 on COVERS(BOOK)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}

	public BooksBase(Context context) {
		this.context = context;
		dbHelper = new DbHelper(context);
		db = dbHelper.getWritableDatabase();
	}

	public BooksBase(Context context, String path) {
		this.context = context;
		String dbName = (path.replaceAll("\\/", "_")).concat(".db");
		dbHelper = new DbHelper(context, dbName);
		db = dbHelper.getWritableDatabase();
	}

	private void open() {
		if (!db.isOpen())
			db = SQLiteDatabase.openDatabase("library.db", null,
					SQLiteDatabase.OPEN_READWRITE);
	}

	public long addBook(EBook book) {
		long bookId;
		ContentValues cv = new ContentValues();
		cv.put("FILE", book.fileName);
		cv.put("TITLE", book.title);
		if (book.authors.size() > 0) {
			cv.put("FIRSTNAME", book.authors.get(0).firstName);
			cv.put("LASTNAME", book.authors.get(0).lastName);
		}
		if (book.sequenceName != null) {
			cv.put("SERIES", book.sequenceName);
		}
		if (book.sequenceNumber != null) {
			cv.put("NUMBER", book.sequenceNumber);
		}
		bookId = db.insertOrThrow("BOOKS", null, cv);
		return bookId;
	}

	@SuppressWarnings("unused")
	private long getAuthorIdByName(String name) {
		long id;
		Cursor cursor = db.rawQuery("select ID from AUTHORS where NAME=?",
				new String[] { name });
		if (cursor.moveToFirst())
			id = cursor.getLong(0);
		else
			id = -1;
		cursor.close();
		return id;
	}

	public long getBookIdByFileName(String fileName) {
		long id;
		Cursor cursor = db.rawQuery("select ID from BOOKS where FILE=?",
				new String[] { fileName });
		if (cursor.moveToFirst())
			id = cursor.getLong(0);
		else
			id = -1;
		cursor.close();
		return id;
	}

	public EBook getBookById(long id) {
		EBook book = new EBook();
		Person author = new Person();
		Cursor cursor = db.rawQuery("select * from BOOKS where id=?",
				new String[] { "" + id });
		if (cursor.moveToFirst()) {
			book.fileName = cursor.getString(cursor.getColumnIndex("FILE"));
			book.title = cursor.getString(cursor.getColumnIndex("TITLE"));
			author.firstName = cursor.getString(cursor
					.getColumnIndex("FIRSTNAME"));
			author.lastName = cursor.getString(cursor
					.getColumnIndex("LASTNAME"));
			book.authors.add(author);
			book.sequenceName = cursor.getString(cursor
					.getColumnIndex("SERIES"));
			book.sequenceNumber = cursor.getString(cursor
					.getColumnIndex("NUMBER"));
			book.isOk = true;
		} else
			book.isOk = false;
		cursor.close();
		return book;
	}

	public EBook getBookByFileName(String fileName) {
		EBook book = new EBook();
		Person author = new Person();
		Cursor cursor = db.rawQuery("select * from BOOKS where FILE=?",
				new String[] { fileName });
		if (cursor.moveToFirst()) {
			book.title = cursor.getString(2);
			author.firstName = cursor.getString(3);
			author.lastName = cursor.getString(4);
			book.authors.add(author);
			book.sequenceName = cursor.getString(5);
			book.sequenceNumber = cursor.getString(6);
			book.isOk = true;
		} else
			book.isOk = false;
		cursor.close();
		return book;
	}

	public void resetDb() {
		db.execSQL("delete from BOOKS");
		db.execSQL("delete from COVERS");
		db.execSQL("reindex INDEX1");
		db.execSQL("reindex INDEX3");
	}

}
