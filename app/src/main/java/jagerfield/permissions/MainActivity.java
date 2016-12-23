package jagerfield.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import jagerfield.library.AppUtilities;
import jagerfield.library.PermissionsUtil.IPermissionUtil;
import jagerfield.utilities.R;
import jagerfield.library.NetworkUtil.NetworkUtil;
import jagerfield.library.PermissionsUtil.PermissionsUtil;
import jagerfield.library.PermissionsUtil.Results.ICheckPermissionResult;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private TextView tv_getting_permission;
    private TextView tv_requestPermissions;
    private TextView tv_requestNeverAskAgainPermissions;
    private TextView tv_neverAskAgainPermissionsList;
    private TextView tv_deniedPermissionsList;
    private Button bt_getDeniedPermissions;
    private Button bt_openSettings;
    private Button bt_checkPermissions;
    private IPermissionUtil permissionsUtil;

    public String PERMISSIONS_MISSING = "Permissions are missing";
    public String PERMISSIONS_GRANTED = "All Permission Available";

    public final String[] PERMISSIONS_ARRAY = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initiateAppViews();
    }

    private void initiateAppViews()
    {
        tv_getting_permission = (TextView) findViewById(R.id.tv_getting_permission);
        tv_requestPermissions = (TextView) findViewById(R.id.tv_requestPermissions);
        tv_requestNeverAskAgainPermissions = (TextView) findViewById(R.id.tv_requestNeverAskAgainPermissions);
        tv_neverAskAgainPermissionsList = (TextView) findViewById(R.id.tv_neverAskAgainPermissionsList);
        tv_deniedPermissionsList = (TextView) findViewById(R.id.tv_deniedPermissionsList);
        bt_getDeniedPermissions = (Button) findViewById(R.id.bt_getDeniedPermissions);
        bt_openSettings = (Button) findViewById(R.id.bt_openSettings);
        bt_checkPermissions = (Button) findViewById(R.id.bt_checkPermissions);

        bt_getDeniedPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                getPermissions();

            }
        });

        bt_openSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                openSettings(MainActivity.this);
            }
        });

        bt_checkPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                getPermissions();
            }
        });
    }

    private void getPermissions()
    {
        permissionsUtil = AppUtilities.getPermissionUtil(this);

        ICheckPermissionResult result = permissionsUtil.checkPermissionsResults(PERMISSIONS_ARRAY);

        if (result.arePermissionsGranted())
        {
            setUserInterface(result);
        }
        else
        {
            permissionsUtil.makePermissionsRequests(PERMISSIONS_ARRAY);
        }

        NetworkUtil network = new NetworkUtil();

        int i = network.getInternetStatus(this);

        String str = "";
    }

    private void setUserInterface(ICheckPermissionResult permissionsResults)
    {
        if (permissionsResults ==null)
        {
            return;
        }

        /**
         *  All permissions granted
         */
        managePermissionStatusTitle(permissionsResults);

        /**
         * If not all permissions granted, then check the permissions user had denied
         */
        manageDeniedPermissionsViews(permissionsResults);

        /**
         * If not all permissions granted,check never show again permissions
         */
        manageNeverAskPermissionsViews(permissionsResults);
    }

    private void managePermissionStatusTitle(ICheckPermissionResult permissionsResults)
    {
        String msg = "";
        boolean gotPermissions = permissionsResults.arePermissionsGranted();

        if (gotPermissions && permissionsResults.getNeverAskAgainPermissionsList().isEmpty() && permissionsResults.getUserDeniedPermissionsList().isEmpty())
        {
            msg = PERMISSIONS_GRANTED;
            tv_getting_permission.setText(msg);
            tv_getting_permission.setTextColor(getThisColor(R.color.green));
            bt_checkPermissions.setVisibility(View.VISIBLE);
        }
        else if (!gotPermissions && !permissionsResults.getNeverAskAgainPermissionsList().isEmpty() && permissionsResults.getUserDeniedPermissionsList().isEmpty())
        {
            msg = PERMISSIONS_MISSING;
            tv_getting_permission.setText(msg);
            tv_getting_permission.setTextColor(getThisColor(R.color.red));
            bt_checkPermissions.setVisibility(View.VISIBLE);
        }
        else if (!gotPermissions && !permissionsResults.getUserDeniedPermissionsList().isEmpty())
        {
            msg = PERMISSIONS_MISSING;
            tv_getting_permission.setText(msg);
            tv_getting_permission.setTextColor(getThisColor(R.color.red));
            bt_checkPermissions.setVisibility(View.GONE);
        }
    }

    private int getThisColor(int color)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            return getColor(color);
        }
        else
        {
            return getResources().getColor(color);
        }
    }

    private void manageDeniedPermissionsViews(ICheckPermissionResult results)
    {
        int status = View.GONE;
        boolean gotPermissions = results.arePermissionsGranted();
        ArrayList<String> list = results.getUserDeniedPermissionsList();

        if (!results.arePermissionsGranted() && results.getUserDeniedPermissionsList().isEmpty())
        {
            status = View.GONE;
        }
        else if (!gotPermissions && !results.getUserDeniedPermissionsList().isEmpty())
        {
            status = View.VISIBLE;
        }

        tv_requestPermissions.setVisibility(status);
        String text = getList(list, status);
        tv_deniedPermissionsList.setText(text);
        tv_deniedPermissionsList.setVisibility(status);
        bt_getDeniedPermissions.setVisibility(status);
    }

    private void manageNeverAskPermissionsViews(ICheckPermissionResult results)
    {
        int status = View.GONE;
        boolean gotPermissions = results.arePermissionsGranted();
        ArrayList<String> list = results.getNeverAskAgainPermissionsList();

        if (!gotPermissions && results.getNeverAskAgainPermissionsList().isEmpty())
        {
            status = View.GONE;
        }
        else if (!gotPermissions && !results.getNeverAskAgainPermissionsList().isEmpty())
        {
            status = View.VISIBLE;
        }

        tv_requestNeverAskAgainPermissions.setVisibility(status);
        String text = getList(list, status);
        tv_neverAskAgainPermissionsList.setText(text);
        tv_neverAskAgainPermissionsList.setVisibility(status);
        bt_openSettings.setVisibility(status);
    }

    @NonNull
    private String getList(ArrayList<String> list, int status)
    {
        StringBuilder items = new StringBuilder();

        if (status == View.VISIBLE)
        {
            for (int i = 0; i < list.size(); i++)
            {
                String permission = list.get(i);
                int v = permission.lastIndexOf(".");
                String name = permission.substring(v+1, permission.length()).toLowerCase();
                char c = Character.toUpperCase(name.charAt(0));
                name = name.replaceFirst(Character.toString(name.charAt(0)), Character.toString(c));
                items.append(i +1 + " - " + name + "\n");
            }
        }

        return items.toString().replace("_", " ").trim();
    }

    public static void openSettings(final Activity context) {
        if (context == null) {
            return;
        }

        Uri uri = Uri.parse("package:" + context.getPackageName());
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
        context.startActivity(intent);


//        final Intent i = new Intent();
//        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//        i.addCategory(Intent.CATEGORY_DEFAULT);
//        i.setData(Uri.parse("package:" + context.getPackageName()));
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//        context.startActivity(i);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        if (requestCode == permissionsUtil.getPermissionsReqFlag())
        {
            ICheckPermissionResult result = null;
            result= permissionsUtil.checkPermissionsResults(PERMISSIONS_ARRAY);
            if (result == null)
            {
                return;
            }

            setUserInterface(result);

            String str = "";
        }
    }
}




















