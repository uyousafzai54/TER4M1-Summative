package com.max_belleville.messaging_app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class TerminalFragment extends Fragment implements ServiceConnection, SerialListener {

    private enum Connected { False, Pending, True }
    private String receiveStr="";
    private String deviceAddress;
    private SerialSocket socket;
    private SerialService service;
    private boolean initialStart = true;
    private Connected connected = Connected.False;

    private MessageAdapter messageAdapter;

    public TerminalFragment() {
    }

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get device from device fragment on fragment creation

        setRetainInstance(true);
        deviceAddress = getArguments().getString("device");
    }

    @Override
    public void onDestroy() {
        //When you close app disconnect bluetooth stop Serial connection

        if (connected != Connected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        //Also save messages into internal storage

        messageAdapter.saveInfo();
        super.onDestroy();
    }

    @Override
    public void onStart() {
        //When fragment has loaded connect fragments listeners to serial also start service

        super.onStart();
        if(service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        //Disconnects listeners that where attached to the serial

        if(service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation") // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try { getActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public void onResume() {
        //When you re-open app reload ui

        super.onResume();
        if(initialStart && service !=null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        //When serial is connected to bluetooth reload ui

        service = ((SerialService.SerialBinder) binder).getService();
        if(initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Setup current view (fragment ui)

        View view = inflater.inflate(R.layout.fragment_terminal, container, false);

        //Init input field and send button

        TextView editText = view.findViewById(R.id.editText);
        ImageButton sendBtn = view.findViewById(R.id.sendButton);
        //Setup message adapter used to handle messages and list that displays all messages

        messageAdapter = new MessageAdapter(getContext());
        ListView viewer = view.findViewById(R.id.messages_view);
        viewer.setAdapter(messageAdapter);
        //Load messages from internal storage

        messageAdapter.loadInfo();
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //When you click send, send message and clear input field
                send(editText.getText().toString());
                editText.setText("");
            }
        });
        return view;
    }
    /*
     * Serial + UI
     */
    private void connect() {
        try {
            //When you connect get device and bluetooth adapter

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            String deviceName = device.getName() != null ? device.getName() : device.getAddress();
            //Connect serial socket to device(used to send data via bluetooth)

            connected = Connected.Pending;
            socket = new SerialSocket();
            service.connect(this, "Connected to " + deviceName);
            socket.connect(getContext(), service, device);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        //Remove serial connection when bluetooth can't connect

        connected = Connected.False;
        service.disconnect();
        socket.disconnect();
        socket = null;
    }

    private void send(String str) {
        //If you can't connect display the fact you can't connect

        if(connected != Connected.True) {
            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        //Otherwise ensure that message isn't empty and isn't a command

        try {
            if(!str.startsWith("/")&&!str.isEmpty())
            messageAdapter.add(new Message(str,true));
            //Convert to byte array as socket can't send string data and send it to bluetooth

            byte[] data = (str + "\n").getBytes();
            socket.write(data);
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    private void receive(byte[] data) {
            //Bluetooth can only receive and send so much data at once so we need to add it to a string
            //Until the message has reached it's end

            receiveStr+=new String(data);
            //Check if it's the /test command before the normal messaging has been received as we
            //we don't want it to be handled like a message and we do to something different with it.

            if(receiveStr.startsWith("/test")) {
                try {
                    //Send message back to arduino so it can light up led

                    socket.write(data);
                } catch (IOException e) {
                    onSerialIoError(e);
                }
            }
            else{
            if(receiveStr.contains("\n")) {
                //Remove ending character from message and add message to list of messages

                receiveStr=receiveStr.replace("\n","");
                messageAdapter.add(new Message(receiveStr,false));
                receiveStr="";
            }
            }
            //TODO: fix error where commands will stop messages from getting received
    }


    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        receive(data);
    }

    @Override
    public void onSerialIoError(Exception e) {
        disconnect();
    }

}
