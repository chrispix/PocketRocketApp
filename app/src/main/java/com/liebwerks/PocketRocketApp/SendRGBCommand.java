package com.liebwerks.PocketRocketApp;

import android.util.Log;

import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by klieberman on 5/25/15.
 */
public class SendRGBCommand implements Runnable {
    SocketAddress address = null;
    Socket clientSocket = null;
    DataOutputStream outToServer = null;
    /* @TODO: change hard coded IP address to a discovery or read from config file */
    String ip = "192.168.0.29";
    //byte [] ip={(byte)192,(byte)168,(byte)0,(byte)20};
    //byte [] IP={(byte)10, (byte)10, (byte)123, (byte)3};

    public RgbCommand command;

    //protected String doInBackground(byte[]... command) {
    public void run() {


        if(clientSocket == null) {
            try {

                address = new InetSocketAddress(InetAddress.getByName(ip), 5577);
                clientSocket = new Socket();
                clientSocket.connect(address, 4000);
                outToServer = new DataOutputStream(clientSocket.getOutputStream());
                Log.d("SocketConnect", "Completed socket connection");
            } catch (Exception e) {
                Log.e("SocketConnect", "Error doing socket connection: " + e.getMessage());
            }
        }

        try {
            outToServer.write(command.command);
        }
        catch(Exception e) {
            Log.e("SendRGBCommand", "error sending command");
        }

    }
    protected void onPostExecute(String theDone) {
        Log.d("TAG", "Done");
    }
}