package com.timotiusoktorio.inventoryapp.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;

public class LoadProductPhotoAsync extends AsyncTask<String, Void, Bitmap> {

    private static final String TAG = LoadProductPhotoAsync.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    @SuppressLint("StaticFieldLeak")
    private ImageView mProductPhotoImageView;

    public LoadProductPhotoAsync(Context context, ImageView productPhotoImageView) {
        mContext = context;
        mProductPhotoImageView = productPhotoImageView;
    }

    @Override
    protected Bitmap doInBackground(String... args) {
        String photoPath = args[0];
        Bitmap photoBitmap;
        try {
            photoBitmap = PhotoHelper.getBitmapFromUri(mContext, Uri.parse(photoPath));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            photoBitmap = BitmapFactory.decodeFile(photoPath);
        }
        return photoBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (bitmap != null) {
            mProductPhotoImageView.setImageBitmap(bitmap);
        }
    }
}