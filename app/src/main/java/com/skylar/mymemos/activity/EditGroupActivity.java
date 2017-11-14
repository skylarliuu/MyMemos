package com.skylar.mymemos.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.skylar.mymemos.R;
import com.skylar.mymemos.adapter.GroupItemAdapter;
import com.skylar.mymemos.application.MyApplication;
import com.skylar.mymemos.db.MemoDBManager;

import java.util.List;

/**
 * Created by Skylar on 2017/4/6.
 */

public class EditGroupActivity extends AppCompatActivity {

    private ListView mListView;
    private List<String> groupNameList;
    private GroupItemAdapter mAdapter;
    private MemoDBManager dbManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.group_manager));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupNameList = (List<String>)getIntent().getSerializableExtra("groupNameList");

        mListView = (ListView) findViewById(R.id.mListView);
        mAdapter = new GroupItemAdapter(this,groupNameList);
        mListView.setAdapter(mAdapter);

        dbManager = MyApplication.getDBManager();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.group_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_group) {
            addGroupDialog();
            return true;
        }else if(id == android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addGroupDialog() {
        //删除分组
        AlertDialog.Builder builder = new AlertDialog.Builder(
                this);
        builder.setTitle(getString(R.string.menu_add));
//                builder.setIcon(android.R.drawable.ic_dialog_alert);
        final EditText editText = new EditText(this);
        editText.setHint(getString(R.string.add_group_hint));
        builder.setView(editText);
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String addGroupName = editText.getText().toString().trim();
                        if(TextUtils.isEmpty(addGroupName)){
                            Toast.makeText(EditGroupActivity.this,R.string.group_name_not_null,Toast.LENGTH_SHORT).show();
                        }
                        dbManager.addGroup(addGroupName);
                        groupNameList.add(addGroupName);
                        mAdapter.notifyDataSetChanged();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }
}
