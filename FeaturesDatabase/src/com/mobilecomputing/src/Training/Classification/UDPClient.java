package com.mobilecomputing.src.Training.Classification;

import org.joda.time.DateTime;

import java.io.*;
import java.net.*;
import java.util.Date;

public class UDPClient {
    private static int PORT = 11112;
    private boolean awake = false;
    private static DateTime restartOn = new DateTime();

    public static void main(String args[]) throws Exception {
        SendString("anil", "pow");
    }

    public static void SendString(String aUsername, String aString) {
        DateTime myCurrentDate = new DateTime();
        if (myCurrentDate.isBefore(restartOn)) { return; }
        restartOn = myCurrentDate.plusMillis(300);

        try {
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName("localhost");
            byte[] sendData = new byte[1024];
            sendData = (aUsername + "_" + aString).getBytes();
            PORT = 11111;
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, PORT);
            clientSocket.send(sendPacket);
            clientSocket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
