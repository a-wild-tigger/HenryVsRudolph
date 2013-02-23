package com.mobilecomputing.src.inout.output;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;

public class MicrophoneReader {
    public static void main(String[] args) {
        TargetDataLine line = null;

        float sampleRate = 8000;
        int sampleSizeInBits = 8;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format =  new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); // format is an AudioFormat object
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Does Not Support");
        }

        // Obtain and open the line.

        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
        } catch (LineUnavailableException ex) {
            System.out.println("Does Not Support Error 2");
        }

// Assume that the TargetDataLine, line, has already
// been obtained and opened.
        ByteArrayOutputStream out  = new ByteArrayOutputStream();
        int numBytesRead;
        byte[] data = new byte[line.getBufferSize() / 5];

// Begin audio capture.
        line.start();

// Here, stopped is a global boolean set by another thread.
        while (true) {
            // Read the next chunk of data from the TargetDataLine.
            numBytesRead =  line.read(data, 0, data.length);
            // Save this chunk of data.
            out.write(data, 0, numBytesRead);
        }


    }
}
