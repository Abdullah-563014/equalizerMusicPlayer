package power.audio.pro.music.player.utils;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import power.audio.pro.music.player.MainApplication;
import power.audio.pro.music.player.R;
import power.audio.pro.music.player.UIElementHelper.MyDialogBuilder;
import power.audio.pro.music.player.ringtoneCutter.RingdroidEditActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SafUtils {
    public static final String PREF_MICRO_SD_URI = "micro_sd_uri";

    public static final int DOES_NOT_EXIST = 0;
    public static final int WRITABLE_OR_ON_SDCARD = 1;
    public static final int CAN_CREATE_FILES = 2;

    public static int checkFolder(File folder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isOnExtSdCard(folder)) {
                if (!folder.exists() || !folder.isDirectory()) {
                    return DOES_NOT_EXIST;
                }

                if (!isWritableNormalOrSaf(folder)) {
                    return CAN_CREATE_FILES;
                }

                return WRITABLE_OR_ON_SDCARD;
            }

            if (isWritable(new File(folder, "DummyFile"))) {
                return WRITABLE_OR_ON_SDCARD;
            }

            return DOES_NOT_EXIST;
        }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            if (isOnExtSdCard(folder)) {
                return WRITABLE_OR_ON_SDCARD;
            }

            if (isWritable(new File(folder, "DummyFile"))) {
                return WRITABLE_OR_ON_SDCARD;
            }

            return DOES_NOT_EXIST;
        }

        if (isWritable(new File(folder, "DummyFile"))) {
            return WRITABLE_OR_ON_SDCARD;
        }

        return DOES_NOT_EXIST;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isOnExtSdCard(File file) {
        return getExtSdCardFolder(file) != null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getExtSdCardFolder(File file) {
        try {
            String canonicalPath = file.getCanonicalPath();

            for (File f : MainApplication.getAppContext().getExternalFilesDirs("external")) {
                if (f != null && !f.equals(MainApplication.getAppContext().getExternalFilesDir("external"))) {
                    int index = f.getAbsolutePath().lastIndexOf("/Android/data");
                    if (index >= 0) {
                        String path = f.getAbsolutePath().substring(0, index);
                        try {
                            path = new File(path).getCanonicalPath();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (canonicalPath.startsWith(path)) {
                            return path;
                        }
                    }
                }
            }

            if (canonicalPath.startsWith("/storage/sdcard1")) {
                return "/storage/sdcard1";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String[] getExtSdCardPaths() {
        List<String> paths = new ArrayList<>();
        for (File file : MainApplication.getAppContext().getExternalFilesDirs("external")) {
            if (file != null && !file.equals(MainApplication.getAppContext().getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index >= 0) {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    paths.add(path);
                }
            }
        }
        if (paths.isEmpty()) {
            paths.add("/storage/sdcard1");
        }
        return paths.toArray(new String[0]);
    }

    public static boolean isWritableNormalOrSaf(File folder) {
        if (folder == null) {
            return false;
        }

        if (!folder.exists() || !folder.isDirectory()) {
            return false;
        }

        int i = 0;
        File file;
        do {
            String fileName = "AugendiagnoseDummyFile" + (++i);
            file = new File(folder, fileName);
        } while (file.exists());

        if (isWritable(file)) {
            return true;
        }

        DocumentFile document = getDocumentFile(file, false);

        if (document == null) {
            return false;
        }

        boolean result = document.canWrite() && file.exists();

        deleteFile(file);

        return result;
    }

    public static boolean deleteFile(File file) {
        boolean fileDelete = rmDir(file);

        if (file.delete() || fileDelete) {
            return true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isOnExtSdCard(file)) {
            DocumentFile document = getDocumentFile(file, false);
            return document != null && document.delete();
        }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            ContentResolver resolver = MainApplication.getAppContext().getContentResolver();

            try {
                Uri uri = getUriFromFile(file.getAbsolutePath());
                if (uri != null) {
                    resolver.delete(uri, null, null);
                }
            } catch (Exception e) {
                return false;
            }
        }

        return !file.exists();
    }

    public static boolean rmDir(File file) {
        if (!file.exists()) {
            return true;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File child : files) {
                    rmDir(child);
                }
            }
        }

        if (file.delete()) {
            return true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DocumentFile document = getDocumentFile(file, true);
            if (document != null && document.delete()) {
                return true;
            }
        }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            ContentResolver resolver = MainApplication.getAppContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            resolver.delete(MediaStore.Files.getContentUri("external"), MediaStore.MediaColumns.DATA + " = ?", new String[]{file.getAbsolutePath()});
        }

        return !file.exists();
    }

    public static Uri getUriFromFile(final String path) {
        ContentResolver resolver = MainApplication.getAppContext().getContentResolver();

        Cursor cursor = resolver.query(MediaStore.Files.getContentUri("external"),
                new String[]{BaseColumns._ID}, MediaStore.MediaColumns.DATA + " = ?",
                new String[]{path}, MediaStore.MediaColumns.DATE_ADDED + " desc");

        if (cursor == null) {
            return null;
        }

        cursor.moveToFirst();

        if (cursor.isAfterLast()) {
            cursor.close();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, path);
            return resolver.insert(MediaStore.Files.getContentUri("external"), values);
        }

        int imageId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
        Uri uri = MediaStore.Files.getContentUri("external").buildUpon().appendPath(Integer.toString(imageId)).build();
        cursor.close();
        return uri;
    }

    public static DocumentFile getDocumentFile(File file, boolean isDirectory) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return DocumentFile.fromFile(file);
        }

        String baseFolder = getExtSdCardFolder(file);
        boolean originalDirectory = false;
        if (TextUtils.isEmpty(baseFolder)) {
            return null;
        }

        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            if (!baseFolder.equals(fullPath)) {
                relativePath = fullPath.substring(baseFolder.length() + 1);
            } else {
                originalDirectory = true;
            }
        } catch (IOException e) {
            return null;
        } catch (Exception f) {
            originalDirectory = true;
        }

        String microSdUri = SpUtils.getString(PREF_MICRO_SD_URI);
        if (TextUtils.isEmpty(microSdUri)) {
            return null;
        }

        Uri treeUri = Uri.parse(microSdUri);
        if (treeUri == null) {
            return null;
        }

        DocumentFile document = DocumentFile.fromTreeUri(MainApplication.getAppContext(), treeUri);
        if (originalDirectory) {
            return document;
        }

        String[] parts = relativePath.split("/");
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(file.toURI().toString()));
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);

            if (nextDocument == null) {
                if (i < parts.length - 1 || isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                } else {
                    nextDocument = document.createFile(mime == null ? "audio/*" : mime, parts[i]);
                }
            }
            document = nextDocument;
        }

        return document;
    }

    public static boolean isWritable(File file) {
        if (file == null) {
            return false;
        }

        boolean isExisting = file.exists();

        try {
            new FileOutputStream(file, true).close();
        } catch (Exception e) {
            return false;
        }

        boolean result = file.canWrite();

        if (!isExisting) {
            file.delete();
        }

        return result;
    }

    private static boolean checkSystemWritePermission(Context context) {
        boolean retVal = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(context);
//            Log.d(Constants.TAG, "Can Write Settings: " + retVal);
        }
        return retVal;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static void openAndroidPermissionsMenu(Context context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + context.getPackageName()));
        //intent.setData(Uri.parse("package:" + context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void SetRingtone(final Context context, final String filePath , final int id){
        if(!checkSystemWritePermission(context)){
            MaterialDialog dialog = new MyDialogBuilder(context)
                    .title(context.getString(R.string.write_setting_perm_title))
                    .content(context.getString(R.string.write_setting_perm_content))
                    .positiveText(context.getString(R.string.ok))
                    .negativeText(context.getString(R.string.cancel))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            openAndroidPermissionsMenu(context);
                        }
                    })
                    .build();

            //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

            dialog.show();

        }else {

            MaterialDialog dialog = new MyDialogBuilder(context)
                    .title(context.getString(R.string.action_set_as_ringtone))
                    .content(context.getString(R.string.ringtone_content))
                    .positiveText(context.getString(R.string.ringtone_positive_text))
                    .negativeText(context.getString(R.string.ringtone_negative_text))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            //launch ringtone cutter
                            Intent intent = new Intent(context.getApplicationContext(), RingdroidEditActivity.class);
                            intent.putExtra("file_path", filePath);
                            intent.putExtra("was_get_content_intent", false);
                            context.startActivity(intent);
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                            context.getContentResolver().update(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id), values, null, null);
                            Uri uri1 = MediaStore.Audio.Media.getContentUriForPath(filePath);
                            String uris = uri1 + "/" + id;
                            RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, Uri.parse(uris));
                            ToastUtils.shortMsg(R.string.msg_change_ringtone_successful);
                        }
                    })
                    .build();

            //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

            dialog.show();
        }
    }
}
