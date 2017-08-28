package com.excellent.serialport.serialutil;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {

    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public SerialPort(File device, int baudrate) throws SecurityException, IOException {

        if (!device.canRead() || !device.canWrite()) {
            try {
                Process su;
                su = Runtime.getRuntime().exec("/system/xbin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }

        mFd = open(device.getAbsolutePath(), baudrate);
        if (mFd == null) {
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    // JNI
    private native static FileDescriptor open(String path, int baudrate);

    public native void close();

    static {
        System.loadLibrary("serial_port");
    }
}
