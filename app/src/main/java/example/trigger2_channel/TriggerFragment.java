package example.trigger2_channel;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayDeque;

public class TriggerFragment extends Fragment implements ServiceConnection, SerialListener {

    private static final String CHANNEL_ID = "BluetoothChannel";

    private SwitchCompat btnRelay1;
    private SwitchCompat btnRelay2;
    private Button btnStartTimer;
    private Button btnMode;
    private Button btnReset;
    private EditText countdownText1;
    private TextView timestampText;

    private CountDownTimer countDownTimer1;
    private CountDownTimer countDownTimer2;
    private int currentMode = 1;
    private boolean relay1On = false;
    private int relay1Time = 0;

    private String deviceAddress;
    private SerialService service;
    private boolean initialStart = true;
    private boolean connected = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trigger, container, false);

        btnRelay1 = view.findViewById(R.id.btn_relay1);
        btnRelay2 = view.findViewById(R.id.btn_relay2);
        timestampText = view.findViewById(R.id.timestampText);
        countdownText1 = view.findViewById(R.id.countdown_text1);
        btnStartTimer = view.findViewById(R.id.btn_StartTimer);
        btnMode = view.findViewById(R.id.btn_Mode);
        btnReset = view.findViewById(R.id.btn_Reset);

        btnRelay1.setOnClickListener(v -> sendCommand("1"));
        btnRelay2.setOnClickListener(v -> sendCommand("2"));
        btnStartTimer.setOnClickListener(v -> startCountdown());
        btnMode.setOnClickListener(v -> switchMode());
        btnReset.setOnClickListener(v -> reset());

        btnRelay1.setOnClickListener(v -> {
            if (btnRelay1.isChecked()) {
                btnRelay1.setChecked(true);
                timestampText.setText("Relay 1: On");
                sendCommand("1"); //Hidupkan Relay 1
            } else {
                btnRelay1.setChecked(false);
                timestampText.setText("Relay 1: Off");
                sendCommand("2"); //Matikan Relay 1
            }
        });

        btnRelay2.setOnClickListener(v -> {
            if (btnRelay2.isChecked()) {
                btnRelay2.setChecked(true);
                timestampText.setText("Relay 2: On");
                sendCommand("3"); //Hidupkan Relay 2
            } else {
                btnRelay2.setChecked(false);
                timestampText.setText("Relay 2: Off");
                sendCommand("4"); //Matikan Relay 2
            }
        });

        deviceAddress = getArguments().getString("device");
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);

        return view;
    }

    @Override
    public void onDestroy() {
        if (connected) {
            disconnect();
        }
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (service != null) {
            service.attach(this);
        } else {
            getActivity().startService(new Intent(getActivity(), SerialService.class));
        }
    }

    @Override
    public void onStop() {
        if (service != null && !getActivity().isChangingConfigurations()) {
            service.detach();
        }
        super.onStop();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if (initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            connected = true;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = false;
        service.disconnect();
    }

    private void sendCommand(String command) {
        if (!connected) {
            Toast.makeText(getActivity(), "Tidak terhubung", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            byte[] data = (command + "\r\n").getBytes();
            service.write(data);
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    @Override
    public void onSerialConnect() {
        connected = true;
        showNotification("Bluetooth Terhubung", "Device berhasil terhubung.");
    }

    private void showNotification(String title, String message) {
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifikasi)
                .setColor(getResources().getColor(R.color.cream))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon))
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(1, builder.build());
        } else {
            Toast.makeText(getActivity(), "Notifikasi dinonaktifkan", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Koneksi Bluetooth";
            String description = "Notifikasi untuk status koneksi Bluetooth";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onSerialConnectError(Exception e) {
        connected = false;
        Toast.makeText(getActivity(), "Koneksi gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSerialRead(byte[] data) {
        // Handle incoming data
    }

    @Override
    public void onSerialRead(ArrayDeque<byte[]> datas) {
        // Handle incoming data
    }

    @Override
    public void onSerialIoError(Exception e) {
        connected = false;
        Toast.makeText(getActivity(), "Koneksi terputus: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void startCountdown() {
    String timeStr = countdownText1.getText().toString();
    if (timeStr.isEmpty()) {
        Toast.makeText(getActivity(), "Silakan masukkan waktu dalam detik", Toast.LENGTH_SHORT).show();
        return;
    }

    long duration = Long.parseLong(timeStr) * 1000;
    if (currentMode == 1) {
        // Mode 1: Hitung mundur tunggal untuk kedua relay
        new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
                timestampText.setText("detik tersisa: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                timestampText.setText("Relay 1 dan 2: ON");
                btnRelay1.setChecked(true);
                btnRelay2.setChecked(true);
                sendCommand("1"); // Hidupkan Relay 1
                sendCommand("3"); // Hidupkan Relay 2
            }
        }.start();
    } else if (currentMode == 2) {
        // Mode 2: Hitung mundur dua tahap untuk setiap relay
        if (relay1Time == 0) {
            // Tahap pertama: Atur waktu untuk Relay 1
            relay1Time = (int) (duration / 1000);
            Toast.makeText(getActivity(), "Waktu diatur untuk Relay 1. Atur waktu untuk Relay 2 dan tekan Mulai Timer lagi.", Toast.LENGTH_SHORT).show();
        } else {
            // Tahap kedua: Hitung mundur untuk Relay 1, lalu Relay 2
            new CountDownTimer(relay1Time * 1000, 1000) {
                public void onTick(long millisUntilFinished) {
                    timestampText.setText("detik tersisa: " + millisUntilFinished / 1000);
                }

                public void onFinish() {
                    timestampText.setText("Relay 1: ON");
                    btnRelay1.setChecked(true);
                    sendCommand("1"); // Hidupkan Relay 1
                    new CountDownTimer(duration, 1000) {
                        public void onTick(long millisUntilFinished) {
                            timestampText.setText("detik tersisa: " + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
                            timestampText.setText("Relay 1 and 2: ON");
                            btnRelay2.setChecked(true);
                            sendCommand("3"); // Hidupkan Relay 2
                        }
                    }.start();
                }
            }.start();
        }
    } else if (currentMode == 3) {
        // Mode 3: Hitung mundur dua tahap dengan logika berbeda
        if (!relay1On) {
            // Tahap pertama: Hitung mundur untuk Relay 1
            new CountDownTimer(duration, 1000) {
                public void onTick(long millisUntilFinished) {
                    timestampText.setText("detik tersisa: " + millisUntilFinished / 1000);
                }

                public void onFinish() {
                    timestampText.setText("Relay 1: ON");
                    btnRelay1.setChecked(true);
                    sendCommand("1"); // Hidupkan Relay 1
                    relay1On = true;
                }
            }.start();
        } else {
            // Tahap kedua: Hitung mundur untuk Relay 2
            new CountDownTimer(duration, 1000) {
                public void onTick(long millisUntilFinished) {
                    timestampText.setText("detik tersisa: " + millisUntilFinished / 1000);
                }

                public void onFinish() {
                    timestampText.setText("Relay 2: ON");
                    btnRelay2.setChecked(true);
                    sendCommand("3"); // Hidupkan Relay 2
                }
            }.start();
        }
    }
}

    private void switchMode() {
        if (currentMode == 1) {
            currentMode = 2;
            btnMode.setText("Mode 2");
        } else if (currentMode == 2) {
            currentMode = 3;
            btnMode.setText("Mode 3");
        } else {
            currentMode = 1;
            btnMode.setText("Mode 1");
        }
        Toast.makeText(getActivity(), "Beralih ke Mode " + currentMode, Toast.LENGTH_SHORT).show();
    }

    private void reset() {
        if (countDownTimer1 != null) {
            countDownTimer1.cancel();
        }
        if (countDownTimer2 != null) {
            countDownTimer2.cancel();
        }
        timestampText.setText("Timestamp: 00:00");
        countdownText1.setText("");
        btnRelay1.setChecked(false);
        sendCommand("2"); //Matikan Relay 1
        btnRelay2.setChecked(false);
        sendCommand("4"); //Matikan Relay 2
        relay1Time = 0;
        relay1On = false;
    }

}