package com.pkmnapps.activitydo.contentproviders

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import org.junit.runner.RunWith
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class ImageProvider : ContentProvider() {
    companion object {
        val CONTENT_URI = Uri.parse("content://com.pkmnapps.activitydo/")
        private val MIME_TYPES: HashMap<String?, String?>? = HashMap()

        init {
            MIME_TYPES[".jpg"] = "image/jpeg"
            MIME_TYPES[".jpeg"] = "image/jpeg"
        }
    }

    override fun onCreate(): Boolean {
        return try {
            val mFile = File(context.getFilesDir(), "newImage.jpg")
            if (!mFile.exists()) {
                mFile.createNewFile()
            }
            context.getContentResolver().notifyChange(CONTENT_URI, null)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun getType(uri: Uri?): String? {
        val path = uri.toString()
        for (extension in MIME_TYPES.keys) {
            if (path.endsWith(extension)) {
                return MIME_TYPES.get(extension)
            }
        }
        return null
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri?, mode: String?): ParcelFileDescriptor? {
        val f = File(context.getFilesDir(), "newImage.jpg")
        if (f.exists()) {
            return ParcelFileDescriptor.open(f,
                    ParcelFileDescriptor.MODE_READ_WRITE)
        }
        throw FileNotFoundException(uri.getPath())
    }

    override fun query(url: Uri?, projection: Array<String?>?, selection: String?,
                       selectionArgs: Array<String?>?, sort: String?): Cursor? {
        throw RuntimeException("Operation not supported")
    }

    override fun insert(uri: Uri?, initialValues: ContentValues?): Uri? {
        throw RuntimeException("Operation not supported")
    }

    override fun update(uri: Uri?, values: ContentValues?, where: String?,
                        whereArgs: Array<String?>?): Int {
        throw RuntimeException("Operation not supported")
    }

    override fun delete(uri: Uri?, where: String?, whereArgs: Array<String?>?): Int {
        throw RuntimeException("Operation not supported")
    }
}