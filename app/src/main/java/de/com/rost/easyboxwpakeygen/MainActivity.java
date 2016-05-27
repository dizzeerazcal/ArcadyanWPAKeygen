package de.com.rost.easyboxwpakeygen;
/**
 * Arcadyan WPA Key Generator *
 * by Daniel Rost *
 **/

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.List;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    ListView wifiListView;
    WifiManager wifi;
    String[] wifis;
    WifiScanReciever wifiReciever;
    List<ScanResult> wifiScanList;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiListView = (ListView) findViewById(R.id.wifiList);
        wifiListView.setOnItemClickListener(this);
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReciever();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    protected void onPause() {
        unregisterReceiver(wifiReciever);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        if (!wifi.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "WiFi is disabled. Enabling it.", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }
        scanWifi(new View(getApplicationContext()));
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String ssid = wifiScanList.get(position).SSID;
        String bssid = wifiScanList.get(position).BSSID;

        if (ssid.contains("EasyBox")) {
            WifiConfiguration config = new WifiConfiguration();
            config.SSID = "\"" + ssid + "\"";
            config.preSharedKey = "\"" + calculateResult(bssid) + "\"";
            wifi.addNetwork(config);

            List<WifiConfiguration> list = wifi.getConfiguredNetworks();
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
                    wifi.disconnect();
                    wifi.enableNetwork(i.networkId, true);

                    wifi.reconnect();
                    break;
                }
            }

            if (wifi.getConnectionInfo().getSSID() == ssid) {
                Toast.makeText(getApplicationContext(), "Connection successful", Toast.LENGTH_LONG).show();
            } else {

                Toast.makeText(getApplicationContext(),
                        "Connecting to Network " + ssid + "\n" + "with WPA Key " + calculateResult(bssid),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Incompatible AP", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    public String calculateResult(String mac) {

        // Reformat MAC String
        mac = String.format("%12s",
                mac.replaceAll("[^0-9a-fA-F]", ""))
                .replaceAll(" ", "0").toUpperCase();

        Log.println(Log.DEBUG, "MAC", mac);

        // Generate NIC ID
        String nicId = String.valueOf(mac.charAt(6)) +
                String.valueOf(mac.charAt(7)) +
                String.valueOf(mac.charAt(8)) +
                String.valueOf(mac.charAt(9)) +
                String.valueOf(mac.charAt(10)) +
                String.valueOf(mac.charAt(11));
        nicId = nicId.toUpperCase();

        // Generate decimal SMAC String
        String smacHex = String.format("%s%s%s%s",
                String.valueOf(mac.charAt(8)),
                String.valueOf(mac.charAt(9)),
                String.valueOf(mac.charAt(10)),
                String.valueOf(mac.charAt(11))
        );
        String smac = String.format("%10s", String.valueOf(Integer.valueOf(smacHex, 16))).replaceAll(" ", "0");
        smac = smac.toUpperCase();

        // Generate Assistance Variables
        String k1 = Integer.toHexString(
                Integer.valueOf(String.valueOf(smac.charAt(6)), 16) +
                        Integer.valueOf(String.valueOf(smac.charAt(7)), 16) +
                        Integer.valueOf(String.valueOf(mac.charAt(10)), 16) +
                        Integer.valueOf(String.valueOf(mac.charAt(11)), 16)
        );
        String k2 = Integer.toHexString(
                Integer.valueOf(String.valueOf(mac.charAt(8)), 16) +
                        Integer.valueOf(String.valueOf(mac.charAt(9)), 16) +
                        Integer.valueOf(String.valueOf(smac.charAt(8)), 16) +
                        Integer.valueOf(String.valueOf(smac.charAt(9)), 16)
        );
        int k1Dec = Integer.valueOf(String.valueOf(k1.charAt(k1.length() - 1)), 16);
        int k2Dec = Integer.valueOf(String.valueOf(k2.charAt(k2.length() - 1)), 16);

        int x1 = k1Dec ^ Integer.valueOf(String.valueOf(smac.charAt(smac.length() - 1)), 16);
        int x2 = k1Dec ^ Integer.valueOf(String.valueOf(smac.charAt(smac.length() - 2)), 16);
        int x3 = k1Dec ^ Integer.valueOf(String.valueOf(smac.charAt(smac.length() - 3)), 16);

        int y1 = k2Dec ^ Integer.valueOf(String.valueOf(mac.charAt(9)), 16);
        int y2 = k2Dec ^ Integer.valueOf(String.valueOf(mac.charAt(10)), 16);
        int y3 = k2Dec ^ Integer.valueOf(String.valueOf(mac.charAt(11)), 16);

        int z1 = Integer.valueOf(String.valueOf(mac.charAt(10)), 16) ^
                Integer.valueOf(String.valueOf(smac.charAt(smac.length() - 1)), 16);
        int z2 = Integer.valueOf(String.valueOf(mac.charAt(11)), 16) ^
                Integer.valueOf(String.valueOf(smac.charAt(smac.length() - 2)), 16);
        int z3 = (k1Dec ^ k2Dec);

        //Log.println(Log.DEBUG, "", "Assistance:\nx1: " + x1 + " x2: " + x2 + " x3: " + x3 +
        // "\ny1: " + y1 + " y2: " + y2 + " y3: " + y3 +
        // "\nz1: " + z1 + " z2: " + z2 + " z3: " + z3);

        // Generate SSID
        String ssid = "EasyBox-" + mac.charAt(6) +
                mac.charAt(7) +
                mac.charAt(8) +
                mac.charAt(9) +
                smac.charAt(5) +
                smac.charAt(9);

        //Log.println(Log.DEBUG, "SSID", ssid);

        // Generate WPA Key
        String wpaKey = String.format("%s%s%s%s%s%s%s%s%s",
                Integer.toHexString(x1),
                Integer.toHexString(y1),
                Integer.toHexString(z1),
                Integer.toHexString(x2),
                Integer.toHexString(y2),
                Integer.toHexString(z2),
                Integer.toHexString(x3),
                Integer.toHexString(y3),
                Integer.toHexString(z3));
        wpaKey = wpaKey.toUpperCase();
        Log.println(Log.DEBUG, "WPA Key", "" + wpaKey);
        Resources res = getResources();

        String result = String.format("SSID:                 %s%n%nWPA-PSK:        %s%n", ssid, wpaKey);
        return wpaKey;
    }

    public void scanWifi(View view) {
        Toast.makeText(getApplicationContext(), getResources().
                getText(R.string.info_scanning), Toast.LENGTH_SHORT).show();
        wifi.startScan();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://de.com.rost.easyboxwpakeygen/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://de.com.rost.easyboxwpakeygen/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class WifiScanReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            wifiScanList = wifi.getScanResults();
            wifiListView.setAdapter(new WifiArrayAdapter(getApplicationContext(), wifiScanList));
        }
    }

}