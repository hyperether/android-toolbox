package com.hyperether.toolbox.graphic;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import com.hyperether.toolbox.HyperApp;
import com.hyperether.toolbox.HyperLog;
import com.hyperether.toolbox.storage.HyperFileManager;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;

import static android.graphics.Bitmap.createBitmap;

/**
 * HyperImageProcessing - image manipulation, save and load
 *
 * @author Marko Katic
 * @version 1.0 - 19/02/18.
 */

public class HyperImageProcessing {

    private static final String TAG = HyperImageProcessing.class.getSimpleName();

    /**
     * Decode Bitmap From File Path
     * @param path path
     * @param reqWidth required width
     * @return bitmap
     */
    public static Bitmap decodeBitmapFromFilePath(String path, int reqWidth) {
        Bitmap b;
        if (path != null) {
            try {
                // First decode with inJustDecodeBounds=true to check dimensions
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, options);

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, reqWidth);
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    //noinspection deprecation
                    options.inDither = true;
                }
                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                b = BitmapFactory.decodeFile(path, options);
                return b;
            } catch (OutOfMemoryError error) {
                HyperLog.getInstance().e(TAG, "decodeBitmapFromFilePath",
                        "decodeBitmapFromFilePath OutOfMemory: " + error.toString());
            }
        }
        return null;
    }

    /**
     * Decode Bitmap From Resources
     *
     * @param res Resources
     * @param id id
     * @param minDimension smaller dimension
     * @return bitmap
     */
    public static Bitmap decodeBitmapFromResources(Resources res, int id, int minDimension) {
        Bitmap b;
        if (res != null && id != -1 && id != 0) {
            try {
                // First decode with inJustDecodeBounds=true to check dimensions
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(res, id, options);

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, minDimension);
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    //noinspection deprecation
                    options.inDither = true;
                }
                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                b = BitmapFactory.decodeResource(res, id, options);
                return b;
            } catch (OutOfMemoryError error) {
                HyperLog.getInstance().e(TAG, "decodeBitmapFromResources",
                        "decodeBitmapFromResources OutOfMemory: " + error.toString());
            }
        }
        return null;
    }

    /**
     * Calculation of the In Sample Size
     * @param options Bitmap Factory options
     * @param minDimension smaller dimension
     * @return in sample size
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, float minDimension) {
        float reqHeight;
        float reqWidth;
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        // calculation of requiredHeight
        // Important**** after generating, bitmap may be rotated later so minDimension is used to
        // set minRequired width/height to fit layout properly
        if (height > width) {
            reqWidth = minDimension;
            reqHeight = (height * reqWidth) / width;
        } else {
            reqHeight = minDimension;
            reqWidth = (width * reqHeight) / height;
        }

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Read Bitmap From Uri
     * @param selectedImage selectedImage
     * @param requiredWidth requiredWidth
     * @return bitmap
     * @throws Exception exception
     */
    public static Bitmap readBitmapFromUri(Uri selectedImage, int requiredWidth) throws Exception {
        Bitmap bm = null;
        ParcelFileDescriptor parcelFileDescriptor;
        parcelFileDescriptor = HyperApp.getInstance().getApplicationContext()
                .getContentResolver().openFileDescriptor(selectedImage, "r");
        FileDescriptor fileDescriptor = null;
        if (parcelFileDescriptor != null) {
            fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        }
        if (fileDescriptor != null) {
            bm = decodeBitmapFromFileDescriptor(fileDescriptor, requiredWidth);
            parcelFileDescriptor.close();
        }
        return bm;
    }

    /**
     * Decode Bitmap From File Descriptor
     * @param fileDescriptor file Descriptor
     * @param reqWidth required Width
     * @return bitmap
     */
    private static Bitmap decodeBitmapFromFileDescriptor(FileDescriptor fileDescriptor,
                                                         float reqWidth) {
        Bitmap b;
        if (fileDescriptor != null) {
            try {
                // First decode with inJustDecodeBounds=true to check dimensions
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, reqWidth);
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    //noinspection deprecation
                    options.inDither = true;
                }
                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                b = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
                return b;
            } catch (OutOfMemoryError error) {
                HyperLog.getInstance().e(TAG, "decodeBitmapFromFileDescriptor",
                        "decodeBitmapFromFileDescriptor OutOfMemory: " + error.toString());
            }
        }
        return null;
    }

    /**
     * Correction of the image orientation
     *
     * @return bitmap with proper rotation
     */
    public static Bitmap rotateImage(Bitmap bmp, int orientation) {
        Bitmap correctBmp = bmp;
        try {
            int angle = 0;

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                angle = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                angle = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                angle = 270;
            }

            Matrix mat = new Matrix();
            mat.postRotate(angle);
            correctBmp = createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);

        } catch (OutOfMemoryError oom) {
            HyperLog.getInstance().e(TAG, "fixImageRotation - OOM",
                    "rotateImage OutOfMemory " + oom.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return correctBmp;
    }

    /**
     * Get image orientation
     * @param uri uri
     * @return orientation
     */
    public static int getOrientation(Uri uri) {
        int orientation = 0;
        if (uri != null) {
            try {
                ExifInterface exif = null;

                String realImgPath;
                realImgPath = HyperFileManager
                        .getFilePathFromUri(HyperApp.getInstance().getApplicationContext(), uri);

                File pictureFile = null;
                if (realImgPath != null) {
                    pictureFile = new File(realImgPath);
                }
                if (pictureFile != null) {
                    exif = new ExifInterface(pictureFile.getAbsolutePath());
                }
                if (exif != null) {
                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface
                            .ORIENTATION_NORMAL);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return orientation;
    }

    /**
     * Save Bitmap To Jpg
     * @param context context
     * @param finalBitmap finalBitmap
     * @param filename filename
     * @param quality quality
     * @return image path
     */
    public static String saveBitmapToJpg(Context context, Bitmap finalBitmap, String filename,
                                         int quality) {
        File file = HyperFileManager.getFile(context, filename);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
            out.flush();
            out.close();
            return file.getPath();
        } catch (OutOfMemoryError e) {
            HyperLog.getInstance().e(TAG, "saveBitmapToJpg", "OutOfMemory");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
