package com.timotiusoktorio.inventoryapp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PhotoHelper {

    private static final String CONTENT_URI_PREFIX = "content://";

    /**
     * Helper method for creating a new blank File object in the private external directory.
     * The file will be saved at: /sdcard/Android/data/package_name/files/Pictures.
     *
     * @param context - The activity which calls this method.
     * @return File - The blank new File object.
     * @throws IOException if File.createTempFile method throws an IOException.
     */
    public static File createPhotoFile(Context context) throws IOException {
        File photoFile = null;
        // Ensure that the external storage is available.
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // Use time stamp format to create a collision-proof file name.
            String timeStamp = new SimpleDateFormat("ddMMyy_HHmmss", Locale.getDefault()).format(new Date());
            String photoFileName = "IMG_" + timeStamp + "_";
            File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            photoFile = File.createTempFile(photoFileName, ".jpg", storageDir);
        }
        return photoFile;
    }

    /**
     * Helper method for getting a Bitmap file from an Uri.
     *
     * @param context - The activity which calls this method.
     * @param uri     - The Uri pointing to the image file.
     * @return Bitmap - The image Bitmap.
     * @throws IOException if ContentResolver.openFileDescriptor method throws an IOException.
     */
    @Nullable
    public static Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
        if (parcelFileDescriptor != null) {
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        }
        return null;
    }

    /**
     * Helper method for deleting a previously captured photo file which was set to an ImageView.
     * The purpose of this method is to free up device storage by deleting unused image files.
     *
     * @param tag - The ImageView tag where the file path is stored.
     */
    public static void deleteCapturedPhotoFile(Object tag) {
        if (tag == null) {
            return; // If tag is null, there's nothing to do. Return early.
        }
        String photoPath = tag.toString();
        // Make sure that the file is a captured photo not a selected image from the gallery.
        if (!photoPath.contains(CONTENT_URI_PREFIX)) {
            File photoFile = new File(photoPath);
            boolean deleteSuccess = photoFile.delete();
            Log.d("PhotoHelper", "deleteCapturedPhotoFile: deleteSuccess = " + deleteSuccess);
        }
    }
}