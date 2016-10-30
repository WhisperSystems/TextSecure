/**
 * Copyright (C) 2016 Open Whisper Systems
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

import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;

/**
 * PagerAdapter that manages a Cursor, adapted from CursorRecyclerViewAdapter.
 */
public abstract class CursorPagerAdapter extends PagerAdapter {
  private final DataSetObserver observer = new AdapterDataSetObserver();

  private Cursor  cursor;
  private boolean valid;

  protected CursorPagerAdapter(Cursor cursor) {
    this.cursor = cursor;
    if (cursor != null) {
      valid = true;
      cursor.registerDataSetObserver(observer);
    }
  }

  public void changeCursor(Cursor cursor) {
    Cursor old = swapCursor(cursor);
    if (old != null) {
      old.close();
    }
  }

  public Cursor swapCursor(Cursor newCursor) {
    if (newCursor == cursor) {
      return null;
    }

    final Cursor oldCursor = cursor;
    if (oldCursor != null) {
      oldCursor.unregisterDataSetObserver(observer);
    }

    cursor = newCursor;
    if (cursor != null) {
      cursor.registerDataSetObserver(observer);
    }

    valid = cursor != null;
    notifyDataSetChanged();
    return oldCursor;
  }

  @Override
  public int getCount() {
    if (!isActiveCursor()) return 0;

    return cursor.getCount();
  }

  protected @NonNull Cursor getCursorAtPositionOrThrow(final int position) {
    if (!isActiveCursor()) {
      throw new IllegalStateException("this should only be called when the cursor is valid");
    }
    if (!cursor.moveToPosition(position)) {
      throw new IllegalStateException("couldn't move cursor to position " + position);
    }
    return cursor;
  }

  private boolean isActiveCursor() {
    return valid && cursor != null;
  }

  private class AdapterDataSetObserver extends DataSetObserver {
    @Override
    public void onChanged() {
      super.onChanged();
      valid = true;
      notifyDataSetChanged();
    }

    @Override
    public void onInvalidated() {
      super.onInvalidated();
      valid = false;
      notifyDataSetChanged();
    }
  }
}
