package com.mobilecomputing.src.inout.output;

import com.threegear.gloveless.network.*;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import java.io.IOException;

public class SingleCapture implements HandTrackingListener {
    private boolean waitingCapture = false;
    private PoseMessage aMessage;

    public synchronized boolean isReady() {
        return waitingCapture;
    }

    public synchronized PoseMessage Capture() {
        return aMessage;
    }

    @Override
    public synchronized void handleEvent(HandTrackingMessage rawMessage) {
        if (rawMessage.getType() == HandTrackingMessage.MessageType.POSE) {
            waitingCapture = true;
            PoseMessage message = (PoseMessage) rawMessage;
            aMessage = message;
        }
    }

    @Override
    public synchronized void handleConnectionClosed() {

    }
}
