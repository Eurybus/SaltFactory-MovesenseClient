package com.junction2018.eurybus.movesenseclient;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsConnectionListener;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.MdsSubscription;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.scan.ScanSettings;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;

public class DeviceConnect extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private final static String LOG_TAG = "DeviceConnect";
    static private RxBleClient mBleClient;

    private Mds mMds;
    private Subscription mScanSubscription;

    static private String URI_MEAS_ACC_13 = "/Meas/Acc/13";
    static private String URI_MEAS_ECG_125 = "/Meas/ECG/125";
    static private String URI_MEAS_HR = "/Meas/HR";
    static private String URI_SYSTEM_STATES_MOVEMENT = "/System/States/0";
    static private String URI_SYSTEM_STATES_CONNECTED = "/System/States/2";
    static private String URI_MEAS_GYRO_INFO = "/Meas/Gyro/Info";
    static private String URI_MEAS_GYRO_CONFIG = "/Meas/Gyro/Config";
    static private String URI_MEAS_GYRO_GET_13 = "/Meas/Gyro/13";


    private DogImageUpdater dogImageUpdater;

    private MdsSubscription mdsSubscription;
    private String subscribedDeviceSerial;

    private MyScanResult connectedDevice;

    // UI
    private ListView mScanResultListView;
    private ArrayList<MyScanResult> mScanResArrayList = new ArrayList<>();
    ArrayAdapter<MyScanResult> mScanResArrayAdapter;

    public static final String URI_CONNECTEDDEVICES = "suunto://MDS/ConnectedDevices";
    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";
    public static final String SCHEME_PREFIX = "suunto://";

    public List<MathUtils.Vector> accell_log;

    public static MqttHelper mqttHelper;

    private void startMqtt(){
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("MQTT", "Connected to Mqtt: " + s);
            }

            @Override
            public void connectionLost(Throwable throwable) {
                Log.e("MQTT", "Lost connection to " + mqttHelper.serverUri);
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
//                Log.w("MQTT",mqttMessage.toString());
                DogProtocolPayloadObject doggoPayload = new Gson().fromJson(mqttMessage.toString(), DogProtocolPayloadObject.class);
                dogImageUpdater.ChangeState(doggoPayload.picture_id);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    private void updateAccelLog(MathUtils.Vector input) {
        if (this.accell_log == null)
                this.accell_log = new ArrayList<>();
        this.accell_log.add(input);
    }
    private void initMds(Context context){
        mMds = Mds.builder().build(context);
    }

    private RxBleClient getBleClient() {
        // Init RxAndroidBle (Ble helper library) if not yet initialized
        if (mBleClient == null)
        {
            mBleClient = RxBleClient.create(this);
        }

        return mBleClient;
    }

    protected void RelayNotification(String message) {
        mqttHelper.PublishMessage(message);
        Log.d(LOG_TAG, "Sent message to mqtt");
    }
    private void ShowConnectedDeviceInfo(MyScanResult device) {
        FrameLayout frameLayout = findViewById(R.id.connectedDeviceInfo);
        TextView deviceNameText = findViewById(R.id.connectedDeviceMaker);
        TextView helpText = findViewById(R.id.helpText);
        deviceNameText.setText( device.name);
        helpText.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);

    }

    private void HideConnectedDeviceInfo() {
        FrameLayout frameLayout = findViewById(R.id.connectedDeviceInfo);
        frameLayout.setVisibility(View.GONE);
        TextView helpText = findViewById(R.id.helpText);
        helpText.setVisibility(View.VISIBLE);
    }

    private void connectBLEDevice(MyScanResult device) {
        RxBleDevice bleDevice = getBleClient().getBleDevice(device.macAddress);

        Log.i(LOG_TAG, "Connecting to BLE device: " + bleDevice.getMacAddress());
        mMds.connect(bleDevice.getMacAddress(), new MdsConnectionListener() {

            @Override
            public void onConnect(String s) {
                Log.d(LOG_TAG, "onConnect:" + s);
            }

            @Override
            public void onConnectionComplete(String macAddress, String serial) {
                for (MyScanResult sr : mScanResArrayList) {
                    if (sr.macAddress.equalsIgnoreCase(macAddress)) {
                        sr.markConnected(serial);
                        ShowConnectedDeviceInfo(sr);
                        connectedDevice = sr;
                        findViewById(R.id.listScanResult).setVisibility(View.GONE);
                        subscribeToSensor(device.connectedSerial);
                        break;
                    }
                }
                mScanResArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(MdsException e) {
                Log.e(LOG_TAG, "onError:" + e);

                showConnectionError(e);
            }

            @Override
            public void onDisconnect(String bleAddress) {

                Log.d(LOG_TAG, "onDisconnect: " + bleAddress);
                for (MyScanResult sr : mScanResArrayList) {
                    if (bleAddress.equals(sr.macAddress))
                    {
                        // unsubscribe if was subscribed
                        if (sr.connectedSerial != null && sr.connectedSerial.equals(subscribedDeviceSerial))
                            unsubscribe();

                        sr.markDisconnected();
                        HideConnectedDeviceInfo();
                        findViewById(R.id.listScanResult).setVisibility(View.VISIBLE);
                    }
                }
                mScanResArrayAdapter.notifyDataSetChanged();
            }
        });
    }

    private void subscribeToSensor(String connectedSerial) {
        // Clean up existing subscription (if there is one)
        if (mdsSubscription != null) {
            unsubscribe();
        }

        // Build JSON doc that describes what resource and device to subscribe
        // Here we subscribe to 13 hertz accelerometer data
        StringBuilder sb = new StringBuilder();
        String strContract = sb.append("{\"Uri\": \"").append(connectedSerial).append(URI_MEAS_GYRO_GET_13).append("\"}").toString();
        Log.d(LOG_TAG, strContract);
        final View sensorUI = findViewById(R.id.sensorUI_text);

        subscribedDeviceSerial = connectedSerial;

        mdsSubscription = mMds.builder().build(this).subscribe(URI_EVENTLISTENER,
                strContract, new MdsNotificationListener() {
                    long startTime = 0;
                    @Override
                    public void onNotification(String data) {
                        Log.d(LOG_TAG, "onNotification(): " + data);

                        // If UI not enabled, do it now
                        if (sensorUI.getVisibility() == View.GONE)
                            sensorUI.setVisibility(View.VISIBLE);

                        GyroDataResponse gyroResponse = new Gson().fromJson(data, GyroDataResponse.class);
                        if (gyroResponse != null && gyroResponse.body.array.length > 0) {
                            String gyroStr = String.format("%.02f, %.02f, %.02f",
                                    gyroResponse.body.array[0].x, gyroResponse.body.array[0].y, gyroResponse.body.array[0].z);
                            ((TextView) findViewById(R.id.sensorUI_text)).setText(gyroStr);
                            updateAccelLog(new MathUtils.Vector(
                                    (float)gyroResponse.body.array[0].x,
                                    (float)gyroResponse.body.array[0].y,
                                    (float)gyroResponse.body.array[0].z
                            ));
                            if (startTime == 0)
                                startTime = gyroResponse.body.timestamp;

                            if (gyroResponse.body.timestamp - startTime > 1000) {
                                startTime = gyroResponse.body.timestamp;
                                sendAccelLogs(gyroResponse.body.timestamp);
                            }
                        }
                    }

                    @Override
                    public void onError(MdsException error) {
                        Log.e(LOG_TAG, "subscription onError(): ", error);
                        unsubscribe();
                    }
                });

    }

    private void sendAccelLogs(Long timestamp) {
        MathUtils.Vector result = MathUtils.CalculateTotalAcceleration(this.accell_log);
        this.accell_log = new ArrayList<>();
        Log.i(LOG_TAG, "Calculated vector: " + result.toString() + " from " + this.accell_log.size() + " elements");
        MessageQueuePayload payload = new MessageQueuePayload(result, timestamp);
        RelayNotification(payload.toJSON());
    }

    private void unsubscribe() {
        if (mdsSubscription != null) {
            mdsSubscription.unsubscribe();
            mdsSubscription = null;
        }


        subscribedDeviceSerial = null;

        // If UI not invisible, do it now
        final View sensorUI = findViewById(R.id.sensorUI);
        if (sensorUI.getVisibility() != View.GONE)
            sensorUI.setVisibility(View.GONE);

    }

    private void showConnectionError(MdsException e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Connection Error:")
                .setMessage(e.getMessage());

        builder.create().show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_connect);
        initMds(this);

        mScanResultListView = findViewById(R.id.listScanResult);
        mScanResArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, mScanResArrayList);
        mScanResultListView.setAdapter(mScanResArrayAdapter);
        mScanResultListView.setOnItemLongClickListener(this);
        mScanResultListView.setOnItemClickListener(this);
        onScanClicked(findViewById(R.id.buttonScan));

        dogImageUpdater = new DogImageUpdater(findViewById(R.id.dogImageView), this);
        startMqtt();

    }

    public void onScanClicked(View view) {
        findViewById(R.id.buttonScan).setVisibility(View.GONE);
        findViewById(R.id.buttonScanStop).setVisibility(View.VISIBLE);

        // Start with empty list
        mScanResArrayList.clear();
        mScanResArrayAdapter.notifyDataSetChanged();

        mScanSubscription = getBleClient().scanBleDevices(
                new ScanSettings.Builder()
                        // .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // change if needed
                        // .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
                        .build()
                // add filters if needed
        )
                .subscribe(
                        scanResult -> {
                            Log.d(LOG_TAG,"scanResult: " + scanResult);

                            // Process scan result here. filter movesense devices.
                            if (scanResult.getBleDevice()!=null &&
                                    scanResult.getBleDevice().getName() != null &&
                                    scanResult.getBleDevice().getName().startsWith("Movesense")) {

                                // replace if exists already, add otherwise
                                MyScanResult msr = new MyScanResult(scanResult);
                                if (mScanResArrayList.contains(msr))
                                    mScanResArrayList.set(mScanResArrayList.indexOf(msr), msr);
                                else
                                    mScanResArrayList.add(0, msr);

                                mScanResArrayAdapter.notifyDataSetChanged();
                            }
                        },
                        throwable -> {
                            Log.e(LOG_TAG,"scan error: " + throwable);
                            // Handle an error here.

                            // Re-enable scan buttons, just like with ScanStop
                            onScanStopClicked(null);
                        }
                );


    }

    public void onScanStopClicked(View view) {
        if (mScanSubscription != null)
        {
            mScanSubscription.unsubscribe();
            mScanSubscription = null;
        }

        findViewById(R.id.buttonScan).setVisibility(View.VISIBLE);
        findViewById(R.id.buttonScanStop).setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position < 0 || position >= mScanResArrayList.size())
            return;

        MyScanResult device = mScanResArrayList.get(position);
        if (!device.isConnected()) {
            // Stop scanning
            onScanStopClicked(null);

            // And connect to the device
            connectBLEDevice(device);
        }
        else {
            subscribeToSensor(device.connectedSerial);
        }
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (position < 0 || position >= mScanResArrayList.size())
            return false;

        MyScanResult device = mScanResArrayList.get(position);
        if (!device.isConnected()) {
            onScanStopClicked(null);
            connectBLEDevice(device);
        }
        else
        {
            Log.i(LOG_TAG, "Disconnecting from BLE device: " + device.macAddress);
            mMds.disconnect(device.macAddress);
        }
        return true;
    }

    public void onDisconnectClicked(View view) {

        mdsSubscription.unsubscribe();
        mMds.disconnect(connectedDevice.macAddress);
        connectedDevice = null;
    }

    public String RequestDataFromDevice(String serial, String endpoint) {
        String uri = SCHEME_PREFIX + serial + endpoint;
        final Context ctx = this;
        mMds.get(uri, null, new MdsResponseListener() {
            @Override
            public void onSuccess(String s) {
                Log.i(LOG_TAG, "Device " + serial + " /info request succesful: " + s);
                // Display info in alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setTitle("Device info:")
                        .setMessage(s)
                        .show();
                RelayNotification(s);
            }

            @Override
            public void onError(MdsException e) {
                Log.e(LOG_TAG, "Device " + serial + " "+ endpoint + " returned error: " + e);
            }
        });
        return "asd";
    }

    public void onTestBtnClicked(View view) {
        RequestDataFromDevice(connectedDevice.connectedSerial, URI_MEAS_GYRO_INFO);
    }
}
