package com.skaffl.paperless.dummy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.skaffl.paperless.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Worksheets {
    private static final String TAG = "Worksheets";

    public static class Worksheet {
        public Worksheet(String id, String path, Bitmap original, Bitmap student, Bitmap teacher) {
            this.id = id;
            this.path = path;
            this.original = original;
            this.student = student;
            this.teacher = teacher;
        }

        public String id = null;
        public String path = null;

        public Bitmap student = null;
        public Bitmap teacher = null;
        public Bitmap original = null;

        public String toString() {
            return this.path;
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

        // final Bitmap original = BitmapFactory.decodeFile("/sdcard/paper/ooo.png");
        final Bitmap original = BitmapFactory.decodeResource(context.getResources(), R.drawable.ooo);

        addItem(new Worksheet("A", "Differential Equations", original, null, null));
        addItem(new Worksheet("B", "Order of Operations", original, null, null));
        addItem(new Worksheet("C", "Circular Logic", original, null, null));
    }

    private static void addItem(Worksheet item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.path, item);
    }
}
