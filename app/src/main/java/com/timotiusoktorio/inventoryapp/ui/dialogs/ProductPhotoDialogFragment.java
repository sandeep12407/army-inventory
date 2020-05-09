package com.timotiusoktorio.inventoryapp.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.timotiusoktorio.inventoryapp.R;

public class ProductPhotoDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.title_dialog_product_photo);
        builder.setItems(R.array.product_photo_dialog_options, (DialogInterface.OnClickListener) getActivity());
        return builder.create();
    }
}