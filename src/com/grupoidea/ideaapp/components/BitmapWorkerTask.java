package com.grupoidea.ideaapp.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by fernando on 1/17/14.
 */
public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
    String TAG = this.getClass().getSimpleName();
    Context mContext;
    public String data;
    ImageView imageView;

    public BitmapWorkerTask(Context mContext, ImageView imageView) {
        this.mContext = mContext;
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        try {
            Bitmap bitmap; File appDir, file2;
            data = params[0];
            //Extraer nombre de archivo de URL
            String temp[] = params[0].split("/");
            String fileName = temp[temp.length-1];
            String state = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
                appDir = mContext.getExternalFilesDir("img/");
                file2 = new File(appDir, fileName);
//                Log.d(TAG, "Buscando imagen en SD " + file2.getAbsolutePath());
                bitmap = BitmapFactory.decodeFile(file2.getAbsolutePath());

                if(bitmap == null){
                    //Descarga imagen del server de IDEA
//                    Log.d(TAG, "Imagen no existe localmente. Descargando del servidor");
                    InputStream in = new java.net.URL(params[0]).openStream();

                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inPurgeable = true;
                    opt.inInputShareable = true;
                    opt.inPreferredConfig = Bitmap.Config.RGB_565;

                    bitmap = BitmapFactory.decodeStream(in, null, opt);
                    in.close();

                    //Guardar imagen en SD
                    if(Environment.MEDIA_MOUNTED.equals(state)){
                        appDir = mContext.getExternalFilesDir("img/");
                        File fileSave = new File(appDir,fileName);
                        if(!fileSave.exists())
                            if(fileSave.createNewFile()){
//                                Log.d(TAG, "Guardando imagen en "+ fileSave.getAbsolutePath());
                                FileOutputStream out = new FileOutputStream(fileSave);
                                Bitmap bitmapSave = Bitmap.createBitmap(bitmap);
                                bitmapSave.compress(Bitmap.CompressFormat.JPEG, 90, out);
                                out.flush();
                                out.close();
                            }
                    }else{
//                        Log.d(TAG, "No se pudo salvar la imagen "+fileName);
                        Toast.makeText(mContext, "No se pudo salvar la imagen " + fileName, Toast.LENGTH_SHORT).show();
                    }

                    return bitmap;
                }else{
                    //Imagen está en SD
                    return bitmap;
                }
            }else{
//                Log.d(TAG, "Tarjeta SD no disponible ");
                Toast.makeText(mContext, "Tarjeta SD no disponible ", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
//            Log.e("ImageDownload Exception: ", e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        if(imageView!= null){
            imageView.setImageBitmap(bitmap);
        }

//        if (productoWeakReference != null) {
//            Producto prod = productoWeakReference.get();
//            if (prod != null) {
//                Log.d(TAG,"Imagen de producto "+prod.getCodigo()+" obtenida");
//                prod.setImagen(bitmap);
//                if(catalogoAdapter != null){
//                    catalogoAdapter.notifyDataSetChanged();
//                }
//            }
//        }
    }
}