/**
 * Copyright (C) 2011 Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thoughtcrime.securesms.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.thoughtcrime.securesms.util.GroupUtil;
import org.thoughtcrime.securesms.util.Util;
import org.whispersystems.textsecure.util.InvalidNumberException;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CanonicalAddressDatabase {
  private static final String TAG              = "CanonicalAddressDb";
  private static final String DATABASE_NAME    = "canonical_address.db";
  private static final String TABLE            = "canonical_addresses";
  private static final String ID_COLUMN        = "_id";
  private static final String ADDRESS_COLUMN   = "address";

  private static final String DATABASE_CREATE  = "CREATE TABLE " + TABLE + " (" + ID_COLUMN + " integer PRIMARY KEY, " + ADDRESS_COLUMN + " TEXT NOT NULL);";
  private static final String[] ID_PROJECTION  = {ID_COLUMN};
  private static final String SELECTION        = ADDRESS_COLUMN + " = ?";
  private static final Object lock             = new Object();

  private static final int DATABASE_VERSION          = 2;
  private static final int INTRODUCED_E164_ADDRESSES = 2;

  private static CanonicalAddressDatabase instance;
  private        DatabaseHelper           databaseHelper;
  private final  Context                  context;

  private final Map<String,Long> addressCache = Collections.synchronizedMap(new HashMap<String,Long>());
  private final Map<String,String> idCache    = Collections.synchronizedMap(new HashMap<String,String>());

  public static CanonicalAddressDatabase getInstance(Context context) {
    synchronized (lock) {
      if (instance == null)
        instance = new CanonicalAddressDatabase(context);

      return instance;
    }
  }

  private CanonicalAddressDatabase(Context context) {
    this.context = context.getApplicationContext();
    databaseHelper = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    fillCache();
  }

  public void reset(Context context) {
    DatabaseHelper old  = this.databaseHelper;
    this.databaseHelper = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    old.close();
    fillCache();
  }

  private void fillCache() {
    Cursor cursor = null;

    try {
      SQLiteDatabase db = databaseHelper.getReadableDatabase();
      cursor            = db.query(TABLE, null, null, null, null, null, null);

      while (cursor != null && cursor.moveToNext()) {
        long id        = cursor.getLong(cursor.getColumnIndexOrThrow(ID_COLUMN));
        String address = cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS_COLUMN));

        if (address == null || address.trim().length() == 0)
          address = "Anonymous";

        idCache.put(id+"", address);
        addressCache.put(address, id);
      }
    } finally {
      if (cursor != null)
        cursor.close();
    }
  }

  public String getAddressFromId(String id) {
    if (id == null || id.trim().equals("")) return "Anonymous";

    String cachedAddress = idCache.get(id);

    if (cachedAddress != null)
      return cachedAddress;

    Cursor cursor = null;

    try {
      Log.w(TAG, "Hitting DB on query [ID].");

      SQLiteDatabase db = databaseHelper.getReadableDatabase();
      cursor            = db.query(TABLE, null, ID_COLUMN + " = ?", new String[] {id+""}, null, null, null);

      if (!cursor.moveToFirst())
        return "Anonymous";

      String address = cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS_COLUMN));

      if (address == null || address.trim().equals("")) {
        return "Anonymous";
      } else {
        idCache.put(id, address);
        return address;
      }
    } finally {
      if (cursor != null)
        cursor.close();
    }
  }

  public void close() {
    databaseHelper.close();
    instance = null;
  }

  public long getCanonicalAddressId(String address) {
    long addressId;
    String canonicalAddress;
    if (GroupUtil.isEncodedGroup(address)) {
      canonicalAddress = address;
    } else {
      try {
        canonicalAddress = Util.canonicalizeNumber(context, address);
      } catch (InvalidNumberException ine) {
        Log.w(TAG, ine);
        canonicalAddress = address;
      }
    }
    if ((addressId = getCanonicalAddressIdFromCache(canonicalAddress)) != -1)
      return addressId;

    addressId = getCanonicalAddressIdFromDatabase(canonicalAddress);
    addressCache.put(canonicalAddress, addressId);

    return addressId;
  }

  public List<Long> getCanonicalAddressIds(List<String> addresses) {
    List<Long> addressIdList = new LinkedList<Long>();

    for (String address : addresses) {
      addressIdList.add(getCanonicalAddressId(address));
    }

    return addressIdList;
  }

  private long getCanonicalAddressIdFromCache(String address) {
    if (addressCache.containsKey(address))
      return Long.valueOf(addressCache.get(address));

    return -1L;
  }

  private long getCanonicalAddressIdFromDatabase(String address) {
    Cursor cursor = null;

    try {
      SQLiteDatabase db           = databaseHelper.getWritableDatabase();
      String[] selectionArguments = new String[] {address};
      cursor                      = db.query(TABLE, ID_PROJECTION, SELECTION, selectionArguments, null, null, null);

      if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(ADDRESS_COLUMN, address);

        return db.insert(TABLE, ADDRESS_COLUMN, contentValues);
      }

      return cursor.getLong(cursor.getColumnIndexOrThrow(ID_COLUMN));
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  private static class DatabaseHelper extends SQLiteOpenHelper {
    private final Context context;

    public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
      super(context, name, factory, version);
      this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      if (oldVersion < INTRODUCED_E164_ADDRESSES) {
        long startMillis = System.currentTimeMillis();
        Log.w(TAG, "canonicalizing canonical addresses");
        final Cursor cursor = db.query("canonical_addresses", null, null, null, null, null, null);

        db.beginTransaction();
        try {
          while (cursor != null && cursor.moveToNext()) {
            final long id = cursor.getInt(0);
            final String address = cursor.getString(1);

            String canonicalAddress;
            if (GroupUtil.isEncodedGroup(address)) {
              canonicalAddress = address;
            } else {
              try {
                canonicalAddress = Util.canonicalizeNumber(context, address);
              } catch (InvalidNumberException ine) {
                Log.w(TAG, ine);
                canonicalAddress = address;
              }
            }
            ContentValues values = new ContentValues(1);
            values.put("address", canonicalAddress);
            db.update("canonical_addresses", values, "_id = ?", new String[]{"" + id});
          }
          Log.w(TAG, "upgraded " + (cursor == null ? 0 : cursor.getCount()) + " numbers, took " +
                     (System.currentTimeMillis() - startMillis) + "ms");
          db.setTransactionSuccessful();
        } finally {
          db.endTransaction();
        }
      }
    }
  }
}
