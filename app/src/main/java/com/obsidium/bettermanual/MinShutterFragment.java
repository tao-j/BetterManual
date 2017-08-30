package com.obsidium.bettermanual;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.killerink.ActivityInterface;
import com.github.killerink.KeyEvents;
import com.github.killerink.MainActivity;
import com.sony.scalar.hardware.CameraEx;

public class MinShutterFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, KeyEvents
{
    private SeekBar     m_sbShutter;
    private TextView    m_tvInfo;

    private ActivityInterface activityInterface;

    public static MinShutterFragment getMinShutterActivity(ActivityInterface activityInterface)
    {
        MinShutterFragment msa = new MinShutterFragment();
        msa.setActivityInterface(activityInterface);
        return msa;
    }


    public void setActivityInterface(ActivityInterface activityInterface) {
        this.activityInterface = activityInterface;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.min_shutter_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        m_sbShutter = (SeekBar) view.findViewById(R.id.sbShutter);
        m_sbShutter.setOnSeekBarChangeListener(this);
        m_sbShutter.setMax(CameraUtil.MIN_SHUTTER_VALUES.length - 1);

        m_tvInfo = (TextView) view.findViewById(R.id.tvInfo);

        Button btnOk = (Button) view.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activityInterface.loadFragment(MainActivity.FRAGMENT_CAMERA_UI);
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        CameraEx.ShutterSpeedInfo info = activityInterface.getCamera().getShutterSpeedInfo();
        setShutterSpeedToUi(info,true);
        activityInterface.getCamera().setShutterSpeedChangeListener(new CameraEx.ShutterSpeedChangeListener()
        {
            @Override
            public void onShutterSpeedChange(final CameraEx.ShutterSpeedInfo shutterSpeedInfo, CameraEx cameraEx)
            {
                setShutterSpeedToUi(shutterSpeedInfo,false);
            }
        });
    }

    private void setShutterSpeedToUi(CameraEx.ShutterSpeedInfo shutterSpeedInfo, boolean updateSeekbarProgress) {
        final int idx = CameraUtil.getShutterValueIndex(shutterSpeedInfo.currentAvailableMin_n, shutterSpeedInfo.currentAvailableMin_d);
        if (idx >= 0)
        {
            if (updateSeekbarProgress)
                m_sbShutter.setProgress(idx);
            m_tvInfo.setText(CameraUtil.formatShutterSpeed(shutterSpeedInfo.currentAvailableMin_n, shutterSpeedInfo.currentAvailableMin_d));

        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        // Save minimum shutter speed

        activityInterface.getPreferences().setMinShutterSpeed(activityInterface.getCamera().getAutoShutterSpeedLowLimit());
    }



    @Override
    public boolean onEnterKeyDown()
    {
        return true;
    }

    @Override
    public boolean onEnterKeyUp() {
        activityInterface.loadFragment(MainActivity.FRAGMENT_CAMERA_UI);
        return false;
    }

    @Override
    public boolean onFnKeyDown() {
        return false;
    }

    @Override
    public boolean onFnKeyUp() {
        return false;
    }

    @Override
    public boolean onAelKeyDown() {
        return false;
    }

    @Override
    public boolean onAelKeyUp() {
        return false;
    }

    @Override
    public boolean onMenuKeyDown() {
        return false;
    }

    @Override
    public boolean onMenuKeyUp() {
        return false;
    }

    @Override
    public boolean onFocusKeyDown() {
        return false;
    }

    @Override
    public boolean onFocusKeyUp() {
        return false;
    }

    @Override
    public boolean onShutterKeyDown() {
        return false;
    }

    @Override
    public boolean onShutterKeyUp() {
        return false;
    }

    @Override
    public boolean onPlayKeyDown() {
        return false;
    }

    @Override
    public boolean onPlayKeyUp() {
        return false;
    }

    @Override
    public boolean onMovieKeyDown() {
        return false;
    }

    @Override
    public boolean onMovieKeyUp() {
        return false;
    }

    @Override
    public boolean onC1KeyDown() {
        return false;
    }

    @Override
    public boolean onC1KeyUp() {
        return false;
    }

    @Override
    public boolean onLensAttached() {
        return false;
    }

    @Override
    public boolean onLensDetached() {
        return false;
    }

    @Override
    public boolean onModeDialChanged(int value) {
        return false;
    }

    @Override
    public boolean onZoomTeleKey() {
        return false;
    }

    @Override
    public boolean onZoomWideKey() {
        return false;
    }

    @Override
    public boolean onZoomOffKey() {
        return false;
    }

    @Override
    public boolean onDeleteKeyDown() {
        return false;
    }

    @Override
    public boolean onDeleteKeyUp() {
        return false;
    }

    @Override
    public boolean onUpperDialChanged(int value)
    {
        m_sbShutter.incrementProgressBy(value);
        activityInterface.getCamera().setAutoShutterSpeedLowLimit(CameraUtil.MIN_SHUTTER_VALUES[m_sbShutter.getProgress()]);
        return true;
    }

    @Override
    public boolean onLowerDialChanged(int value) {
        return false;
    }

    @Override
    public boolean onUpKeyDown() {
        return false;
    }

    @Override
    public boolean onUpKeyUp() {
        return false;
    }

    @Override
    public boolean onDownKeyDown() {
        return false;
    }

    @Override
    public boolean onDownKeyUp() {
        return false;
    }

    @Override
    public boolean onLeftKeyDown() {
        return false;
    }

    @Override
    public boolean onLeftKeyUp() {
        return false;
    }

    @Override
    public boolean onRightKeyDown() {
        return false;
    }

    @Override
    public boolean onRightKeyUp() {
        return false;
    }

    /* OnSeekBarChangeListener */
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        if (fromUser)
        {
            activityInterface.getCamera().setAutoShutterSpeedLowLimit(CameraUtil.MIN_SHUTTER_VALUES[progress]);
        }
    }
    public void onStartTrackingTouch(SeekBar var1)
    {
    }
    public void onStopTrackingTouch(SeekBar var1)
    {
    }

}
