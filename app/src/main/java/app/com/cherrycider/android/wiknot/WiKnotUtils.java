package app.com.cherrycider.android.wiknot;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;


public class WiKnotUtils {

    ////////////////////////////////
    //// ПЕРЕМЕННЫЕ


    // фильтр для логов logcat
    private static final String TAG = "myLogs";

    private AsyncTask<Void, Void, Void> async_cient;
    private AsyncTask<Void, Void, Void> async_tcp_cient;
    public String Message;
    private static int SEND_SERVER_PORT = 4567;
    private static int SEND_FILE_SERVER_PORT = 4568;
    public static String MyIpAddress = "not set";
    public static String MyMACAddress = "not set";
    public static String MyIpNetmask = "not set";

    public static ArrayList<String> onLineIPAddresses = new ArrayList<>();


    public SharedPreferences myProfile;

    public File wiknotFolder = new File(Environment.getExternalStorageDirectory() + "/wiknot");



    ////////////////////////////////
    //// METHODS
    ///////////////////////////////


    /**
            * The following methods will resize the picture before printing

    */

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }


    // получаем массив ip адресов для отправкии сообщений в общий чат
    public static ArrayList<String> allCurrentUserIPAddresses() {

        // создаем объект для создания и управления версиями БД
        PeopleDBHelper peopleDBHelper = new PeopleDBHelper(MainActivity.MainContext);

        // создаем объект для данных
        ContentValues cv = new ContentValues();

        // получаем данные из полей ввода


        // подключаемся к БД
        SQLiteDatabase db = peopleDBHelper.getWritableDatabase();

        // Log.d(TAG, "--- onCreateView -- печатаем  peopleTable в страницу people online : ---");
        // делаем запрос всех данных из таблицы peopleTable, получаем Cursor
        Cursor c = db.query("peopleTable", null, null, null, null, null, null);


        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {


            // определяем номера столбцов по имени в выборке

            //int idColIndex = c.getColumnIndex("_id");
            //int nameColIndex = c.getColumnIndex("name");
            //int onlineStatusColIndex = c.getColumnIndex("onlineStatus");
            int IpAddressColIndex = c.getColumnIndex("IpAddress");
            //int SSIDColIndex = c.getColumnIndex("SSID");
            //int BSSIDColIndex = c.getColumnIndex("BSSID");
            //int userIDColIndex = c.getColumnIndex("userID");
            //int moreInfoColIndex = c.getColumnIndex("moreInfo");

            do {

                // выделяем ip адрес  и добавляем его в список адресов online

                String onLineIPAddress = c.getString(IpAddressColIndex);
                //String onLineIPAddress = msgToInterpret.replace("IAmOnline, ","");

                // TODO отобрать только IP адреса из текущей подсети

                if (!thisIPAddressIsOnline(onLineIPAddress)) {
                    onLineIPAddresses.add(onLineIPAddress);
                }

            } while (c.moveToNext());
        } else
            // Log.d(TAG, "0 rows");
            c.close();

        return onLineIPAddresses;
    }
    // этот метод проверяет, есть ли новый ip адрес в списке online ip адресов

    public static boolean thisIPAddressIsOnline(String ipaddr) {
        for (int i = 0; i < (onLineIPAddresses.size()); i++) {

            if (onLineIPAddresses.get(i).equals(ipaddr)) {

                return (true);
            }

        }

        return (false);


    }

    // получаем дату и время
    public static String getTimeAndDate() {
        Calendar timeanddate = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm");
        //return timeanddate.getTime().toString();  //полностью вся информация
        return sdf.format(timeanddate.getTime());
    }


    // прячем клавиатуру при курсоре в поле edittext
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    /////  Методы для определения HOTSPOT  Just call
    //              getWifiApConfiguration(getActivity()).SSID
    //to get the hotspot name. Nullpointer check is recommended before ;)


    public static WifiConfiguration getWifiApConfiguration(final Context ctx) {
        final WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        final Method m = getWifiManagerMethod("getWifiApConfiguration", wifiManager);
        if (m != null) {
            try {
                return (WifiConfiguration) m.invoke(wifiManager);
            } catch (Exception e) {
            }
        }
        return null;
    }

    private static Method getWifiManagerMethod(final String methodName, final WifiManager wifiManager) {
        final Method[] methods = wifiManager.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }


    // получаем ширину и высоту экрана
    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }


    //получаем уникальный ID пользователя, если приложение запускается впервые
    public String InstallationID(Context context) {

        final String INSTALLATION = "INSTALLATION";
        File installation = new File(context.getFilesDir(), INSTALLATION);
        try {
            if (!installation.exists()) {
                FileOutputStream out = new FileOutputStream(installation);
                String id = UUID.randomUUID().toString();
                out.write(id.getBytes());
                out.close();
            }

            RandomAccessFile f = new RandomAccessFile(installation, "r");
            byte[] bytes = new byte[(int) f.length()];
            f.readFully(bytes);
            f.close();
            return new String(bytes);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public String getMacAddress(Context context) {
        WifiManager wimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String macAddress = wimanager.getConnectionInfo().getMacAddress();
        if (macAddress == null) {
            macAddress = "Device don't have mac address or wi-fi is disabled";
        }
        return macAddress;
    }


    /**
     * Returns MAC address of the given interface name.
     * <p/>
     * public static String getMACAddress(String interfaceName) {
     * try {
     * List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
     * for (NetworkInterface intf : interfaces) {
     * if (interfaceName != null) {
     * if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
     * }
     * byte[] mac = intf.getHardwareAddress();
     * if (mac==null) return "";
     * StringBuilder buf = new StringBuilder();
     * for (int idx=0; idx<mac.length; idx++)
     * buf.append(String.format("%02X:", mac[idx]));
     * if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
     * return buf.toString();
     * }
     * } catch (Exception ex) { } // for now eat exceptions
     * return "";
     * try {
     * // this is so Linux hack
     * return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
     * } catch (IOException ex) {
     * return null;
     * }
     * }
     */


    // Этот метод пишет logcat внутри приложения
    public void LogCat() {


        //сделано  в новом thread


        new Thread(new Runnable() {
            public void run() {


                try {

                    Runtime.getRuntime().exec("logcat -f " + wiknotFolder + "/LogcatDump.txt");
  /*

            Process process = Runtime.getRuntime().exec("logcat -d");
           //Process process = Runtime.getRuntime().exec("logcat");



            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log=new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line + "\n");

            }
            appendToLogFile(log.toString());
            //Log.d(TAG, "добавляем в sdcard/wi-knot_log.file ");

*/
                } catch (IOException e) {
                }


            }


        }).start();


    }


    /**
     * This method writes log to file  sdcard/wi-knot_log.file
     */
    public void appendToLogFile(String text) {
        File wiknotFolder = new File(Environment.getExternalStorageDirectory() + "/wiknot");
        File logFile = new File(wiknotFolder + "/wi-knot_log.file");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
                //Log.d(TAG, "создан sdcard/wi-knot_log.file");

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                //Log.d(TAG, "Не создан sdcard/wi-knot_log.file !!!!! " );
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            //buf.flush();
            buf.close();
            //Log.d(TAG, "добавлено в sdcard/wi-knot_log.file");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * метод getIpAddress() возвращает мой IP адрес (и маску ставит пока 24)
     */


    public String getIpAddress() {
        InetAddress inetAddress = null;
        InetAddress myAddr = null;
        int inetMask = 0;
        int myMask = 0;
        MyIpAddress = "not set";
        MyIpNetmask = "not set";


        try {
            for (Enumeration<NetworkInterface> networkInterface = NetworkInterface.getNetworkInterfaces();
                 networkInterface.hasMoreElements(); ) {

                NetworkInterface singleInterface = networkInterface.nextElement();

                for (Enumeration<InetAddress> IpAddresses = singleInterface.getInetAddresses();
                     IpAddresses.hasMoreElements(); ) {
                    inetAddress = IpAddresses.nextElement();

                    if (!inetAddress.isLoopbackAddress() && (inetAddress.getAddress().length == 4) &&
                            (singleInterface.getDisplayName().contains("wlan0") ||
                                    singleInterface.getDisplayName().contains("wl0") ||
                                    singleInterface.getDisplayName().contains("eth0") ||
                                    singleInterface.getDisplayName().contains("ap0"))) {
                        //     if (Build.VERSION.SDK_INT >=11)
                        //         inetMask = singleInterface.getInterfaceAddresses().get(1).getNetworkPrefixLength();

                        //    else
                        inetMask = 24;
                        myAddr = inetAddress;
                        myMask = inetMask;
                        //Log.d(TAG, "myAddr = " + String.valueOf(myAddr) + "/" + String.valueOf(myMask));
                    }
                }
            }

        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }

        if (!(myAddr == null)) {
            MyIpAddress = myAddr.toString().replace("/", "");
            MyIpNetmask = String.valueOf(myMask);
            return MyIpAddress;
        }
        return MyIpAddress;
    }


    public void SendFile(final File sourcePath,
                         final String sourceFileName,
                         final String destinationFileName,
                         final String inetAddrToSendTo) {
        async_tcp_cient = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected Void doInBackground(Void... params) {


                // check if file exists
                File sourceFile = new File(sourcePath, sourceFileName);

                if (sourceFile.exists()) {

                    Socket socket = null;


                    try {

                        //Log.d(TAG, "wiknot.SendFile: start sending file");
                        socket = new Socket(inetAddrToSendTo, SEND_FILE_SERVER_PORT);


                        //File sourceFile = new File(sourcePath, sourceFileName);


                        //default path Environment.getExternalStorageDirectory()

                        // Get the size of the file
                        // long length = file.length();

                        //if (length > Integer.MAX_VALUE) {
                        //    System.out.println("File is too large.");
                        //}
                        //byte[] bytes = new byte[(int) length];

                        byte[] bytes = new byte[(int) sourceFile.length()];
                        InputStream in = new FileInputStream(sourceFile);

                        BufferedInputStream bis = new BufferedInputStream(in);
                        DataInputStream dis = new DataInputStream(bis);
                        dis.readFully(bytes, 0, bytes.length);

                        OutputStream out = socket.getOutputStream();

                        //Sending file name and file size to the server
                        DataOutputStream dos = new DataOutputStream(out);
                        dos.writeUTF(destinationFileName);
                        dos.writeLong(bytes.length);
                        dos.write(bytes, 0, bytes.length);
                        dos.flush();


                        //int count;
                        //while ((count = in.read(bytes)) > 0) {
                        //out.write(bytes, 0, count);
                        //}

                        //Sending file data to the server
                        out.write(bytes, 0, bytes.length);
                        out.flush();

                        out.close();
                        dos.close();
                        in.close();
                        socket.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "wiknot.SendFile IOException:" + e);
                    }


                }
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }

        };


        async_tcp_cient.execute();
    }


    /// This method SendMessage sends message in AsyncTask

    public void SendMessage(final String msgToSend, final String inetAddrToSendTo) {
        async_cient = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected Void doInBackground(Void... params) {


                DatagramSocket ds = null;

                try {
                    ds = new DatagramSocket();
                    DatagramPacket dp;
                    dp = new DatagramPacket(msgToSend.getBytes("cp1251"), msgToSend.length(), InetAddress.getByName(inetAddrToSendTo), SEND_SERVER_PORT);
                    ds.setBroadcast(true);
                    ds.send(dp);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (ds != null) {
                        ds.close();
                    }
                }


                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }

        };


        async_cient.execute();
    }


    /// This method SendMessage sends message in the same Thread (dont use in UI)

    public void SendMessagefromSameThread(final String msgToSend, final String inetAddrToSendTo) {

                DatagramSocket ds = null;

                try {
                    ds = new DatagramSocket();
                    DatagramPacket dp;
                    dp = new DatagramPacket(msgToSend.getBytes("cp1251"), msgToSend.length(), InetAddress.getByName(inetAddrToSendTo), SEND_SERVER_PORT);
                    ds.setBroadcast(true);
                    ds.send(dp);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (ds != null) {
                        ds.close();
                    }
                }

    }


    /**
     *
     * метод возвращает IP Broadcast адрес моей сети
     *
     *

     public InetAddress getBroadcast(InetAddress inetAddr) {

     NetworkInterface temp;
     InetAddress bAddr = null;

     try {
     temp = NetworkInterface.getByInetAddress(inetAddr);
     List< InterfaceAddress > addresses = temp.getInterfaceAddresses();

     for (InterfaceAddress inetAddress: addresses)

     bAddr = inetAddress.getBroadcast();
     //Log.d(TAG, "bAddr=" + bAddr);
     BroadcastAddress = bAddr.toString().replace("/","");
     return bAddr;

     } catch (SocketException e) {

     e.printStackTrace();
     //Log.d(TAG, "getBroadcast" + e.getMessage());
     }
     return null;
     }

     */


    /**
     * Метод writeLogCatToFile пишет логи в файл
     * @param str
     *

    public static void writeLogCatToFile(String str) {

    File file = new File("sdcard/wi-knot_logcat.file");
    InputStream fileInputStream = null;
    FileOutputStream fileOutpurStream = null;
    try {
    fileInputStream = new FileInputStream(file);
    fileOutpurStream = new FileOutputStream(file);
    if (file.exists()) {
    int ch = 0;
    int current = 0;
    StringBuffer buffer = new StringBuffer();
    while ((ch = fileInputStream.read()) != -1) {
    buffer.append((char) ch);
    current=current++;
    }
    byte data[] = new byte[(int) file.length()];
    fileInputStream.read(data);
    fileOutpurStream.write(data);
    fileOutpurStream.write(str.getBytes(), 0, str.getBytes().length);
    fileOutpurStream.flush();
    } else {
    file.createNewFile();
    fileOutpurStream.write(str.getBytes(), 0, str.getBytes().length);
    fileOutpurStream.flush();
    }
    } catch (Exception e) {
    e.printStackTrace();
    } finally {
    try {
    fileInputStream.close();
    fileOutpurStream.flush();
    fileOutpurStream.close();
    fileOutpurStream = null;
    fileInputStream = null;
    } catch (IOException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
    }
    }
    }



     */


}