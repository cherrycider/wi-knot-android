package app.com.cherrycider.android.wiknot;

/**
 * Created by V on 27.02.16.
 */

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PersonDetails extends Activity {


    SharedPreferences myProfile;

    private static final String TAG = "myLogs";

    private static final int REQUEST_CAMERA = 1888;
    private static final int SELECT_FILE = 1999;

    // для аватара
    private Uri mImageCaptureUri;
    private ImageView mImageView;
    private Uri mCapturedImageURI;
    private String lastShotPath;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE = 3;
    private static final int REQUEST_CODE_CROP_IMAGE = 4;

    public static String myPhoto;
    public static String myName;
    public static String moreInfo;
    public static boolean hideFace = false;

    boolean nowFun;

    public static String INTRO_INTENT = "com.cherrycider.udpX.intro";

    File wiknotFolder = new File(Environment.getExternalStorageDirectory() + "/wiknot");

    // коструктор вспомогательного класса WiKnotUtils
    WiKnotUtils wiknot = new WiKnotUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myprofile);


        //Log.d(TAG, "PersonDetails is onCreate ");

        // убираем клавиатуру с самого начала показа activity

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );




        // растягиваем фото по ширине экрана

        ImageView imageView = (ImageView) findViewById(R.id.myPhoto);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nophoto);

        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();

        int newWidth = wiknot.getScreenWidth(); //this method should return the width of device screen.
        float scaleFactor = (float) newWidth / (float) imageWidth;
        int newHeight = (int) (imageHeight * scaleFactor);

        bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        imageView.setImageBitmap(bitmap);

        loadMyProfile();

        // если не представился, запускаем intro
        if (myName.equals("null") || myName.equals("") || myName.equals("firsttimetest")) {

            Intent intent = new Intent(INTRO_INTENT);
            startActivity(intent);
        }


        nowFun = false;

    }


    ////////////////////////////////////////////////////////////
    ///// Methods

    // медод показываем лицо

    public void showMyFace(View view) {

        hideFace = false;

        // растягиваем myphoto по ширине экрана и печатаем

        ImageView imageView = (ImageView) findViewById(R.id.myPhoto);

        File myPhotoFile = new File(wiknotFolder, "myPhoto.jpg");
        if (myPhotoFile.exists()) {


            String myPhotoPath = wiknotFolder + "/myPhoto.jpg";

            Bitmap bitmap = BitmapFactory.decodeFile(myPhotoPath);
            int imageWidth = bitmap.getWidth();
            int imageHeight = bitmap.getHeight();

            int newWidth = wiknot.getScreenWidth(); //this method should return the width of device screen.
            float scaleFactor = (float) newWidth / (float) imageWidth;
            int newHeight = (int) (imageHeight * scaleFactor);

            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            imageView.setImageBitmap(bitmap);

        }


        // создаем файл для хранения профиля
        myProfile = getSharedPreferences("myProfile", MODE_PRIVATE);
        SharedPreferences.Editor ed = myProfile.edit();


        // записываем в файл "myProfile"
        ed.putString("myPhoto", String.valueOf("myPhoto.jpg"));


    }


    // медод прячем лицо

    public void hideMyFace(View view) {

        hideFace = true;

        // растягиваем nophoto по ширине экрана и печатаем

        ImageView imageView = (ImageView) findViewById(R.id.myPhoto);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nophoto);

        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();

        int newWidth = wiknot.getScreenWidth(); //this method should return the width of device screen.
        float scaleFactor = (float) newWidth / (float) imageWidth;
        int newHeight = (int) (imageHeight * scaleFactor);

        bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        imageView.setImageBitmap(bitmap);

        //TODO сохраняем bitmap в файл noPhoto.jpg и записываем в shared preferences
        // создаем файл noPhoto.jpg

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(wiknotFolder, "noPhoto.jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // создаем файл для хранения профиля
        myProfile = getSharedPreferences("myProfile", MODE_PRIVATE);
        SharedPreferences.Editor ed = myProfile.edit();


        // записываем в файл "myProfile"
        ed.putString("myPhoto", String.valueOf("noPhoto.jpg"));

    }


    /////   Процедура создания диалога выбора картинки для профайла

    public void selectImageFromCameraOrGallery(View view) {
        //final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        final CharSequence[] items = {getString(R.string.Take_Photo), getString(R.string.Choose_from_Library), getString(R.string.Cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(PersonDetails.this);
        builder.setTitle(getString(R.string.Add_Photo));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                //if (items[item].equals("Take Photo")) {
                if (items[item].equals(getString(R.string.Take_Photo))) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "pictureForWiKnotAvatar");
                    mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intentPicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intentPicture.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                    startActivityForResult(intentPicture, REQUEST_CAMERA);

                } else if (items[item].equals(getString(R.string.Choose_from_Library))) {
                    //} else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    //TODO здесь разобраться с интентом на нужные программы
                    intent.setType("image/*");
                    //startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);

                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), SELECT_FILE);


                } else if (items[item].equals(getString(R.string.Cancel))) {
                    //} else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /// процедура принятия картинки без  компрессирования
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ImageView myPhoto = (ImageView) findViewById(R.id.myPhoto);

        if (resultCode == RESULT_OK) {

            // если выбрана камера, приходит снимок
            if (requestCode == REQUEST_CAMERA) {

                if (mCapturedImageURI == null) {


                    //эта часть взята из статьи Null Intent passed back On Samsung Galaxy Tab…
                    // ссылка https://kevinpotgieter.wordpress.com/2011/03/30/null-intent-passed-back-on-samsung-galaxy-tab/


                    // Describe the columns you'd like to have returned.
                    // Selecting from the Thumbnails location gives you both the Thumbnail Image ID,
                    // as well as the original image ID
                    String[] projection = {
                            MediaStore.Images.Thumbnails._ID,  // The columns we want
                            MediaStore.Images.Thumbnails.IMAGE_ID,
                            MediaStore.Images.Thumbnails.KIND,
                            MediaStore.Images.Thumbnails.DATA};
                    String selection = MediaStore.Images.Thumbnails.KIND + "=" + // Select only mini's
                            MediaStore.Images.Thumbnails.MINI_KIND;

                    String sort = MediaStore.Images.Thumbnails._ID + " DESC";

                    //At the moment, this is a bit of a hack,
                    // as I'm returning ALL images, and just taking the latest one.
                    // There is a better way to narrow this down I think with a WHERE clause which is currently the selection variable
                    Cursor myCursor = this.managedQuery(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, selection, null, sort);

                    long imageId = 0l;
                    long thumbnailImageId = 0l;
                    //String thumbnailPath = "";

                    try {
                        myCursor.moveToFirst();
                        imageId = myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID));
                        thumbnailImageId = myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID));
                        //thumbnailPath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
                    } finally {
                        myCursor.close();
                    }

                    //Create new Cursor to obtain the file Path for the large image

                    String[] largeFileProjection = {
                            MediaStore.Images.ImageColumns._ID,
                            MediaStore.Images.ImageColumns.DATA
                    };

                    String largeFileSort = MediaStore.Images.ImageColumns._ID + " DESC";
                    myCursor = this.managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, largeFileProjection, null, null, largeFileSort);
                    //String largeImagePath = "";

                    try {
                        myCursor.moveToFirst();

                        //This will actually give yo uthe file path location of the image.
                        //largeImagePath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
                    } finally {
                        myCursor.close();
                    }
                    // These are the two URI's you'll be interested in. They give you a handle to the actual images
                    Uri uriLargeImage = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(imageId));
                    Uri uriThumbnailImage = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, String.valueOf(thumbnailImageId));

                    // I've left out the remaining code, as all I do is assign the URI's to my own objects anyways...


                    //конец части
                    //эта часть взята из статьи Null Intent passed back On Samsung Galaxy Tab…
                    // ссылка https://kevinpotgieter.wordpress.com/2011/03/30/null-intent-passed-back-on-samsung-galaxy-tab/


                    // берем большой файл из части из статьи выше
                    lastShotPath = getRealPathFromURI(uriLargeImage);

                } else {


                    // это было:
                    lastShotPath = getRealPathFromURI(mCapturedImageURI);

                }

                // make file from path lastShotPath
                int end = lastShotPath.toString().lastIndexOf("/");
                String str1 = lastShotPath.toString().substring(0, end);
                String str2 = lastShotPath.toString().substring(end + 1, lastShotPath.length());
                File lastShotFile = new File(str1, str2);

                //Log.d (TAG, "PersonDetails: send to crop lastShotFile " + str1 + str2);

                // снимок автоматически отсылаем обрезать
                doMyCrop(lastShotFile);


            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
                        null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();

                String selectedImagePath = cursor.getString(column_index);
                File destination = new File(wiknotFolder, "myPhoto.jpg");

                //Log.d(TAG, "PersonDetails: start copy from " + selectedImagePath + " to " + destination);

                //  write file from selectedImagePath to destination


                // public static boolean copyFile(String from, String to) {
                try {
                    File sd = wiknotFolder;
                    if (sd.canWrite()) {
                        int end = selectedImagePath.toString().lastIndexOf("/");
                        String str1 = selectedImagePath.toString().substring(0, end);
                        String str2 = selectedImagePath.toString().substring(end + 1, selectedImagePath.length());
                        File source = new File(str1, str2);
                        //File destination= new File(to, str2);
                        if (source.exists()) {
                            FileChannel src = new FileInputStream(source).getChannel();
                            FileChannel dst = new FileOutputStream(destination).getChannel();
                            dst.transferFrom(src, 0, src.size());
                            src.close();
                            dst.close();
                        }
                    }

                } catch (Exception e) {
                    Log.d(TAG, "PersonDetails: Exception " + e);
                }


                // снимок автоматически отсылаем обрезать
                doMyCrop(destination);


                //myPhoto.setImageBitmap(picFromGallery);


                // принятие файла после компрессирования и копирование его в myPhoto.jpg
            } else if (requestCode == REQUEST_CODE_CROP_IMAGE) {

                String path = data.getStringExtra(CropImage.IMAGE_PATH);

                // if nothing received
                if (path == null) {

                    Toast toast = Toast.makeText(this, this.getString(R.string.Picture_NOT_saved), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();

                    Log.d(TAG, "PersonDetails: Picture NOT saved!!! " + path);
                    return;
                }

                // cropped bitmap
                Bitmap bitmap = BitmapFactory.decodeFile(path);

                //Toast.makeText(this, "Picture saved " + path, Toast.LENGTH_SHORT).show();
                //Log.d(TAG, "PersonDetails: Picture saved " + path);
                myPhoto.setImageBitmap(bitmap);

                // copy cropped file to myPhoto.jpg file

                // public static boolean copyFile(String from, String to) {
                try {
                    File sd = wiknotFolder;
                    if (sd.canWrite()) {
                        int end = path.toString().lastIndexOf("/");
                        String str1 = path.toString().substring(0, end);
                        String str2 = path.toString().substring(end + 1, path.length());
                        File source = new File(str1, str2);
                        File destination = new File(wiknotFolder, "myPhoto.jpg");
                        if (source.exists() && (!(str2.matches("myPhoto.jpg")))) {
                            FileChannel src = new FileInputStream(source).getChannel();
                            FileChannel dst = new FileOutputStream(destination).getChannel();
                            dst.transferFrom(src, 0, src.size());
                            src.close();
                            dst.close();

                            //Log.d(TAG, "PersonDetails: cropped file saved to  " + wiknotFolder + "/myPhoto.jpg");
                        }


                    }

                } catch (Exception e) {
                    Log.d(TAG, "PersonDetails: Exception " + e);
                }

                // растягиваем картинку по экрану

                ImageView imageView = (ImageView) findViewById(R.id.myPhoto);


                int imageWidth = bitmap.getWidth();
                int imageHeight = bitmap.getHeight();

                int newWidth = wiknot.getScreenWidth(); //this method should return the width of device screen.
                float scaleFactor = (float) newWidth / (float) imageWidth;
                int newHeight = (int) (imageHeight * scaleFactor);

                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                imageView.setImageBitmap(bitmap);


                Toast toast = Toast.makeText(this, this.getString(R.string.Picture_saved), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();

            }
        }

    }


//----------------------------------------

    /**
     * This method is used to get real path of file from from uri
     *
     * @param contentUri
     * @return String
     */
    //----------------------------------------
    public String getRealPathFromURI(Uri contentUri) {
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = managedQuery(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            return contentUri.getPath();
        }
    }


    // метод после получения фотографии или выбора из галереи вырезает квадрат
    private void doMyCrop(File fileToCrop) {

        // create explicit intent
        Intent cropIntent = new Intent(this, CropImage.class);

        // tell CropImage activity to look for image to crop
        //String filePath = mImageCaptureUri.toString();

        //Log.d(TAG, "PersonDetails: mImageCaptureUri " + mImageCaptureUri +" converting to string before send to crop");
        //File file = new File(mImageCaptureUri.toString());
        //String filePath = file.getAbsolutePath();
        //Log.d(TAG, "PersonDetails: filePath " + filePath +" converted to string before send to crop");

        //String filePath = "/storage/sdcard0/myPhoto.jpg";

        //File fileToCrop = new File(wiknotFolder,"myPhoto.jpg");
        String filePath = fileToCrop.getAbsolutePath();

        cropIntent.putExtra(CropImage.IMAGE_PATH, filePath);

        // allow CropImage activity to rescale image
        cropIntent.putExtra(CropImage.SCALE, true);

        // if the aspect ratio is fixed to ratio 3/4
        cropIntent.putExtra(CropImage.ASPECT_X, 4);
        cropIntent.putExtra(CropImage.ASPECT_Y, 3);

        // start activity CropImage with certain request code and listen
        // for result
        startActivityForResult(cropIntent, REQUEST_CODE_CROP_IMAGE);
    }



/*

 *
    /////   Процедура создания диалога выбора картинки для профайла

    public void selectImageFromCameraOrGallery(View view) {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };



        AlertDialog.Builder builder = new AlertDialog.Builder(PersonDetails.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /// процедура принятия картинки и компрессирование (без кропа)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ImageView myPhoto = (ImageView) findViewById(R.id.myPhoto);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

                File destination = new File(wiknotFolder,
                        System.currentTimeMillis() + "myPhoto.jpg");

                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                myPhoto.setImageBitmap(thumbnail);

            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
                        null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();

                String selectedImagePath = cursor.getString(column_index);

                Bitmap bm;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 400;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bm = BitmapFactory.decodeFile(selectedImagePath, options);

                myPhoto.setImageBitmap(bm);
            }
        }

    }

    */


    // сохраняем все данные профиля в файл Shared Preferences

    public void saveMyProfile(View view) {

        // прячем клавиатуру
        wiknot.hideKeyboard(this);


        // создаем файл для хранения профиля
        myProfile = getSharedPreferences("myProfile", MODE_PRIVATE);
        SharedPreferences.Editor ed = myProfile.edit();

        // получаем данные из полей ввода и заменяем запятые на |comma|

        EditText nameEditText = (EditText) findViewById(R.id.myName);
        myName = nameEditText.getText().toString().replace(",", "|comma|");
        if (myName.equals("")) {
            Toast.makeText(this, this.getString(R.string.Please_write_your_name), Toast.LENGTH_SHORT).show();
            return;
        }


        EditText infoEditText = (EditText) findViewById(R.id.moreInfo);
        moreInfo = infoEditText.getText().toString().replace(",", "|comma|");

        if (moreInfo.equals("")) {
            moreInfo = "|no info|";
        }

        String myID = MainActivity.myID;

        // здесь меняем myPhoto.jpg на nophoto.jpg
        // записываем в файл "myProfile"

        if (hideFace) {
            ed.putString("myPhoto", String.valueOf("noPhoto.jpg"));
        } else {
            ed.putString("myPhoto", String.valueOf("myPhoto.jpg"));
        }
        ed.putString("myID", String.valueOf(myID));
        ed.putString("myName", String.valueOf(myName));
        ed.putString("moreInfo", String.valueOf(moreInfo));
        ed.commit();


        Toast.makeText(this, this.getString(R.string.My_Profile_is_saved), Toast.LENGTH_SHORT).show();
    }


    void loadMyProfile() {

        myProfile = getSharedPreferences("myProfile", MODE_PRIVATE);

        // достаем значения из файла "myProfile"
        myPhoto = myProfile.getString("myPhoto", String.valueOf(myPhoto));

        myName = myProfile.getString("myName", String.valueOf(myName));

        moreInfo = myProfile.getString("moreInfo", String.valueOf(moreInfo));
        if (moreInfo.equals("|no info|")) {
            moreInfo = "";
        }

        //Log.d(TAG, "my profile:  " + myPhoto + " " + myName + " " + moreInfo);

        // растягиваем и печатаем  фото  или nophoto

        ImageView imageView = (ImageView) findViewById(R.id.myPhoto);
        Bitmap bitmap;
        //File myPhotoFile = new File(wiknotFolder, "myPhoto.jpg");

        File myPhotoFile = new File(wiknotFolder, myPhoto);


        if (myPhotoFile.exists()) {
            bitmap = BitmapFactory.decodeFile(myPhotoFile.getPath());
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nophoto);
        }

        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();

        int newWidth = wiknot.getScreenWidth(); //this method should return the width of device screen.
        float scaleFactor = (float) newWidth / (float) imageWidth;
        int newHeight = (int) (imageHeight * scaleFactor);

        bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        imageView.setImageBitmap(bitmap);


        // заливаем в поля сохраненные значения
        EditText nameEditText = (EditText) findViewById(R.id.myName);
        if (!myName.equals("null")) {

            //заменяем |comma| на запятые и выводим на печать
            String myNameWithCommas = myName.replace("|comma|", ",");
            nameEditText.setText(myNameWithCommas, TextView.BufferType.EDITABLE);
        }
        EditText infoEditText = (EditText) findViewById(R.id.moreInfo);
        if (!moreInfo.equals("null")) {
            //заменяем |comma| на запятые и выводим на печать
            String moreInfoWithCommas = moreInfo.replace("|comma|", ",");
            infoEditText.setText(moreInfoWithCommas, TextView.BufferType.EDITABLE);
        }

        //Toast.makeText(this, "Preferences loaded", Toast.LENGTH_SHORT).show();
    }


    public void onDestroy() {
        super.onDestroy();

        // если в профайле не заполнено имя,
        myProfile = getSharedPreferences("myProfile", MODE_PRIVATE);
        myName = myProfile.getString("myName", String.valueOf(myName));
        if (myName.equals("null") || myName.equals("")) {
            //Toast.makeText(this, "onDestroy()with no name ", Toast.LENGTH_SHORT).show();
            return;

        }

    }

    public void backArrowMyProfile(View view) {

        onBackPressed();
    }

    @Override
    public void onBackPressed() {

        // если в профайле не заполнено имя,
        myProfile = getSharedPreferences("myProfile", MODE_PRIVATE);
        myName = myProfile.getString("myName", String.valueOf(myName));
        if (myName.equals("null") || myName.equals("")) {
            Toast.makeText(this, this.getString(R.string.Please_write_your_name), Toast.LENGTH_SHORT).show();
            return;

        } else super.onBackPressed();
    }



    public void fillMoreInfo(View view) {

        FillBattonProgressbar fillBattonProgressbar = new FillBattonProgressbar();
        fillBattonProgressbar.execute();

        if (!nowFun) {

            // заливаем в поле форму для примера
            EditText infoEditText = (EditText) findViewById(R.id.moreInfo);

            infoEditText.setText(getString(R.string.status_1), TextView.BufferType.EDITABLE);
            nowFun = true;
            return;
        }else {

            // выбираем наудачу изречение
            // расфасовываем сообщения с запятыми в массив statusesList
            String funny_statuses = getString(R.string.funny_statuses);
            ArrayList statusesList = new ArrayList(Arrays.asList(funny_statuses.split(",")));
            // выбираем любое

            int min = 1;
            int max = statusesList.size();


            Random r = new Random();
            int i = r.nextInt(max - min) + min;

            String status = String.valueOf(statusesList.get(i));
            //заменяем |comma| на запятые и выводим на печать
            String statusWithCommas = status.replace("|comma|", ",");
            // заливаем
            EditText infoEditText = (EditText) findViewById(R.id.moreInfo);
            infoEditText.setText(statusWithCommas, TextView.BufferType.EDITABLE);


        }







    }





    // класс асинхронно запускает прогрессбар на 1 сек
    class FillBattonProgressbar extends AsyncTask<Void, Void, Void> {

        ImageView fillBtn;
        ProgressBar progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            fillBtn = (ImageView)findViewById(R.id.helptofill);
            progress = (ProgressBar) findViewById(R.id.helptofillprogress);
            fillBtn.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            //redrawOnlineUsers();

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {

                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            fillBtn.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);


        }
    }










}

