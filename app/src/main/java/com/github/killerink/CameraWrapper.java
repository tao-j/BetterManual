package com.github.killerink;

import android.hardware.Camera;
import android.util.Pair;
import android.view.SurfaceHolder;

import com.sony.scalar.hardware.CameraEx;

import java.io.IOException;
import java.util.List;

/**
 * Created by troop on 26.08.2017.
 */

public class CameraWrapper
{
    private CameraEx m_camera;
    private Camera.Parameters parameters;
    private CameraEx.ParametersModifier modifier;

    public CameraWrapper()
    {
        m_camera = CameraEx.open(0, null);
        parameters = m_camera.getNormalCamera().getParameters();
        modifier = m_camera.createParametersModifier(parameters);
    }

    public void closeCamera()
    {
        m_camera.getNormalCamera().stopPreview();
        m_camera.release();
        m_camera = null;
    }

    public CameraEx getCameraEx()
    {
        return m_camera;
    }

    public void takePicture()
    {
        m_camera.stopDirectShutter(new CameraEx.DirectShutterStoppedCallback() {
            @Override
            public void onShutterStopped(CameraEx cameraEx) {
                m_camera.burstableTakePicture();
            }
        });

    }

    public void cancelTakePicture()
    {
        m_camera.cancelTakePicture();
        m_camera.startDirectShutter();
    }

    public Camera.Parameters getParameters()
    {
        return parameters;
    }

    public void setParameters(Camera.Parameters parameters)
    {
        m_camera.getNormalCamera().setParameters(parameters);
    }

    public CameraEx.ParametersModifier getParametersModifier()
    {
        return m_camera.createParametersModifier(m_camera.getNormalCamera().getParameters());
    }

    public void setSurfaceHolder(SurfaceHolder surface)
    {
        try {
            m_camera.getNormalCamera().setPreviewDisplay(surface);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startDisplay()
    {
        m_camera.getNormalCamera().startPreview();
    }

    public void stopDisplay()
    {
        m_camera.getNormalCamera().stopPreview();
    }

    public void startPreview()
    {
        m_camera.startDirectShutter();
    }

    public void stopPreview()
    {
        m_camera.stopDirectShutter(new CameraEx.DirectShutterStoppedCallback() {
            @Override
            public void onShutterStopped(CameraEx cameraEx) {

            }
        });
    }


    public int getExposureCompensation() {
        return getParameters().getExposureCompensation();
    }

    public void setExposureCompensation(int value) {
        Camera.Parameters parameters = getParameters();
        parameters.setExposureCompensation(value);
        setParameters(parameters);
    }

    public int getMaxExposureCompensation() {
        return getParameters().getMaxExposureCompensation();
    }

    public int getMinExposureCompensation() {
        return getParameters().getMinExposureCompensation();
    }

    public float getExposureCompensationStep() {
        return getParameters().getExposureCompensationStep();
    }

    public boolean isLongExposureNoiseReductionSupported()
    {
        return modifier.isSupportedLongExposureNR();
    }

    public void setLongExposureNoiseReduction(boolean enable)
    {
        modifier.setLongExposureNR(enable);
        setParameters(parameters);
    }

    public void setFocusMode(String value)
    {
        parameters.setFocusMode(value);
        setParameters(parameters);
    }

    public void setSceneMode(String value)
    {
        parameters.setSceneMode(value);
        setParameters(parameters);
    }

    public String getSceneMode()
    {
        return parameters.getSceneMode();
    }

    public void setDriveMode(String value)
    {
        modifier.setDriveMode(value);
        setParameters(parameters);
    }

    public String getDriveMode()
    {
        return modifier.getDriveMode();
    }

    public void setImageAspectRatio(String value)
    {
        modifier.setImageAspectRatio(value);
        setParameters(parameters);
    }

    public void setBurstDriveSpeed(String value)
    {
        modifier.setBurstDriveSpeed(value);
        setParameters(parameters);
    }

    public String getBurstDriveSpeed()
    {
        return modifier.getBurstDriveSpeed();
    }

    public boolean isAutoShutterSpeedLowLimitSupported()
    {
        return modifier.isSupportedAutoShutterSpeedLowLimit();
    }

    public void setAutoShutterSpeedLowLimit(int value)
    {
        modifier.setAutoShutterSpeedLowLimit(value);
        setParameters(parameters);
    }

    public void setSelfTimer(int value)
    {
        modifier.setSelfTimer(value);
        setParameters(parameters);
    }

    public List<Integer> getSupportedISOSensitivities()
    {
        return modifier.getSupportedISOSensitivities();
    }

    public int getISOSensitivity()
    {
        return modifier.getISOSensitivity();
    }

    public void setISOSensitivity(int value)
    {
        modifier.setISOSensitivity(value);
        setParameters(parameters);
    }

    public void setPreviewMagnification(int factor, Pair position)
    {
        m_camera.setPreviewMagnification(factor,position);
    }

    public void decrementShutterSpeed(){m_camera.decrementShutterSpeed();}
    public void incrementShutterSpeed(){m_camera.incrementShutterSpeed();}
    public void decrementAperture(){m_camera.decrementAperture();}
    public void incrementAperture(){m_camera.incrementAperture();}

    public Pair getShutterSpeed()
    {
        return modifier.getShutterSpeed();
    }

    public void adjustShutterSpeed(int val)
    {
        m_camera.adjustShutterSpeed(val);
    }
}
