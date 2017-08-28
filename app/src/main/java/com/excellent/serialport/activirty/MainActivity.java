package com.excellent.serialport.activirty;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.excellent.serialport.R;
import com.excellent.serialport.serialutil.SerialPort;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MainActivity extends Activity {
    private TextView card_no;
    private FileInputStream mInputStream;
    private SerialPort sp;
    private ReadThread mReadThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        card_no = (TextView) findViewById(R.id.card_no);

        try {
            sp = new SerialPort(new File("/dev/ttyS3"), 9600);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mReadThread = new ReadThread();
        mReadThread.start();

        mInputStream = (FileInputStream) sp.getInputStream();
    }
    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[64];
                    if (mInputStream == null) {
                        return;
                    }
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        onDataReceived(buffer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }


    void onDataReceived(final byte[] buffer) {
        Message message = new Message();
        Bundle dataBundle = new Bundle();
        System.out.println("2016 " + printHexString(buffer));
        dataBundle.putString("card_no", printHexString(buffer).substring(0, 12));
        message.what = 1;
        message.setData(dataBundle);
        handler.sendMessage(message);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1:

                    card_no.append(" "+msg.getData().getString("card_no"));

                    break;
            }

            super.handleMessage(msg);
        }
    };

    private String printHexString(byte[] b) {
        String hexs = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            hexs += hex;
        }
        return hexs;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sp != null) {
            sp.close();
        }
    }
}
