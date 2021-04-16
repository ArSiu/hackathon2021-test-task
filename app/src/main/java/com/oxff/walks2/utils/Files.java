package com.oxff.walks2.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Scanner;

public class Files {
    private static String TAG = "Files";

    public static boolean fileExists(Context context, String fileName){
        File file = context.getFileStreamPath(fileName);
        return file.exists();
    }

    public static String readFile(Context context, @NonNull String fileName) {
        FileInputStream inputStream;
        String text = "";
        try {
            inputStream = context.openFileInput(fileName);
            Scanner sc = new Scanner(inputStream);
            StringBuffer sb = new StringBuffer();
            while (sc.hasNext()) {
                sb.append(sc.nextLine());
            }
            text = sb.toString();
            // Log.e(TAG, text);
            inputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
        return text;
    }

    public static void writeFile(Context context, @NonNull String fileName, @NonNull String content, boolean update) {
        FileOutputStream outputStream;
        try {
            if (update) {
                outputStream = context.openFileOutput(fileName, Context.MODE_APPEND);
            } else {
                outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            }
            outputStream.write(content.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Error writing file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
