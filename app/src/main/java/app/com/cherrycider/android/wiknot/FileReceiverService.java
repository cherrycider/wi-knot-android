package app.com.cherrycider.android.wiknot;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class FileReceiverService extends IntentService {

    final String TAG = "myLogs";


    private static boolean serviceActive = true;
    public String msgReceived;
    private static int LISTEN_FILE_SERVER_PORT = 4568;
    public File wiknotFolder = new File(Environment.getExternalStorageDirectory() + "/wiknot");


    // коструктор вспомогательного класса WiKnotUtils
    WiKnotUtils wiknot = new WiKnotUtils();

    ChatDBHelper chatDBHelper;


    public ServerSocket serverSocket = null;
    public Socket socket = null;
    //public InputStream in = null;
    //public OutputStream out = null;

    public FileReceiverService() {
        super("File Receiver Service");
    }

    public void onCreate() {
        super.onCreate();
        //Log.d(TAG, "UdpReceiverService onCreate");


        try {
            serverSocket = new ServerSocket(LISTEN_FILE_SERVER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // создаем объект для создания и управления версиями БД
        chatDBHelper = new ChatDBHelper(this);
    }

    ;


    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.d(TAG, "FileReceiverService onStartCommand");

        //receiveTask();


        onHandleIntent(intent);
        //return super.onStartCommand(intent, flags, startId);
        //Log.d(TAG, "FileReceiverService is UP");
        return START_STICKY;
    }

    public void onDestroy() {
        // onDestroy почему-то не запускается скомандой stopService !!!!!
        // по команде goExit запускается но не работает метод :public void stopUdpReceiverService()
        //                                                       {stopService(new Intent(this, UdpReceiverService.class));}


        try {
            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.d(TAG, "FileReceiverService is onDestroy()");
        super.onDestroy();


    }


    public IBinder onBind(Intent intent) {
        //Log.d(TAG, "UdpReceiverService onBind");
        return null;
    }


    @Override
    protected void onHandleIntent(final Intent intent) {

        // получаем разрешение на прием Multicast UDP
        WifiManager wifi;
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wifi.createMulticastLock("MulticastLock");
        multicastLock.acquire();


        /*

        //void receiveTask() {
        new Thread(new Runnable() {
            public void run() {


                //ServerSocket serverSocket = null;

                //try {

                //serverSocket = new ServerSocket(); // <-- create an unbound socket first
                //serverSocket.setReuseAddress(true);
                //serverSocket.bind(new InetSocketAddress(LISTEN_FILE_SERVER_PORT)); // <-- now bind it

                //serverSocket = new ServerSocket(LISTEN_FILE_SERVER_PORT);

                //} catch (IOException ex) {
                //     System.out.println("Can't setup server on this port number. ");
                //    Log.e(TAG, "FileReceiverService: Can't setup server on this port number. " + ex);
                //}


                int bytesRead;
                InputStream in = null;
                //OutputStream out = null;

                if (serverSocket != null)
                    try {
                        socket = serverSocket.accept();


                        if (socket != null) {
                            in = socket.getInputStream();


                            DataInputStream clientData = new DataInputStream(in);

                            String fileName = clientData.readUTF();

                            OutputStream output = new FileOutputStream(wiknotFolder + "/" + fileName);
                            long size = clientData.readLong();
                            byte[] buffer = new byte[1024];

                            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                                output.write(buffer, 0, bytesRead);
                                size -= bytesRead;
                            }

                            //Log.d(TAG, "FileReceiverService: FileOutputStream" + wiknotFolder + "/" + fileName);


                            // Closing the FileOutputStream handle
                            in.close();
                            clientData.close();
                            output.close();


                            //Log.d(TAG, "FileReceiverService: file is received " + wiknotFolder + "/" + fileName);


                        } else
                            Log.d(TAG, "FileReceiverService: socket = null");

                    } catch (IOException e) {
                        e.printStackTrace();

                    }


                // } catch (FileNotFoundException ex) {
                //     System.out.println("File not found. ");
                //     Log.e(TAG, "FileReceiverService: FileOutputStream(\"/storage/sdcard0/receivedtest.jpg\")" + ex);
                // }

                // byte[] bytes = new byte[16*1024];

                // int count;
                // try {
                //     while ((count = in.read(bytes)) > 0) {
                //         out.write(bytes, 0, count);
                //     }
                // } catch (IOException e) {
                //     e.printStackTrace();
                //    Log.e(TAG, "FileReceiverService: in.read(bytes)" + e);
                // }





            }


        }).start();

        */


        FileReceiverThread fileReceiverThread = new FileReceiverThread();
        fileReceiverThread.start();

    }


    class FileReceiverThread extends Thread {

        // method receive file
        FileReceiverThread() {
        }

        public void run() {

            while (serviceActive) {
                int bytesRead;
                InputStream in = null;
                //OutputStream out = null;

                if (serverSocket != null)
                    try {
                        socket = serverSocket.accept();


                        if (socket != null) {
                            in = socket.getInputStream();


                            DataInputStream clientData = new DataInputStream(in);

                            String fileName = clientData.readUTF();

                            OutputStream output = new FileOutputStream(wiknotFolder + "/" + fileName);
                            long size = clientData.readLong();
                            byte[] buffer = new byte[1024];

                            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                                output.write(buffer, 0, bytesRead);
                                size -= bytesRead;
                            }

                            //Log.d(TAG, "FileReceiverService: FileOutputStream" + wiknotFolder + "/" + fileName);


                            // Closing the FileOutputStream handle
                            in.close();
                            clientData.close();
                            output.close();


                            //Log.d(TAG, "FileReceiverService: file is received " + wiknotFolder + "/" + fileName);


                        } else
                            Log.d(TAG, "FileReceiverService: socket = null");

                    } catch (IOException e) {
                        e.printStackTrace();

                    }
            }
        }
    }


}









