//package com.jxtools.wx.view.activity;
//
//import android.graphics.Bitmap;
//import android.graphics.pdf.PdfRenderer;
//import android.os.Bundle;
//import android.os.ParcelFileDescriptor;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.viewpager.widget.PagerAdapter;
//import androidx.viewpager.widget.ViewPager;
//
//import com.jxtools.wx.R;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
//public class PdfActivity extends AppCompatActivity {
//    private ViewPager vpPdf;
//
//    private LayoutInflater mInflater;
//    private ParcelFileDescriptor mDescriptor;
//    private PdfRenderer mRenderer;
//
//    public static final String FILE_NAME = "alibaba.pdf";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.a_pdf_reader);
//
//        mInflater = LayoutInflater.from(this);
//        vpPdf = findViewById(R.id.vp_pdf);
//
//        try {
//            openRender();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    private void openRender() throws IOException {
//
//        File file = new File(getExternalCacheDir(), FILE_NAME);
//        if (!file.exists()) {
//            //复制文件到本地存储
//            InputStream asset = getAssets().open(FILE_NAME);
//            FileOutputStream fos = new FileOutputStream(file);
//            byte[] buffer = new byte[1024];
//
//            int size;
//            while ((size = asset.read(buffer)) != -1) {
//                fos.write(buffer, 0, size);
//            }
//
//            asset.close();
//            fos.close();
//        }
//
//        //初始化PdfRender
//        mDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
//        if (mDescriptor != null) {
//            mRenderer = new PdfRenderer(mDescriptor);
//        }
//
//        //初始化ViewPager的适配器并绑定
//        MyAdapter adapter = new MyAdapter();
//        vpPdf.setAdapter(adapter);
//    }
//
//    @Override
//    protected void onDestroy() {
//        //销毁页面的时候释放资源,习惯
//        try {
//            closeRenderer();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        super.onDestroy();
//    }
//
//    private void closeRenderer() throws IOException {
//        mRenderer.close();
//        mDescriptor.close();
//
//    }
//
//    class MyAdapter extends PagerAdapter {
//
//        @Override
//        public int getCount() {
//            return mRenderer.getPageCount();
//        }
//
//        @Override
//        public boolean isViewFromObject(View view, Object object) {
//            return view == object;
//        }
//
//        @Override
//        public Object instantiateItem(ViewGroup container, int position) {
//            View view = mInflater.inflate(R.layout.item_pdf, null);
//
//            PhotoView pvPdf = view.findViewById(R.id.iv_pdf);
//            pvPdf.enable();
//
//            if (getCount() <= position) {
//                return view;
//            }
//
//            PdfRenderer.Page currentPage = mRenderer.openPage(position);
//            Bitmap bitmap = Bitmap.createBitmap(1080, 1760, Bitmap.Config
//                    .ARGB_8888);
//            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
//            pvPdf.setImageBitmap(bitmap);
//            //关闭当前Page对象
//            currentPage.close();
//
//            container.addView(view);
//            return view;
//        }
//
//        @Override
//        public void destroyItem(ViewGroup container, int position, Object object) {
//            //销毁需要销毁的视图
//            container.removeView((View) object);
//        }
//    }
//}
//package com.jxtools.wx.view.activity;
//
//import android.graphics.Bitmap;
//import android.graphics.pdf.PdfRenderer;
//import android.os.Bundle;
//import android.os.ParcelFileDescriptor;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.viewpager.widget.PagerAdapter;
//import androidx.viewpager.widget.ViewPager;
//
//import com.jxtools.wx.R;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
//public class PdfActivity extends AppCompatActivity {
//    private ViewPager vpPdf;
//
//    private LayoutInflater mInflater;
//    private ParcelFileDescriptor mDescriptor;
//    private PdfRenderer mRenderer;
//
//    public static final String FILE_NAME = "alibaba.pdf";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.a_pdf_reader);
//
//        mInflater = LayoutInflater.from(this);
//        vpPdf = findViewById(R.id.vp_pdf);
//
//        try {
//            openRender();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    private void openRender() throws IOException {
//
//        File file = new File(getExternalCacheDir(), FILE_NAME);
//        if (!file.exists()) {
//            //复制文件到本地存储
//            InputStream asset = getAssets().open(FILE_NAME);
//            FileOutputStream fos = new FileOutputStream(file);
//            byte[] buffer = new byte[1024];
//
//            int size;
//            while ((size = asset.read(buffer)) != -1) {
//                fos.write(buffer, 0, size);
//            }
//
//            asset.close();
//            fos.close();
//        }
//
//        //初始化PdfRender
//        mDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
//        if (mDescriptor != null) {
//            mRenderer = new PdfRenderer(mDescriptor);
//        }
//
//        //初始化ViewPager的适配器并绑定
//        MyAdapter adapter = new MyAdapter();
//        vpPdf.setAdapter(adapter);
//    }
//
//    @Override
//    protected void onDestroy() {
//        //销毁页面的时候释放资源,习惯
//        try {
//            closeRenderer();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        super.onDestroy();
//    }
//
//    private void closeRenderer() throws IOException {
//        mRenderer.close();
//        mDescriptor.close();
//
//    }
//
//    class MyAdapter extends PagerAdapter {
//
//        @Override
//        public int getCount() {
//            return mRenderer.getPageCount();
//        }
//
//        @Override
//        public boolean isViewFromObject(View view, Object object) {
//            return view == object;
//        }
//
//        @Override
//        public Object instantiateItem(ViewGroup container, int position) {
//            View view = mInflater.inflate(R.layout.item_pdf, null);
//
//            PhotoView pvPdf = view.findViewById(R.id.iv_pdf);
//            pvPdf.enable();
//
//            if (getCount() <= position) {
//                return view;
//            }
//
//            PdfRenderer.Page currentPage = mRenderer.openPage(position);
//            Bitmap bitmap = Bitmap.createBitmap(1080, 1760, Bitmap.Config
//                    .ARGB_8888);
//            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
//            pvPdf.setImageBitmap(bitmap);
//            //关闭当前Page对象
//            currentPage.close();
//
//            container.addView(view);
//            return view;
//        }
//
//        @Override
//        public void destroyItem(ViewGroup container, int position, Object object) {
//            //销毁需要销毁的视图
//            container.removeView((View) object);
//        }
//    }
//}
