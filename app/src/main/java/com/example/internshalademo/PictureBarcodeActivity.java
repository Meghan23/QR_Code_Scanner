package com.example.internshalademo;

import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.FileNotFoundException;

public class PictureBarcodeActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnOpenCamera;
    TextView txtResultBody;
    ImageView QR;
    int Code = 1;

    private BarcodeDetector detector;
    private Uri imageUri;
    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_barcode);

        initViews();

        if (savedInstanceState != null) {
            if (imageUri != null) {
                imageUri = Uri.parse(savedInstanceState.getString(SAVED_INSTANCE_URI));
                txtResultBody.setText(savedInstanceState.getString(SAVED_INSTANCE_RESULT));
            }
        }

        detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();

        if (!detector.isOperational()) {
            txtResultBody.setText("Detector initialisation failed");
        }
    }

    private void initViews() {
        txtResultBody = findViewById(R.id.txtResultsBody);
        btnOpenCamera = findViewById(R.id.btnOpenCamera);
        btnOpenCamera.setOnClickListener(this);
        QR = findViewById(R.id.imageView);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btnOpenCamera) {

            Intent in = new Intent(Intent.ACTION_PICK);
            in.setType("image/*");
            startActivityForResult(in, Code);// start
        }

    }

    protected void onActivityResult(int requestcode, int resultcode,
                                    Intent imagereturnintent) {
        super.onActivityResult(requestcode, resultcode, imagereturnintent);
        switch (Code) {
            case 1:
                if (resultcode == RESULT_OK) {
                    try {

                        Uri imageuri = imagereturnintent.getData();
                        Bitmap bitmap = decodeUri(PictureBarcodeActivity.this, imageuri, 300);

                        if (detector.isOperational() && bitmap != null) {
                            QR.setImageBitmap(bitmap);
                            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                            SparseArray<Barcode> barcodes = detector.detect(frame);
                            if (barcodes.size() == 0) {
                                txtResultBody.setText("No barcode could be detected. Please try again.");
                            } else {
                                for (int index = 0; index < barcodes.size(); index++) {
                                    Barcode code = barcodes.valueAt(index);
                                    txtResultBody.setText(txtResultBody.getText() + "\n" + code.displayValue + "\n");
                                }
                            }

                        } else
                            Toast.makeText(PictureBarcodeActivity.this,
                                    "Error occurred.",
                                    Toast.LENGTH_SHORT).show();
                    } catch (FileNotFoundException e) {

                        e.printStackTrace();
                        Toast.makeText(PictureBarcodeActivity.this, "File not found.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }

    public static Bitmap decodeUri(Context context, Uri uri,
                                   final int requiredSize) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver()
                .openInputStream(uri), null, o);

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;

        while (true) {
            if (width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(context.getContentResolver()
                .openInputStream(uri), null, o2);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (imageUri != null) {
            outState.putString(SAVED_INSTANCE_URI, imageUri.toString());
            outState.putString(SAVED_INSTANCE_RESULT, txtResultBody.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }
}
