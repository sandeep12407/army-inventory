package com.timotiusoktorio.inventoryapp.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.timotiusoktorio.inventoryapp.R;
import com.timotiusoktorio.inventoryapp.data.ProductDbHelper;
import com.timotiusoktorio.inventoryapp.ui.dialogs.ProductPhotoDialogFragment;
import com.timotiusoktorio.inventoryapp.util.LoadProductPhotoAsync;
import com.timotiusoktorio.inventoryapp.util.PhotoHelper;
import com.timotiusoktorio.inventoryapp.data.model.Product;

import java.io.File;
import java.io.IOException;

public class CreateActivity extends AppCompatActivity implements DialogInterface.OnClickListener {

    private static final String TAG = CreateActivity.class.getSimpleName();
    private static final String PRODUCT_PHOTO_DIALOG_TAG = "PRODUCT_PHOTO_DIALOG_TAG";
    private static final String INTENT_EXTRA_PRODUCT = "INTENT_EXTRA_PRODUCT";
    private static final int REQUEST_CODE_TAKE_PHOTO = 0;
    private static final int REQUEST_CODE_CHOOSE_PHOTO = 1;

    private ImageView mProductPhotoImageView;
    private TextInputLayout mProductNameTIL;
    private TextInputEditText mProductNameTextField;
    private TextInputEditText mProductCodeTextField;
    private TextInputLayout mProductSupplierTIL;
    private TextInputEditText mProductSupplierTextField;
    private TextInputLayout mProductSupplierEmailTIL;
    private TextInputEditText mProductSupplierEmailTextField;
    private TextInputLayout mProductPriceTIL;
    private TextInputEditText mProductPriceTextField;
    private TextInputLayout mProductQuantityTIL;
    private TextInputEditText mProductQuantityTextField;

    private ProductDbHelper mDbHelper;
    private Product mPassedProduct;
    // Global variable to hold the path of the photo file which gets created each time the user
    // capture a photo using camera intent. This is stored globally so the file can be accessed
    // later on. If the user cancel taking a picture via the camera intent or decided to choose
    // a photo from the gallery instead, the file needs to be deleted as it's no longer needed.
    private String mTempPhotoFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        mProductPhotoImageView = findViewById(R.id.product_photo_image_view);
        mProductNameTIL = findViewById(R.id.product_name_text_input_layout);
        mProductNameTextField = findViewById(R.id.product_name_text_field);
        mProductCodeTextField = findViewById(R.id.product_code_text_field);
        mProductSupplierTIL = findViewById(R.id.product_supplier_text_input_layout);
        mProductSupplierTextField = findViewById(R.id.product_supplier_text_field);
        mProductSupplierEmailTIL = findViewById(R.id.product_supplier_email_text_input_layout);
        mProductSupplierEmailTextField = findViewById(R.id.product_supplier_email_text_field);
        mProductPriceTIL = findViewById(R.id.product_price_text_input_layout);
        mProductPriceTextField = findViewById(R.id.product_price_text_field);
        mProductQuantityTIL = findViewById(R.id.product_quantity_text_input_layout);
        mProductQuantityTextField = findViewById(R.id.product_quantity_text_field);

        // Add text changed listener to all text fields that needs to be validated.
        addTextWatcherToTextFields(mProductNameTIL, mProductNameTextField);
        addTextWatcherToTextFields(mProductSupplierTIL, mProductSupplierTextField);
        addTextWatcherToTextFields(mProductSupplierEmailTIL, mProductSupplierEmailTextField);
        addTextWatcherToTextFields(mProductPriceTIL, mProductPriceTextField);
        addTextWatcherToTextFields(mProductQuantityTIL, mProductQuantityTextField);

        mDbHelper = ProductDbHelper.getInstance(getApplicationContext());
        mPassedProduct = getIntent().getParcelableExtra(INTENT_EXTRA_PRODUCT);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle((mPassedProduct == null) ? R.string.title_action_bar_add_product : R.string.title_action_bar_edit_product);
        }

        // If there is a product object passed from the intent, populate the views with the product data.
        if (mPassedProduct != null) {
            populateViewsWithPassedProductData();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check the request code to determine which intent was dispatched.
        if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                // Take photo successful. If the user has previously set a captured photo on the
                // ImageView, that photo file needs to be deleted since it will be replaced now.
                PhotoHelper.deleteCapturedPhotoFile(mProductPhotoImageView.getTag());
                // Save the file uri as a tag and display the captured photo on the ImageView.
                mProductPhotoImageView.setTag(mTempPhotoFilePath);
                new LoadProductPhotoAsync(this, mProductPhotoImageView).execute(mTempPhotoFilePath);
            } else if (resultCode == RESULT_CANCELED) {
                // The user cancelled taking a photo. The photo file created from the camera intent
                // is just an empty file so delete it since we don't need it anymore.
                File photoFile = new File(mTempPhotoFilePath);
                boolean deletePhotoSuccess = photoFile.delete();
                Log.d(TAG, "onActivityResult: deletePhotoSuccess = " + deletePhotoSuccess);
            }
        } else if (requestCode == REQUEST_CODE_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                // Choose photo successful. Delete previously captured photo file if there's any.
                PhotoHelper.deleteCapturedPhotoFile(mProductPhotoImageView.getTag());
                // Get the picture path and load it into the ImageView.
                Uri selectedPhotoUri = data.getData();
                mProductPhotoImageView.setTag(selectedPhotoUri.toString());
                new LoadProductPhotoAsync(this, mProductPhotoImageView).execute(selectedPhotoUri.toString());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_done) {
            // The user presses the 'Done' action button. Validate user inputs before proceeding.
            if (validateUserInput()) {
                Intent previousActivityIntent;
                // If passed product object is present, update the product with the new data.
                // Otherwise, create a new product object with the data and save it to the database.
                if (mPassedProduct != null) {
                    buildProductWithUserInputData(mPassedProduct);
                    mDbHelper.updateProduct(mPassedProduct);
                    // Set previousActivityIntent to DetailActivity and pass back the updated product object.
                    previousActivityIntent = new Intent(this, DetailActivity.class);
                    previousActivityIntent.putExtra(INTENT_EXTRA_PRODUCT, mPassedProduct);
                } else {
                    Product product = new Product();
                    buildProductWithUserInputData(product);
                    mDbHelper.insertProduct(product);
                    // Set previousActivityIntent to MainActivity.
                    previousActivityIntent = new Intent(this, MainActivity.class);
                }
                // Return to the previous activity which called this activity.
                NavUtils.navigateUpTo(this, previousActivityIntent);
            }
        } else if (item.getItemId() == android.R.id.home) {
            // The user presses the 'Back' action button. Before returning to MainActivity, check
            // if the user has set a captured photo to the ImageView because it needs to be deleted.
            if (mPassedProduct == null) {
                PhotoHelper.deleteCapturedPhotoFile(mProductPhotoImageView.getTag());
            }
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * AlertDialog interface callback method which gets invoked when the user selects one of the
     * available options on the product photo dialog.
     *
     * @param dialogInterface - the dialog interface.
     * @param i               - position of the selected option.
     */
    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == 0) {
            // The user selects 'Take photo' option. Dispatch the camera intent.
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                // Create a File where the photo will be saved to.
                File photoFile = null;
                try {
                    photoFile = PhotoHelper.createPhotoFile(this);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                if (photoFile != null) {
                    // Save the photo file path globally.
                    mTempPhotoFilePath = photoFile.getAbsolutePath();
                    // Get the file content URI using FileProvider to avoid FileUriExposedException.
                    Uri photoUri = FileProvider.getUriForFile(this, getString(R.string.authority), photoFile);
                    // Set the file content URI as an intent extra and dispatch the camera intent.
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(cameraIntent, REQUEST_CODE_TAKE_PHOTO);
                }
            }
        } else {
            // The user selects 'Choose photo' option. Dispatch the choose photo intent.
            Intent choosePhotoIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            choosePhotoIntent.addCategory(Intent.CATEGORY_OPENABLE);
            choosePhotoIntent.setType("image/*");
            if (choosePhotoIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(choosePhotoIntent, REQUEST_CODE_CHOOSE_PHOTO);
            }
        }
    }

    /**
     * Method that gets invoked when the user presses the 'photo camera' floating action button.
     * This method will inflate the product photo dialog using the product photo dialog fragment.
     *
     * @param view - 'photo camera' floating action button.
     */
    public void showProductPhotoDialog(View view) {
        ProductPhotoDialogFragment dialogFragment = new ProductPhotoDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), PRODUCT_PHOTO_DIALOG_TAG);
    }

    /**
     * Method for populating the views with the passed product data. Since the user might come to
     * this activity to edit an existing product object, all text fields and ImageView must be
     * populated by the product object.
     */
    private void populateViewsWithPassedProductData() {
        String photoPath = mPassedProduct.getPhotoPath();
        mProductPhotoImageView.setTag(photoPath);
        new LoadProductPhotoAsync(this, mProductPhotoImageView).execute(photoPath);

        mProductNameTextField.setText(mPassedProduct.getName());
        mProductCodeTextField.setText(mPassedProduct.getCode());
        mProductSupplierTextField.setText(mPassedProduct.getSupplier());
        mProductSupplierEmailTextField.setText(mPassedProduct.getSupplierEmail());
        mProductPriceTextField.setText(String.valueOf(mPassedProduct.getPrice()));
        mProductQuantityTextField.setText(String.valueOf(mPassedProduct.getQuantity()));
    }

    private void addTextWatcherToTextFields(@NonNull final TextInputLayout til, @NonNull TextInputEditText textField) {
        textField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start >= 0 && til.isErrorEnabled()) {
                    til.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * Method for validating user inputs when the user presses the 'Done' action button.
     * The inputs that needs to be validated are: product name, supplier, supplier email, price, and quantity.
     *
     * @return true if all required inputs are present, false if not present.
     */
    private boolean validateUserInput() {
        boolean isProductNameSet = !TextUtils.isEmpty(mProductNameTextField.getText());
        boolean isProductSupplierSet = !TextUtils.isEmpty(mProductSupplierTextField.getText());
        boolean isProductSupplierEmailSet = !TextUtils.isEmpty(mProductSupplierEmailTextField.getText());
        boolean isProductPriceSet = !TextUtils.isEmpty(mProductPriceTextField.getText());
        boolean isProductQtySet = !TextUtils.isEmpty(mProductQuantityTextField.getText());
        if (!isProductNameSet) {
            mProductNameTIL.setError(getString(R.string.error_msg_product_name_empty));
            mProductNameTIL.setErrorEnabled(true);
        }
        if (!isProductSupplierSet) {
            mProductSupplierTIL.setError(getString(R.string.error_msg_product_supplier_empty));
            mProductSupplierTIL.setErrorEnabled(true);
        }
        if (!isProductSupplierEmailSet) {
            mProductSupplierEmailTIL.setError(getString(R.string.error_msg_product_supplier_email_empty));
            mProductSupplierEmailTIL.setErrorEnabled(true);
        }
        if (!isProductPriceSet) {
            mProductPriceTIL.setError(getString(R.string.error_msg_product_price_empty));
            mProductPriceTIL.setErrorEnabled(true);
        }
        if (!isProductQtySet) {
            mProductQuantityTIL.setError(getString(R.string.error_msg_product_quantity_empty));
            mProductQuantityTIL.setErrorEnabled(true);
        }
        return isProductNameSet && isProductSupplierSet && isProductSupplierEmailSet && isProductPriceSet && isProductQtySet;
    }

    /**
     * Method for extracting user inputs from the text fields to a product object.
     *
     * @param product - The product object.
     */
    private void buildProductWithUserInputData(Product product) {
        product.setName(mProductNameTextField.getText().toString());
        product.setCode(mProductCodeTextField.getText().toString());
        product.setSupplier(mProductSupplierTextField.getText().toString());
        product.setSupplierEmail(mProductSupplierEmailTextField.getText().toString());
        // Get the product photo path from the ImageView tag. The tag might contains null data, so
        // it needs to be checked. If it's null, set the photo path to an empty string.
        Object imageViewTag = mProductPhotoImageView.getTag();
        product.setPhotoPath((imageViewTag != null) ? imageViewTag.toString() : "");
        product.setPrice(Double.valueOf(mProductPriceTextField.getText().toString()));
        product.setQuantity(Integer.valueOf(mProductQuantityTextField.getText().toString()));
    }
}