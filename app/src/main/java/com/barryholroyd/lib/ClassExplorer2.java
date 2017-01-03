package com.barryholroyd.lib;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.barryholroyd.bluetoothchatdemo.support.Support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Created by Barry on 1/2/2017.
 */
public class ClassExplorer2 extends com.barryholroyd.lib.ClassExplorer
{
    public ClassExplorer2(String label, Class clazz) {
        super(label, clazz);
    }
    public ClassExplorer2(String label, Object obj) {
        super(label, obj);
    }

    @Override
    protected void log(String s) {
        Support.error(s);
    }


    static public void logBluetoothDevice(BluetoothDevice btdevice) {
        Support.error("_______________ BLUETOOTH DEVICE CLASS INFO _________________");
        ClassExplorer2 ce2 =
                new ClassExplorer2("BluetoothDevice Info", BluetoothDevice.class);
        ce2.opt_methods = true;
        ce2.opt_recursive = true;
        ce2.display();

        try {
            Method m = btdevice.getClass().getMethod("createRfcommSocket",
                    int.class);
            BluetoothSocket mSocket = (BluetoothSocket) m.invoke(btdevice, 1);
            Support.error(String.format("mSocket: %#x", mSocket.hashCode()));

//            btdevice.createRfcommSocket(1);

            Support.error("________________________________");
            Support.error(String.format(Locale.US, "TBD: method accessible is: %b",
                    m.isAccessible()));
            Support.error(String.format("MODIFIERS: %#x, %x", m.getModifiers(), m.getModifiers()));

            Annotation[] annotations = m.getAnnotations();
            Support.error(String.format("ANNOTATION COUNT: %d", annotations.length));

            Support.error("________________________________");
        }
        catch (Exception e) {
            String msg = String.format(Locale.US, "Exception: %s", e.getMessage());
            Support.userMessage(msg);
            return;
        }
    }
}
