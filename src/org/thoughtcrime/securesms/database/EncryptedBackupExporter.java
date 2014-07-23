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

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.thoughtcrime.securesms.crypto.EncryptingPartOutputStream;
import org.whispersystems.textsecure.crypto.MasterCipher;
import org.whispersystems.textsecure.crypto.MasterSecret;
import org.whispersystems.textsecure.util.Util;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class EncryptedBackupExporter {

  public static void exportToSd(Context context, MasterSecret masterSecret) throws NoExternalStorageException, IOException {
    verifyExternalStorageForExport();
    exportDirectory(context, masterSecret);
  }

  public static void importFromSd(Context context) throws NoExternalStorageException, IOException {
    verifyExternalStorageForImport();
//    importDirectory(context);
  }

  private static String getExportDirectoryPath() {
    File sdDirectory = Environment.getExternalStorageDirectory();
    return sdDirectory.getAbsolutePath();
  }

  private static void verifyExternalStorageForExport() throws NoExternalStorageException {
    if (!Environment.getExternalStorageDirectory().canWrite())
      throw new NoExternalStorageException();

    String exportDirectoryPath = getExportDirectoryPath();
    File exportDirectory = new File(exportDirectoryPath);

    if (!exportDirectory.exists())
      exportDirectory.mkdir();
  }

  private static void verifyExternalStorageForImport() throws NoExternalStorageException {
    if (!Environment.getExternalStorageDirectory().canRead() ||
        !(new File(getExportDirectoryPath()).exists()))
      throw new NoExternalStorageException();
  }

  private static void migrateFile(Context context, File from, ZipOutputStream to, String zipName) {
    try {
      if (from.exists()) {

        Log.w("EncryptedBackup", "creating zip entry for \"" + zipName + "\"");
        ZipEntry entry = new ZipEntry(zipName);
        to.putNextEntry(entry);
        FileInputStream source = new FileInputStream(from);
        int n;
        byte[] buffer = new byte[4096];
        while ((n = source.read(buffer, 0, 4096)) != -1) {
          to.write(buffer, 0, n);
        }
        to.closeEntry();
        source.close();
      }
    } catch (IOException ioe) {
      Log.w("EncryptedBackupExporter", ioe);
    }
  }

  private static void exportDirectory(Context context, MasterSecret masterSecret) throws IOException {
    File exportDirectory = new File(getExportDirectoryPath());
    File exportZipFile = new File(exportDirectory.getAbsolutePath() + File.separator + "TextSecureBackup.tsbk");
    if (!exportZipFile.exists()) {
      if (!exportZipFile.createNewFile()) throw new AssertionError("export file didn't exist but then couldn't create one...");
    }
    FileOutputStream zipFileStream = new EncryptingPartOutputStream(exportZipFile, masterSecret);
//    FileOutputStream zipFileStream = new FileOutputStream(exportZipFile);
    ZipOutputStream  zipStream     = new ZipOutputStream(new BufferedOutputStream(zipFileStream));
    BufferedWriter   writer        = new BufferedWriter(new OutputStreamWriter(zipStream));
    try {
      final ZipEntry smsesEntry = new ZipEntry("smses.xml");
      zipStream.putNextEntry(smsesEntry);
      PlaintextBackupExporter.exportPlaintext(context, masterSecret, writer);

      final ZipEntry secretsEntry = new ZipEntry("identity.xml");
      zipStream.putNextEntry(secretsEntry);
      IdentityExporter.exportIdentity(context, masterSecret, writer);

      writer.flush();
    } finally {
      zipStream.close();
      zipFileStream.close();
    }
  }

  private static void importDirectory(Context context, String directoryName) throws IOException {
//    File directory       = new File(getExportDirectoryPath() + File.separator + directoryName);
//    File importDirectory = new File(context.getFilesDir().getParent() + File.separator + directoryName);
//
//    if (directory.exists() && directory.isDirectory()) {
//      if (!importDirectory.mkdirs()) throw new IOException("couldn't create import directory");
//
//      File[] contents = directory.listFiles();
//
//      for (File exportedFile : contents) {
//        if (exportedFile.isFile()) {
//          File localFile = new File(importDirectory.getAbsolutePath() + File.separator + exportedFile.getName());
//          migrateFile(exportedFile, localFile);
//        } else if (exportedFile.isDirectory()) {
//          importDirectory(context, directoryName + File.separator + exportedFile.getName());
//        }
//      }
//    }
  }
}
