package com.hyperether.toolbox.graphic;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import com.hyperether.toolbox.HyperApp;
import com.hyperether.toolbox.HyperLog;
import com.hyperether.toolbox.storage.HyperFileManager;
import com.hyperether.toolbox.streaming.HyperDownloadStreamer;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
     *
     * @param path path
     * @param reqWidth required width
     *
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
                HyperLog.getInstance().e(TAG, "decodeBitmapFromFilePath", error.toString());
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
     *
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
                HyperLog.getInstance().e(TAG, "decodeBitmapFromResources", error.toString());
            }
        }
        return null;
    }

    /**
     * Calculation of the In Sample Size
     *
     * @param options Bitmap Factory options
     * @param minDimension smaller dimension
     *
     * @return in sample size
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, float minDimension) {
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
     *
     * @param selectedImage selectedImage
     * @param requiredWidth requiredWidth
     *
     * @return bitmap
     *
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
     *
     * @param fileDescriptor file Descriptor
     * @param reqWidth required Width
     *
     * @return bitmap
     */
    public static Bitmap decodeBitmapFromFileDescriptor(FileDescriptor fileDescriptor,
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
                HyperLog.getInstance().e(TAG, "decodeBitmapFromFileDescriptor", error.toString());
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
            HyperLog.getInstance().e(TAG, "rotateImage", oom.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return correctBmp;
    }

    /**
     * Get Bitmap From File And Save If Rotated
     *
     * @param pictureFile picture File
     * @param filename filename
     * @param quality quality
     * @param format Compress Format
     *
     * @return bitmap
     */
    public static Bitmap getBitmapFromFileRotation(File pictureFile,
                                                   String filename,
                                                   Bitmap.CompressFormat format,
                                                   int quality) {
        Bitmap bitmap = null;
        if (pictureFile != null) {
            bitmap = decodeBitmapFromFilePath(pictureFile.getAbsolutePath(), quality);
            Uri uri = Uri.fromFile(pictureFile);
            if (uri != null) {
                int orientation = getOrientation(uri);
                if (orientation != 0) {
                    Bitmap rotatedBitmap = rotateImage(bitmap, orientation);
                    saveBitmapToFile(rotatedBitmap, filename, format, quality);
                    return rotatedBitmap;
                }
            }
        }
        return bitmap;
    }

    /**
     * Get Bitmap From Uri Rotation and Save to file
     *
     * @param uri uri
     * @param filename filename
     * @param format format
     * @param quality quality
     *
     * @return bitmap
     */
    public static Bitmap getBitmapFromUriSaveRotation(Uri uri,
                                               String filename,
                                               Bitmap.CompressFormat format,
                                               int quality) {
        Bitmap bitmap = null;
        if (uri != null) {
            try {
                bitmap = readBitmapFromUri(uri, 200);
                int orientation = getOrientation(uri);
                if (orientation != 0) {
                    Bitmap b = rotateImage(bitmap, orientation);
                    saveBitmapToFile(b, filename, format, quality);
                    return b;
                } else {
                    saveBitmapToFile(bitmap, filename, format, quality);
                    return bitmap;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * Get image orientation
     *
     * @param uri uri
     *
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
     *
     * @param finalBitmap finalBitmap
     * @param filename filename
     * @param quality quality
     *
     * @return image path
     */
    public static String saveBitmapToFile(Bitmap finalBitmap,
                                          String filename,
                                          Bitmap.CompressFormat format,
                                          int quality) {
        File file = HyperFileManager.getFile(filename);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(format, quality, out);
            out.flush();
            out.close();
            return file.getPath();
        } catch (OutOfMemoryError e) {
            HyperLog.getInstance().e(TAG, "saveBitmapToJpg", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Crop To Square
     *
     * @param bitmap bitmap
     *
     * @return bitmap
     */
    public static Bitmap cropToSquare(Bitmap bitmap) {
        Bitmap b;
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int newWidth = (height > width) ? width : height;
            int newHeight = (height > width) ? height - (height - width) : height;
            int cropW = (width - height) / 2;
            cropW = (cropW < 0) ? 0 : cropW;
            int cropH = (height - width) / 2;
            cropH = (cropH < 0) ? 0 : cropH;
            b = createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
        } catch (OutOfMemoryError error) {
            HyperLog.getInstance().e(TAG, "cropToSquare", error.toString());
            b = bitmap;
        }
        return b;
    }

    /**
     * Get Resized Bitmap
     *
     * @param bm bitmap
     * @param newWidth new width
     * @param newHeight new height
     *
     * @return bitmap
     */
    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        Bitmap b;
        if (bm == null)
            return null;
        try {
            int width = bm.getWidth();
            int height = bm.getHeight();
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            b = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        } catch (OutOfMemoryError error) {
            HyperLog.getInstance().e(TAG, "getResizedBitmap", error.toString());
            b = bm;
        }
        return b;
    }

    /**
     * One bitmap over another
     *
     * @param bmp1 first bitmap
     * @param bmp2 second bitmap
     * @param left left
     * @param top top
     *
     * @return bitmap
     */
    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2, float left, float top) {
        try {
            int maxWidth = (bmp1.getWidth() > bmp2.getWidth() ? bmp1.getWidth() : bmp2.getWidth());
            int maxHeight = (
                    bmp1.getHeight() > bmp2.getHeight() ? bmp1.getHeight() : bmp2.getHeight());
            Bitmap bmOverlay = createBitmap(maxWidth, maxHeight, bmp1.getConfig());
            Canvas canvas = new Canvas(bmOverlay);
            canvas.drawBitmap(bmp1, 0, 0, null);
            canvas.drawBitmap(bmp2, left, top, null);
            return bmOverlay;
        } catch (Exception e) {
            HyperLog.getInstance().e(TAG, "overlay", e);
            return null;
        }
    }

    /**
     * Get Circle from square
     *
     * @param bitmap bitmap
     *
     * @return bitmap
     */
    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output = null;
        try {
            output = createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            // transparent stroke
            final int color = 0x00FFFFFF;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                    bitmap.getHeight() / 2, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
        } catch (OutOfMemoryError oom) {
            HyperLog.getInstance().e(TAG, "getCircleBitmap", oom.getMessage());
        }
        return output;
    }

    /**
     * Decode Bitmap From Input Stream - this method must run in background thread
     *
     * @param url url
     * @param reqWidth required width
     *
     * @return bitmap
     */
    public static Bitmap decodeBitmapFromInputStream(String url, int reqWidth) {
        Bitmap b;
        // default sample size value - in most case it will be used the best sample size option
        // and if it does not, default value will be taken.
        int sampleSize = 4;
        try {
            InputStream stream = HyperDownloadStreamer.getInputStream(url);
            if (stream != null) {
                stream.mark(stream.available());
                // First decode with inJustDecodeBounds=true to check dimensions
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(stream, null, options);

                // Calculate inSampleSize
                sampleSize = calculateInSampleSize(options, reqWidth);
                options.inSampleSize = sampleSize;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    //noinspection deprecation
                    options.inDither = true;
                }
                // Decode bitmap with inSampleSize set
                stream.reset();
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                b = BitmapFactory.decodeStream(stream, null, options);
                stream.close();
                HyperLog.getInstance().d(TAG, "decodeBitmapFromInputStream", "success");
                return b;
            } else {
                HyperLog.getInstance().e(TAG, "decodeBitmapFromInputStream", "fail");
            }
        } catch (OutOfMemoryError error) {
            HyperLog.getInstance().e(TAG, "decodeBitmapFromInputStream", error.getMessage());
        } catch (IOException e) {
            try {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    //noinspection deprecation
                    options.inDither = true;
                }
                options.inSampleSize = sampleSize;
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                InputStream inputStream = HyperDownloadStreamer.getInputStream(url);
                b = BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();
                HyperLog.getInstance().e(TAG, "decodeBitmapFromInputStream",
                        "success, but with errors: " + e);
                return b;
            } catch (IOException e1) {
                HyperLog.getInstance().e(TAG, "decodeBitmapFromInputStream", e1);
            }
        }
        return null;
    }
}
