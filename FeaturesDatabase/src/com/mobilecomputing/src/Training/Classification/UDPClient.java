package com.mobilecomputing.src.Training.Classification;

import java.io.*;
import java.net.*;

public class UDPClient {
    public static void main(String args[]) throws Exception {
        SendString("anil", "pow");
    }

    public static void SendString(String aUsername, String aString) throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("localhost");
        byte[] sendData = new byte[1024];
        sendData = (aUsername + "_" + aString).getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 21567);
        clientSocket.send(sendPacket);
        clientSocket.close();
    }
}
