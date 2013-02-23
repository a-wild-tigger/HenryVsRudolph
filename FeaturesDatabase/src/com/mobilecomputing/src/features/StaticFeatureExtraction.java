package com.mobilecomputing.src.features;

import com.threegear.apps.demos.WorkspaceRenderingHelpers;
import com.threegear.gloveless.network.HandTrackingAdapter;
import com.threegear.gloveless.network.HandTrackingClient;
import com.threegear.gloveless.network.HandTrackingMessage;
import com.threegear.gloveless.network.PoseMessage;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glColor3f;

public class StaticFeatureExtraction extends HandTrackingAdapter {
    private Matrix4f[][] jointFrames = new Matrix4f[HandTrackingMessage.N_HANDS][HandTrackingMessage.N_JOINTS];
    private Point3f[][] fingerTips = new Point3f[HandTrackingMessage.N_HANDS][HandTrackingMessage.N_FINGERS];

    public synchronized void Snapshot() {

    }

    @Override
    public synchronized void handleEvent(HandTrackingMessage rawMessage) {
        // Cache the coordinate frames and finger tips of the skeleton for rendering
        if (rawMessage.getType() == HandTrackingMessage.MessageType.POSE) {
            PoseMessage message = (PoseMessage) rawMessage;
            for (int iHand=0; iHand<jointFrames.length; iHand++) {
                Matrix4f[] jointFrames = message.getJointFrames(iHand);
                for (int jJoint=0; jJoint<jointFrames.length; jJoint++) {
                    this.jointFrames[iHand][jJoint].set(jointFrames[jJoint]);
                }
            }

            for (int iHand=0; iHand<fingerTips.length; iHand++) {
                for (int jFinger=0; jFinger<fingerTips[0].length; jFinger++) {
                    Point3f[] fingerTips = message.getFingerTips(iHand);
                    this.fingerTips[iHand][jFinger].set(fingerTips[jFinger]);
                }
            }
        }
    }
}
