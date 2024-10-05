package com.caurix.distributorauto;

import com.caurix.distributor.R;

import android.app.Activity;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.caurix.duplicate.helper.DBHelper;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentUris;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

/**
 * Created by ti2 on 12/9/16.
 */
public class Export_Activity extends Activity {
    Button export, import_Distributor_data, import_SMS_data;


   /* String dbPath = "/storage/sdcard0/MySmsDatabase";
    String dbPath_se = "/storage/sdcard0/distributor_db";*/

    /*  public static String DB_FILEPATH = "/data/data/com.caurix.distributorauto/databases/MySmsDatabase";
      public static String DB_FILEPATH_se = "/data/data/com.caurix.distributorauto/databases/distributor_db";*/
    public static String DB_FILEPATH = "/data/data/com.caurix.distributor/databases/MySmsDatabase";
    public static String DB_FILEPATH_se = "/data/data/com.caurix.distributor/databases/distributor_db";

    private static final int FILE_SELECT_CODE = 0;
    int selectedFusnction;

    File sd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export_activity);
        setupDir();
        export = (Button) findViewById(R.id.export_btn);
        import_Distributor_data = (Button) findViewById(R.id.import_btn);
        import_SMS_data = (Button) findViewById(R.id.start_file_picker_button);
        export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportDB("MySmsDatabase");
                exportDB("distributor_db");
            }
        });
        import_Distributor_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedFusnction = 0;
                showFileChooser();

            }
        });
        import_SMS_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedFusnction = 1;
                showFileChooser();

            }
        });

    }

    private void setupDir() {
        sd = new File(Environment.getExternalStorageDirectory(), "OrangeMoney/Databases");
        if (!sd.exists())
            if (!sd.mkdirs())
                Toast.makeText(this, "Storage permissions needed, Unable to create directory", Toast.LENGTH_LONG).show();
    }

    private void exportDB(String dbName) {
        ;
        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;

        DateFormat format = new SimpleDateFormat("ddMMyyHHmmssa");
        Date dateobj = new Date();
        String date = format.format(dateobj);
        Log.e("Print_date", "->" + date);

        //String currentDBPath = "/data/" + "com.caurix.distributorauto" + "/databases/" + dbName;
        String currentDBPath = "/data/" + "com.caurix.distributor" + "/databases/" + dbName;

        String backupDBPath = dbName;
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath + date);
        String backup_data = backupDB.toString();
        Log.e("backup_data", "" + backup_data);
        Log.e("currentDB", "" + currentDBPath);
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void import_db(String fromPath, String toPath) throws IOException {
        Log.e("dbPath1", "" + fromPath);
        Log.e("dbPath1", "" + toPath);
        DBHelper mDbHelper;
        mDbHelper = new DBHelper(getApplicationContext());
        mDbHelper.importDatabase(fromPath, toPath);


        DistributorDB distributorDB;
        distributorDB = new DistributorDB(getApplicationContext());
        distributorDB.checkPostedOnServerColumn();
        Toast.makeText(this, "DB Imported!", Toast.LENGTH_LONG).show();


    }

    private void showFileChooser() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = sd;
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;

        FilePickerDialog dialog = new FilePickerDialog(this, properties);
        dialog.setTitle("Select a File to Upload");

        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                //files is the array of the paths of files selected by the Application User.
                try {
                    import_db(files[0], selectedFusnction == 0 ? DB_FILEPATH_se : DB_FILEPATH);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        dialog.show();


//        //Intent.ACTION_OPEN_DOCUMENT fixes the issue, but only on and above kitkat(api 19)
//        Intent intent = new Intent();
//        intent.setAction(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? Intent.ACTION_OPEN_DOCUMENT : Intent.ACTION_GET_CONTENT);
//        intent.setType("file/*");
//
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
//                startActivityForResult(intent, FILE_SELECT_CODE);
//            else
//                startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
//        } catch (android.content.ActivityNotFoundException ex) {
//            // Potentially direct the user to the Market with a Dialog
//            Toast.makeText(this, "Please install a File Manager.",
//                    Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case FILE_SELECT_CODE:
//                if (resultCode == RESULT_OK) {
//                    // Get the Uri of the selected file
//                    Uri uri = data.getData();
//                    Log.d("file Uri", "File Uri: " + uri.toString());
//                    // Get the path
//
//                    String path = null;
//                    try {
//                        path = FileUtils.getPath(this, uri);
//                        if (path == null) {
//                            path = ImageFilePath.getPath(this, uri);
//                        }
//                        Log.d("file path", "File Path: " + path);
//                        if (selectedFusnction == 0) {
//                            import_db(path, DB_FILEPATH_se);
//                        } else {
//                            import_db(path, DB_FILEPATH);
//                        }
//                    } catch (URISyntaxException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    Log.d("file path", "File Path: " + path);
//                    // Get the file instance
//                    // File file = new File(path);
//                    // Initiate the upload
//                }
//                break;
//        }
//
//        super.onActivityResult(requestCode, resultCode, data);
    }


}

class FileUtils {
    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }


        return null;
    }

}

class ImageFilePath {

    /**
     * Method for return file path of Gallery image
     *
     * @param context
     * @param uri
     * @return path of the selected image file from gallery
     */
    public static String getPath(final Context context, final Uri uri) {

        // check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri
                .getAuthority());
    }
}


   

