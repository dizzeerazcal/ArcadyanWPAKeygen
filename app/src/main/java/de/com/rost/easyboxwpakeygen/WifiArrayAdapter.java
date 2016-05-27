package de.com.rost.easyboxwpakeygen;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Daniel on 14.08.2015.
 */
public class WifiArrayAdapter extends ArrayAdapter<ScanResult> {

    private Context context;
    private List<ScanResult> scanList;

    public WifiArrayAdapter(Context context, List<ScanResult> scanList) {

        // Call the super constructor to pass our XML ListItem and invoke getView([...]).
        super(context, R.layout.wifi_item_1, scanList);
        this.context = context;
        this.scanList = scanList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        // Get an Instance of the LayoutInflater Service...
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        /*
        if (convertView == null) {

            // Layoutdatei entfalten
            convertView = inflater.inflate(R.layout.wifi_item_1, parent,
                    false);

            // Holder erzeugen
            holder = new ViewHolder();
            holder.ssid = (TextView) convertView.findViewById(R.id.res_ssid);
            holder.bssid = (TextView) convertView.findViewById(R.id.res_bssid);
            holder.level = (TextView) convertView.findViewById(R.id.level);
            holder.level = (TextView) convertView.findViewById(R.id.colorIndicator);

            convertView.setTag(holder);
        } else {
            // Holder bereits vorhanden
            holder = (ViewHolder) convertView.getTag();
        }
        */

        // ... and use it to inflate the View from XML with the Parent View as root.
        View row = inflater.inflate(R.layout.wifi_item_1, parent, false);

        // Now we can assign the String variables to the TextViews
        TextView ssid = (TextView) row.findViewById(R.id.res_ssid);
        TextView bssid = (TextView) row.findViewById(R.id.res_bssid);
        TextView level = (TextView) row.findViewById(R.id.level);
        TextView colorIndicator = (TextView) row.findViewById(R.id.colorIndicator);

        ssid.setText(scanList.get(position).SSID);
        bssid.setText(context.getResources().getString(R.string.descr_mac)
                + " " + scanList.get(position).BSSID);
        level.setText("" + scanList.get(position).level + " dBm");

        if(ssid.getText().toString().contains("EasyBox"))
            // Resources are part of the Context !!!!
            colorIndicator.setBackgroundColor(context.getResources().getColor(R.color.blue));
        // And finally return the view with all that stuff
        return row;
    }

    static class ViewHolder {
        TextView ssid, bssid, level, colorIndicator;
    }
}
