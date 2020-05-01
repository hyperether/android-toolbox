package com.hyperether.toolbox.graphic;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.hyperether.toolbox.HyperApp;
import com.hyperether.toolbox.HyperLog;
import com.hyperether.toolbox.storage.HyperFileManager;
import com.hyperether.toolbox.streaming.HyperDownloadStreamer;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.graphics.Bitmap.createBitmap;

/**
 * HyperImageProcessing - image manipulation, save and load
 *
 * @author Marko Katic
 * @author Slobodan Prijic
 * @version 1.1 - 04/07/2020
 */

public class HyperImageProcessing {

    private static final String TAG = HyperImageProcessing.class.getSimpleName();

    /**
     * Decode Bitmap From File Path
     *
     * @param path     path
     * @param reqWidth required width
     * @return bitmap
     */
    public static Bitmap decodeBitmapFromFilePath(String path, int reqWidth) {
        Bitmap b;
        if (path != null) {
            try {
                // First decode with inJustDecodeBounds=true to check dimensions
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, options);
                options = prepareOptions(options, reqWidth);
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
     * @param res      Resources
     * @param id       id
     * @param reqWidth smaller dimension
     * @return bitmap
     */
    public static Bitmap decodeBitmapFromResources(Resources res, int id, int reqWidth) {
        Bitmap b;
        if (res != null && id != -1 && id != 0) {
            try {
                // First decode with inJustDecodeBounds=true to check dimensions
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(res, id, options);
                options = prepareOptions(options, reqWidth);
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
     * @param options      Bitmap Factory options
     * @param minDimension smaller dimension
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

    public static Bitmap getBitmapFromUri(Uri uri, int requiredWidth) throws Exception {
        Bitmap bm = null;
        Context c = HyperApp.getInstance().getApplicationContext();
        try {
            bm = MediaStore.Images.Media.getBitmap(c.getContentResolver(), uri);
        } catch (IOException e) {
            HyperLog.getInstance().e(TAG, "-readBitmapFromUri: " + uri, e);
        }
        if (bm != null) {
            if (requiredWidth > 0)
                bm = getResizedBitmap(bm, requiredWidth, bm.getHeight());
            return bm;
        }

        try {
            bm = BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            HyperLog.getInstance().e(TAG, "--readBitmapFromUri: " + uri, e);
        } catch (OutOfMemoryError oom) {
            HyperLog.getInstance().e(TAG, "--readBitmapFromUri OOM", oom.getLocalizedMessage());
        }

        if (bm != null) {
            if (requiredWidth > 0)
                bm = getResizedBitmap(bm, requiredWidth, bm.getHeight());
            return bm;
        }

        ParcelFileDescriptor parcelFileDescriptor;
        parcelFileDescriptor = c.getContentResolver().openFileDescriptor(uri, "r");
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
     * Read Bitmap From Uri
     *
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
     *
     * @param fileDescriptor file Descriptor
     * @param reqWidth       required Width
     * @return bitmap
     */
    public static Bitmap decodeBitmapFromFileDescriptor(FileDescriptor fileDescriptor,
                                                        int reqWidth) {
        Bitmap b;
        if (fileDescriptor != null) {
            try {
                // First decode with inJustDecodeBounds=true to check dimensions
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
                options = prepareOptions(options, reqWidth);
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
     * @param width       required image width. If set to -1, width is original
     * @return result bitmap
     */
    public static Bitmap getBitmapRotated(File pictureFile, int width) {
        Bitmap bitmap = null;
        if (pictureFile != null) {
            bitmap = decodeBitmapFromFilePath(pictureFile.getAbsolutePath(), width);
            Uri uri = Uri.fromFile(pictureFile);
            if (uri != null) {
                int orientation = getOrientation(uri);
                if (orientation != 0) {
                    return rotateImage(bitmap, orientation);
                }
            }
        }
        return bitmap;
    }

    /**
     * Get Bitmap From Uri Rotation and Save to file
     *
     * @param uri   uri
     * @param width required image width. If set to -1, width is original
     * @return bitmap
     */
    public static Bitmap getBitmapRotated(Uri uri, int width) {
        Bitmap bitmap = null;
        if (uri != null) {
            try {
                bitmap = readBitmapFromUri(uri, width);
                int orientation = getOrientation(uri);
                if (orientation != 0) {
                    return rotateImage(bitmap, orientation);
                } else {
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
     * @param filename    filename
     * @param quality     quality
     * @return image path
     */
    public static String compressBitmapToFile(Bitmap finalBitmap,
                                              String filename,
                                              Bitmap.CompressFormat format,
                                              int quality) {
        File file = HyperFileManager.getFile(filename);
        return compressBitmapToFile(finalBitmap, file, format, quality);
    }

    /**
     * Save Bitmap To Jpg
     *
     * @param bitmap   finalBitmap
     * @param fileName filename
     * @param quality  quality
     * @return image path or NULL
     */
    public static String compressBitmapToTempFile(Bitmap bitmap,
                                                  String fileName,
                                                  Bitmap.CompressFormat format,
                                                  int quality) {
        File file = null;
        try {
            file = File.createTempFile(fileName, ".jpg",
                    HyperApp.getInstance().getApplicationContext().getCacheDir());
        } catch (IOException e) {
            HyperLog.getInstance().e(TAG, "compressBitmapToTempFile", e);
        }
        return compressBitmapToFile(bitmap, file, format, quality);
    }

    /**
     * Save Bitmap To Jpg
     *
     * @param bitmap  finalBitmap
     * @param file    file
     * @param quality quality
     * @return image path
     */
    public static String compressBitmapToFile(Bitmap bitmap,
                                              File file,
                                              Bitmap.CompressFormat format,
                                              int quality) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(format, quality, out);
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
     * @return bitmap
     */
    public static Bitmap cropToSquare(Bitmap bitmap) {
        Bitmap b;
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int newWidth = Math.min(height, width);
            int newHeight = (height > width) ? height - (height - width) : height;
            int cropW = (width - height) / 2;
            cropW = Math.max(cropW, 0);
            int cropH = (height - width) / 2;
            cropH = Math.max(cropH, 0);
            b = createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
        } catch (OutOfMemoryError error) {
            HyperLog.getInstance().e(TAG, "cropToSquare", error.toString());
            b = bitmap;
        }
        return b;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth) {
        if (bm == null)
            return bm;
        float scale = ((float) newWidth) / ((float) bm.getWidth());
        return getResizedBitmap(bm, newWidth, (int) ((float) bm.getHeight() * scale));
    }

    /**
     * Get Resized Bitmap
     *
     * @param bm        bitmap
     * @param newWidth  new width
     * @param newHeight new height
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
     * @param top  top
     * @return bitmap
     */
    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2, float left, float top) {
        try {
            int maxWidth = Math.max(bmp1.getWidth(), bmp2.getWidth());
            int maxHeight = Math.max(bmp1.getHeight(), bmp2.getHeight());
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
     * @return bitmap
     */
    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output = null;
        try {
            output = createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            // transparent stroke
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                    bitmap.getHeight() / 2, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
        } catch (OutOfMemoryError oom) {
            HyperLog.getInstance().e(TAG, "getCircleBitmap", oom.getMessage());
        }
        return output;
    }

    public static Bitmap getBitmap(int drawableRes) {
        Drawable drawable = HyperApp.getInstance().getApplicationContext().getResources()
                .getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Decode Bitmap From Input Stream - this method must run in background thread
     *
     * @param url      url
     * @param reqWidth required width
     * @return bitmap
     */
    public static Bitmap decodeBitmapFromInputStream(String url, int reqWidth) {
        Bitmap b;
        BitmapFactory.Options options = null;
        try {
            InputStream stream = HyperDownloadStreamer.getInputStream(url);
            if (stream != null) {
                stream.mark(stream.available());
                // First decode with inJustDecodeBounds=true to check dimensions
                options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(stream, null, options);

                options = prepareOptions(options, reqWidth);
                // Decode bitmap with inSampleSize set
                stream.reset();
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
                InputStream inputStream = HyperDownloadStreamer.getInputStream(url);
                b = BitmapFactory
                        .decodeStream(inputStream, null, prepareOptions(options, reqWidth));
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

    /**
     * Prepare Bitmap Options
     *
     * @param options  input options
     * @param reqWidth required width
     * @return options
     */
    private static BitmapFactory.Options prepareOptions(BitmapFactory.Options options,
                                                        int reqWidth) {
        // Calculate inSampleSize
        if (options != null) {
            if (reqWidth > -1) {
                options.inSampleSize = calculateInSampleSize(options, reqWidth);
            } else {
                options.inSampleSize = 1;
            }
        } else {
            options = new BitmapFactory.Options();
            // set default sample size
            options.inSampleSize = 1;
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            //noinspection deprecation
            options.inDither = true;
        }
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return options;
    }

    public static Bitmap combineImages(Bitmap bmBack,
                                       Bitmap bmFront,
                                       float frontRatio,
                                       float frontOffset) {
        float width = bmBack.getWidth();
        Bitmap b = null;
        if (bmFront != null) {
            float avatarWith = bmFront.getWidth();
            float avatarHeight = bmFront.getHeight();

            if (avatarWith != avatarHeight) {
                bmFront = cropToSquare(bmFront);
                avatarWith = bmFront.getWidth();
                avatarHeight = bmFront.getHeight();
            }

            Bitmap avatarResized;
            float avWith;
            float avHeight;

            avWith = width * frontRatio;
            double k = avWith / avatarWith;

            if (k > 1) {
                avHeight = (float) k * avatarHeight;
                if (avatarWith > avatarHeight) {
                    avHeight = width * frontRatio;
                    k = avHeight / avatarHeight;
                    avWith = (float) k * avatarWith;
                }
            } else {
                if (avatarWith > avatarHeight) {
                    avHeight = width * frontRatio;
                    k = avHeight / avatarHeight;
                    avWith = (float) k * avatarWith;
                } else {
                    avHeight = (float) k * avatarHeight;
                }
            }

            if (avatarWith != 1) {
                avatarResized = getResizedBitmap(bmFront, (int) avWith,
                        (int) avHeight);
            } else {
                avatarResized = getResizedBitmap(bmFront, (int) (width * frontRatio),
                        (int) (width * frontRatio));
            }

            Bitmap bitmap = getCircleBitmap(avatarResized);
            b = overlay(bmBack, bitmap, width / frontOffset, width / frontOffset);
        }
        return b;
    }
}
