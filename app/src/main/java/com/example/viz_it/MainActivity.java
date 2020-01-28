package com.example.viz_it;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCapture.CaptureMode;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import de.hdodenhof.circleimageview.CircleImageView;


public final class MainActivity extends AppCompatActivity implements LifecycleOwner {


    private final String[] requiredPermissions = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private static final int requestCodePermission = 101;
    private static final int pickImageCode = 101;
    private TextureView textureView;
    private ImageButton imageButton;
    private StorageReference sReference;
    private Uri uri;
    CircleImageView pimage;
    String currentUserID;
    FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Visitors");
        sReference = FirebaseStorage.getInstance().getReference().child("Images");

        textureView =  findViewById(R.id.photo);
        imageButton = findViewById(R.id.imageButton);


//        <--------if permission granted start the camera else request for granted permission---------->

        if(allPermissions()){

            textureView.post(new Runnable() {
                @Override
                public void run() {
                    startCamera();
                }
            });
        }else {

            ActivityCompat.requestPermissions(this, requiredPermissions, requestCodePermission);
        }

        textureView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (left == oldLeft && top == oldTop && right == oldRight && bottom == oldBottom) {
                    updateTransform();
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == requestCodePermission){
            if(allPermissions()){
                startCamera();
            } else{
               Snackbar.make(findViewById(android.R.id.content), "Permission not granted ", Snackbar.LENGTH_LONG).show();
                finish();
            }
        }
    }



//   <--------------startacamera method---------------->

    private void startCamera() {

        Rational aspectRatio = new Rational(textureView.getWidth(), textureView.getHeight());
        Size size = new Size(textureView.getWidth(), textureView.getHeight());
        PreviewConfig previewConfig = new PreviewConfig.Builder().setTargetAspectRatio(aspectRatio)
                .setTargetResolution(size)
                .build();

        Preview preview = new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {

                ViewGroup parent = (ViewGroup) textureView.getParent();
                parent.removeView(textureView);
                parent.addView(textureView, 0);

                textureView.setSurfaceTexture(output.getSurfaceTexture());
                updateTransform();
            }
        });

        ImageCaptureConfig config =
                new ImageCaptureConfig.Builder().setCaptureMode(CaptureMode.MIN_LATENCY)
                        .setTargetRotation(getWindowManager()
                                .getDefaultDisplay().getRotation())
                        .build();

        final ImageCapture imageCapture = new ImageCapture(config);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//               <----- add path to store the image------>

//                <-----Make new Directory for our app------->

                File folder = new File(Environment.getExternalStorageDirectory() + "/Vizit/");
                if(!folder.exists()){
                    folder.mkdir();
                }

//<----------------------Allocate Location where our captured images will be Saved--------->
                File file = new File(Environment.getExternalStorageDirectory() + "/Vizit/" + System.currentTimeMillis() + ".jpg");

                imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
                        @Override
                        public void onImageSaved(File file) {
                            chooseImage();

                        }


                        @Override
                        public void onError(ImageCapture.UseCaseError useCaseError, String message, Throwable cause) {

                            String msg = "Image Capture failed" + message;
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                            if (cause != null) {
                                cause.printStackTrace();
                            }
                        }
                    });

                }
        });

//        <---------bind to lifecycle---------->
        CameraX.bindToLifecycle(this, preview, imageCapture);

    }

//    <------------update transform method---------------->

    private void updateTransform(){

        Matrix matrix = new Matrix();

        float centreX = textureView.getMeasuredWidth()/2f;
        float centreY = textureView.getMeasuredHeight()/2f;

        int rotationDeg = 0;
        int rotation = (int) textureView.getRotation();

        switch(rotation){

            case Surface.ROTATION_0:
                rotationDeg = 0;
                break;
            case Surface.ROTATION_90:
                rotationDeg = 90;
                break;
            case Surface.ROTATION_180:
                rotationDeg = 270;
                break;
            case Surface.ROTATION_270:
                rotationDeg = 360;
                break;
            default:
        }

        matrix.postRotate((float) -rotationDeg, centreX, centreY);
        textureView.setTransform(matrix);
    }

    //  <-------- check if permission already exists, return false otherwise return true-------->
    private boolean allPermissions() {

        for(String permission : requiredPermissions){

            if(ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){

                return false;
            }
        }
        return true;
    }


// <-----------------method to move from one MainActivity to Phone.class----------->
    private void openPhone() {

        Intent intent = new Intent(this, Phone.class);
        startActivity(intent);
    }

//   <----------choose image to put it on imageview-------->

    public void chooseImage() {

                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, pickImageCode);

            }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == pickImageCode && resultCode == RESULT_OK && data != null) {

            // Get the Uri of data
            uri = data.getData();
            uploadImage();
        }
    }

    //    <---------upload data to Cloud Storage---------->


    public void uploadImage() {


            if (uri != null) {

                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Uploading to Server...");
                progressDialog.show();


                sReference.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
                {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                    {
                        if (!task.isSuccessful())
                        {
                            progressDialog.dismiss();
                            throw task.getException();
                        }
                        return sReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        if (task.isSuccessful())
                        {
                            Uri downloadUri = task.getResult();
                            Upload upload = new Upload(downloadUri.toString());
                            databaseReference.push().setValue(upload);
                            progressDialog.dismiss();
                            openPhone();
                            finish();

                            Toast.makeText(getApplicationContext(), "Upload Successfully", Toast.LENGTH_SHORT).show();
                        } else
                        {
                            Toast.makeText(getApplicationContext(), "upload failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        }

}








