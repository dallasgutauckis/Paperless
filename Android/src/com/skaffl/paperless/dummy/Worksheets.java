package com.skaffl.paperless.dummy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import com.cloudmine.api.CMApiCredentials;
import com.cloudmine.api.CMFile;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.CMStore;
import com.cloudmine.api.rest.callbacks.FileCreationResponseCallback;
import com.cloudmine.api.rest.callbacks.FileLoadCallback;
import com.cloudmine.api.rest.response.FileCreationResponse;
import com.cloudmine.api.rest.response.FileLoadResponse;
import com.skaffl.paperless.Constants;
import com.skaffl.paperless.R;
import com.skaffl.paperless.view.PaperDrawingView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Worksheets {
    private static final String TAG = "Worksheets";

    public static class Worksheet {
        public Worksheet(String id, String name, Bitmap original, Bitmap student, Bitmap teacher) {
            this.id = id;
            this.name = name;
            this.original = original;
            this.student = student;
            this.teacher = teacher;
        }

        public String id = null;
        public String name = null;

        public Bitmap student = null;
        public Bitmap teacher = null;
        public Bitmap original = null;

        public String toString() {
            return this.name;
        }
    }

    public static List<Worksheet> ITEMS = new ArrayList<Worksheet>();
    public static Map<String, Worksheet> ITEM_MAP = new HashMap<String, Worksheet>();

    static boolean isInitialized = false;

    public static void init(Context context) {

        if (isInitialized) {
            return;
        }

        isInitialized = true;

        CMApiCredentials.initialize(Constants.CM_APP_ID, Constants.CM_API_KEY, context.getApplicationContext());

        Bitmap original;

        original = BitmapFactory.decodeResource(context.getResources(), R.drawable.analogies);
        addItem(new Worksheet("A", "Analogies", original, null, null));
        original = BitmapFactory.decodeResource(context.getResources(), R.drawable.ooo);
        addItem(new Worksheet("B", "Order of Operations", original, null, null));
        addItem(new Worksheet("C", "Circular Logic", original, null, null));

        getFiles();
    }

    public static void getFiles() {
        CMStore.getStore().loadApplicationFile("student.png", new FileLoadCallback("fileId") {
            public void onCompletion(FileLoadResponse loadResponse) {
                Log.v(TAG, "Did we load the file? " + loadResponse.wasSuccess());
                CMFile file = loadResponse.getFile();

                BitmapFactory.Options inOptions = new BitmapFactory.Options();
                inOptions.inMutable = true;

                byte[] fileContents = file.getFileContents();

                if (fileContents != null) {
                    final Bitmap student = Worksheets.ITEMS.get(1).student = BitmapFactory.decodeByteArray(fileContents, 0, fileContents.length, inOptions);
                    if (student != null) {
                        student.setDensity(Bitmap.DENSITY_NONE);
                    }
                }

                if (PaperDrawingView.sCallback != null) {
                    PaperDrawingView.sCallback.onUpdate();
                }
            }
        });
        CMStore.getStore().loadApplicationFile("teacher.png", new FileLoadCallback("fileId") {
            public void onCompletion(FileLoadResponse loadResponse) {
                Log.v(TAG, "Did we load the file? " + loadResponse.wasSuccess());
                CMFile file = loadResponse.getFile();

                BitmapFactory.Options inOptions = new BitmapFactory.Options();
                inOptions.inMutable = true;

                byte[] fileContents = file.getFileContents();

                if (fileContents != null) {
                    final Bitmap teacher = Worksheets.ITEMS.get(1).teacher = BitmapFactory.decodeByteArray(fileContents, 0, fileContents.length, inOptions);
                    if (teacher != null) {
                        teacher.setDensity(Bitmap.DENSITY_NONE);
                    }
                }

                if (PaperDrawingView.sCallback != null) {
                    PaperDrawingView.sCallback.onUpdate();
                }
            }
        });
    }

    private static void addItem(Worksheet item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.name, item);
    }

    public static void resetStudent() {
        CMStore.getStore().deleteApplicationFile("student.png");
        ITEMS.get(1).student = null;

        if (PaperDrawingView.sCallback != null) {
            PaperDrawingView.sCallback.onUpdate();
        }
    }

    public static void saveFiles(final Context context) {
        (new Thread() {
            public void run() {
                String path = context.getApplicationContext().getCacheDir() + "/student";
                FileOutputStream out;

                try {
                    out = new FileOutputStream(path);
                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return;
                }

                Bitmap bmp = Worksheets.ITEMS.get(1).student;
                bmp.compress(Bitmap.CompressFormat.PNG, 99, out);

                FileInputStream fis;
                try {
                    fis = new FileInputStream(path);
                } catch (CreationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return;
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return;
                }

                CMFile file = new CMFile(fis, "student.png", CMFile.IMAGE_PNG_CONTENT_TYPE);
                file.save(new FileCreationResponseCallback() {
                    public void onCompletion(FileCreationResponse response) {
                        Log.v(TAG, "CloudMine: Response: " + response);
                        Toast.makeText(context.getApplicationContext(), "SAVED!!!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(Throwable thrown, String message) {
                        Log.e(TAG, "CloudMine: " + message, thrown);
                        super.onFailure(thrown, message);
                    }
                });
            };
        }).start();
    }
}
