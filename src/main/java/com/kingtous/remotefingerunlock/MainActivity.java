package com.kingtous.remotefingerunlock;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import com.kingtous.remotefingerunlock.ToolFragment.DataManagement;
import com.kingtous.remotefingerunlock.ToolFragment.Scan;
import com.kingtous.remotefingerunlock.ToolFragment.Settings;
import com.kingtous.remotefingerunlock.ToolFragment.Unlock;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import pub.devrel.easypermissions.EasyPermissions;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,EasyPermissions.PermissionCallbacks {

    Toolbar toolbar;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;


    FragmentManager fragmentManager;

    Fragment unlock,scan,settings,dataManagement;
    Fragment currentFragment;
    //request code
    int FINGER_REQUEST_CODE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        checkPermission();

        //fragment
        fragmentManager=getSupportFragmentManager();
        unlock=new Unlock();
        scan=new Scan();
        settings=new Settings();
        dataManagement=new DataManagement();
        switchFragment(unlock).commit();

    }


    private void checkPermission(){
        if (!EasyPermissions.hasPermissions(this, Manifest.permission.USE_FINGERPRINT)){
            String[] permissions=new String[]{Manifest.permission.USE_FINGERPRINT,Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN};
            EasyPermissions.requestPermissions(this,"需要申请指纹权限",FINGER_REQUEST_CODE,permissions);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @androidx.annotation.NonNull List<String> perms) {
        return;
    }

    @Override
    public void onPermissionsDenied(int requestCode, @androidx.annotation.NonNull List<String> perms) {
        if (requestCode==FINGER_REQUEST_CODE){
            new AlertDialog.Builder(this)
                    .setTitle("权限获取")
                    .setMessage("权限获取失败，请允许指纹权限")
                    .setPositiveButton("好", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();

        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.Unlock) {
            switchFragment(unlock).commit();
        } else if (id == R.id.Scan) {
            switchFragment(scan).addToBackStack(null)
                    .commit();
        } else if (id == R.id.DataManagement) {
            switchFragment(dataManagement).addToBackStack(null)
                    .commit();
        } else if (id == R.id.Settings) {
            switchFragment(settings).addToBackStack(null)
                    .commit();
        } else if (id == R.id.Share) {

        } else if (id == R.id.About) {
            TextView view=new TextView(this);
            SpannableString s=new SpannableString("  远程解锁电脑\n  作者:Kingtous\n  项目地址:\n  ");
            Linkify.addLinks(s,Linkify.WEB_URLS);
            view.setText(s);
            view.setMovementMethod(LinkMovementMethod.getInstance());

            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this).setTitle("关于")
                    .setView(view)
                    .setPositiveButton("好", null)
                    .show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private FragmentTransaction switchFragment(Fragment targetFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (!targetFragment.isAdded()) {
         //第一次使用switchFragment()时currentFragment为null，所以要判断一下        
            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }
            transaction.add(R.id.fragmentShow,targetFragment,targetFragment.getClass().getName());
        } else {
            transaction.hide(currentFragment).show(targetFragment);
        }
        currentFragment = targetFragment;
        return transaction;
    }

}
