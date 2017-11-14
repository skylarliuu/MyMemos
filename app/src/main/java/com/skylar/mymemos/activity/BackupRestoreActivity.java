package com.skylar.mymemos.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.skylar.mymemos.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by Skylar on 2017/4/20.
 */

public class BackupRestoreActivity extends AppCompatActivity {

    private Button backup;
    private ListView listView;

    private ArrayList<String> fileList = new ArrayList<String>(); // 数据库文件列表
    private AlertDialog dialog = null;
    private String BACK_FOLDER = "backup";
    private String appName = "MyMemos";
    private String folder_date ;//备份时新建的文件名
    private BackupAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_restore);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.backup_restore));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        backup = (Button) findViewById(R.id.backup);
        listView = (ListView) findViewById(R.id.listView);

        fileList = getFileList();
        if(fileList != null){
            adapter = new BackupAdapter(this,fileList);
            listView.setAdapter(adapter);
        }

        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backupDB();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class BackupAdapter extends BaseAdapter {

        private Context mContext;
        private ArrayList<String> fileList;
        private LayoutInflater mInflater;

        public BackupAdapter(Context context,ArrayList<String> fileList){
            mInflater = LayoutInflater.from(context);
            mContext = context;
            this.fileList = fileList;
        }

        @Override
        public int getCount() {
            return fileList.size();
        }

        @Override
        public String getItem(int position) {
            return fileList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null)
            {
                convertView = mInflater.inflate(R.layout.backup_list_item, parent,
                        false);
                holder =  new ViewHolder();
                holder.fileName = (TextView) convertView.findViewById(R.id.fileName);
                holder.deleteView = (LinearLayout) convertView.findViewById(R.id.deleteView);
                holder.restoreView = (LinearLayout) convertView.findViewById(R.id.restoreView);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }


            holder.fileName.setText(getItem(position));

            //恢复数据
            holder.restoreView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    restoreDB(position);
                }
            });

            //删除备份
            holder.deleteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteDB(position);
                }
            });

            return convertView;
        }
    }

    static class ViewHolder{
        TextView fileName;
        LinearLayout restoreView;
        LinearLayout deleteView;
    }


       public static boolean delete(File file) {
                if (file.isFile()) {
                       file.delete();
                       return true;
                   }

              if(file.isDirectory()){
                       File[] childFiles = file.listFiles();
                      if (childFiles == null || childFiles.length == 0) {
                               file.delete();
                              return true;
                           }

                        for (int i = 0; i < childFiles.length; i++) {
                           delete(childFiles[i]);
                           }
                       file.delete();
                       return true;
                   }
              return false;
         }

    public void deleteDB(final int position){
        new AlertDialog.Builder(this).setTitle(getString(R.string.menu_delete))
                .setMessage(getString(R.string.delete_db))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogI, int which) {
                        // 恢复数据库
                        String sp = File.separator;
                        String deleteFilePath = Environment.getExternalStorageDirectory() + sp
                                + appName + sp + BACK_FOLDER +sp + fileList.get(position);
                        File file = new File(deleteFilePath);
                        if(!file.exists()){
                            Toast.makeText(BackupRestoreActivity.this,R.string.file_not_exist,Toast.LENGTH_SHORT).show();
                            return ;
                        }
                        boolean success = delete(file);
                        if(success){
                            Toast.makeText(BackupRestoreActivity.this,R.string.delete_success,Toast.LENGTH_SHORT).show();
                            fileList.remove(position);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }).setNegativeButton(android.R.string.cancel, null)
                .show();
    }


    public void restoreDB(final int position){
        new AlertDialog.Builder(this).setTitle(getString(R.string.restore))
                .setMessage(getString(R.string.restore_db))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogI, int which) {
                        // 恢复数据库
                        String sp = File.separator;
                        String folderName = fileList.get(position);
                        String backUpPath = Environment.getExternalStorageDirectory()
                                + sp + appName + sp + BACK_FOLDER + sp + folderName;
                        File file = new File(backUpPath);
                        if (file.isDirectory()) {
                            File[] files = file.listFiles();
                            boolean isOk = false;
                            for (int i = 0; i < files.length; i++) {
                                File f = files[i];
                                isOk = restore(f.getName(), f);
                                if (!isOk) {
                                    String fail_msg = getString(R.string.restore_fail) + ":" + f.getName();
                                    Toast.makeText(BackupRestoreActivity.this, fail_msg,
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            if (isOk) {
                                // 如果有数据体现则需要刷新出新的数据
                                Toast.makeText(BackupRestoreActivity.this,getString(R.string.restore_success),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).setNegativeButton(android.R.string.cancel, null)
                .show();
    }



    /**
     * 备份数据库
     */
    public void backupDB() {
        new AlertDialog.Builder(this).setTitle(getString(R.string.backup))
                .setMessage(getString(R.string.backup_db))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogI, int which) {
                        // 备份数据库
                        if (dialog == null) {
                            dialog = awaitDialog(BackupRestoreActivity.this);
                        } else {
                            dialog.show();
                        }
                        new ExecuteTask().execute('B');
                    }
                }).setNegativeButton(android.R.string.cancel, null)
                .show();
    }



        /**
        * 备份操作
        */
        private boolean backUp() {
          boolean isOk = false;
          String sp= File.separator;
          File sdFile = sdCardOk();//检测备份的文件夹是否存在
          if (sdFile != null) {
            try {
              String dbName = "Memo.db";//备份的数据库名称
              folder_date = datePrefix();// 创建日期文件夹
              File f = new File(sdFile.getAbsolutePath() + sp + folder_date);
              if (!f.exists()) {
                 f.mkdirs();
              }
              File dbFile = dbOk(dbName);//检测数据库文件是否存在
              if (dbFile != null) {
                 File backFile = new File(f.getAbsolutePath() + sp
                                    + dbFile.getName());
                 backFile.createNewFile();
                 //复制数据库
                 isOk = fileCopy(backFile, dbFile.getAbsoluteFile());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
          }
          return isOk;
        }

    /**
     * 时间前缀
     *
     * @return
     */
    private String datePrefix() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String str = format.format(date);
        return str;
    }

    /**
     * 文件夹列表
     *
     * @return
     */
    private ArrayList<String> getFileList() {
        ArrayList<String> fileList = new ArrayList<String>();
        File file = sdCardOk();
        if (file != null) {
            File[] list = file.listFiles();
            List<File> files = Arrays.asList(list);
            if (files.size() == 0)
                return null;
            Collections.sort(files, new Comparator< File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (o1.isDirectory() && o2.isFile())
                        return -1;
                    if (o1.isFile() && o2.isDirectory())
                        return 1;
                    return o1.getName().compareTo(o2.getName());
                }
            });

            for (int i = files.size() - 1; i >= 0; i--) {
                File temp = files.get(i);
                String fileName = temp.getName();
                fileList.add(fileName);
            }
        }
        return fileList;
    }


    /**
     * sdCard是否存在 备份的文件夹是否存在
     *
     * @return null不能使用
     */
    private File sdCardOk() {
        File bkFile = null;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            String sp = File.separator;
            String backUpPath = Environment.getExternalStorageDirectory() + sp
                    + appName + sp + BACK_FOLDER;
            bkFile = new File(backUpPath);
            if (!bkFile.exists()) {
                bkFile.mkdirs();
            } else
                return bkFile;
        } else
            Toast.makeText(this, "SdCard 不存在", Toast.LENGTH_SHORT).show();
        return bkFile;
    }

    /**
     * 恢复数据库
     *
     * @param name
     *            选择的文件名称 选中的数据库名称
     * @param  f
     *            需要恢复的数据库名称
     * @return
     */
    public boolean restore(String name, File f) {
        boolean isOk = false;
        if (f != null) {
            File dbFile = dbOk(name);
            try {
                if (dbFile != null) {
                    isOk = fileCopy(dbFile, f.getAbsoluteFile());
                } else
                    isOk = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isOk;
    }

    /**
     * 数据库文件是否存在，并可以使用
     *
     * @return
     */
    private File dbOk(String dbName) {
        String sp = File.separator;
        String absPath = Environment.getDataDirectory().getAbsolutePath();
        String pakName = getPackageName();
        String dbPath = absPath + sp + "data" + sp + pakName + sp + "databases"
                + sp + dbName;
        File file = new File(dbPath);
        if (file.exists()) {
            return file;
        } else {
            return null;
        }
    }

    /**
     * 等候动画
     */
    public AlertDialog awaitDialog(Context context) {
        ProgressBar bar = new ProgressBar(context);
        bar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setCancelable(false);
        dialog.show();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = 50;
        params.height = 50;
        window.setAttributes(params);
        window.setContentView(bar);
        return dialog;
    }

    /**
     *
     * @param outFile
     *            写入
     * @param inFile
     *            读取
     * @throws FileNotFoundException
     */
    private boolean fileCopy(File outFile, File inFile) throws IOException {
        if (outFile == null || inFile == null) {
            return false;
        }
        boolean isOk = true;
        FileChannel inChannel = new FileInputStream(inFile).getChannel();// 只读
        FileChannel outChannel = new FileOutputStream(outFile).getChannel();// 只写
        try {
            long size = inChannel.transferTo(0, inChannel.size(), outChannel);
            if (size <= 0) {
                isOk = false;
            }
        } catch (IOException e) {
            isOk = false;
            e.printStackTrace();
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
        return isOk;
    }


    private class ExecuteTask extends AsyncTask<Character, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Character... params) {
            char c = params[0];
            boolean success = false;
            if (c == 'B') {
                success = backUp();
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (dialog != null) {
                dialog.dismiss();
            }

            if(result){
                Toast.makeText(BackupRestoreActivity.this,getString(R.string.backup_success),Toast.LENGTH_SHORT).show();
//                adapter = new BackupAdapter(BackupRestoreActivity.this,fileList);
//                listView.setAdapter(adapter) ;

                fileList.add(0,folder_date);
                if(adapter == null){//第一次备份
                    adapter = new BackupAdapter(BackupRestoreActivity.this,fileList);
                    listView.setAdapter(adapter);
                }else{
                    adapter.notifyDataSetChanged();
                }
            }else{
                Toast.makeText(BackupRestoreActivity.this,getString(R.string.backup_fail),Toast.LENGTH_SHORT).show();
            }
        }
    }

}
