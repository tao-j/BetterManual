package com.obsidium.bettermanual;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.killerink.ActivityInterface;
import com.github.killerink.KeyEvents;
import com.github.killerink.MainActivity;
import com.obsidium.bettermanual.views.PreviewNavView;
import com.sony.scalar.hardware.CameraEx;

import java.util.List;


public class PreviewMagnificationFragment extends Fragment implements KeyEvents, CameraEx.PreviewMagnificationListener {

    private class SurfaceSwipeTouchListener extends OnSwipeTouchListener
    {
        public SurfaceSwipeTouchListener(Context context)
        {
            super(context);
        }

        @Override
        public boolean onScrolled(float distanceX, float distanceY)
        {
            if (m_curPreviewMagnification != 0)
            {
                m_curPreviewMagnificationPos = new Pair<Integer, Integer>(Math.max(Math.min(m_curPreviewMagnificationMaxPos, m_curPreviewMagnificationPos.first + (int)distanceX), -m_curPreviewMagnificationMaxPos),
                        Math.max(Math.min(m_curPreviewMagnificationMaxPos, m_curPreviewMagnificationPos.second + (int)distanceY), -m_curPreviewMagnificationMaxPos));
                activityInterface.getCamera().setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
                return true;
            }
            return false;
        }
    }

    private final String TAG = PreviewMagnificationFragment.class.getSimpleName();

    private ActivityInterface activityInterface;
    private TextView magnification;
    private PreviewNavView previewNavView;

    // Preview magnification
    private List<Integer> m_supportedPreviewMagnifications;
    private boolean         m_zoomLeverPressed;
    private int             m_curPreviewMagnification;
    private float           m_curPreviewMagnificationFactor;
    private Pair<Integer, Integer> m_curPreviewMagnificationPos = new Pair<Integer, Integer>(0, 0);
    private int             m_curPreviewMagnificationMaxPos;

    public static PreviewMagnificationFragment getFragment(ActivityInterface activityInterface)
    {
        PreviewMagnificationFragment cu = new PreviewMagnificationFragment();
        cu.activityInterface = activityInterface;
        return cu;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");
        return inflater.inflate(R.layout.preview_magnification_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        magnification = (TextView) view.findViewById(R.id.tvMagnification);
        previewNavView = (PreviewNavView)view.findViewById(R.id.vPreviewNav);
    }

    @Override
    public void onResume() {
        super.onResume();
        activityInterface.setSurfaceViewOnTouchListner(new SurfaceSwipeTouchListener(getContext()));
        activityInterface.getCamera().setPreviewMagnificationListener(this);
        m_supportedPreviewMagnifications = (List<Integer>) activityInterface.getCamera().getSupportedPreviewMagnification();
        Log.d(TAG,m_supportedPreviewMagnifications.toString());
    }

    @Override
    public void onPause() {
        super.onPause();
        activityInterface.setSurfaceViewOnTouchListner(null);
    }

    /*private void togglePreviewMagnificationViews(final boolean magnificationActive)
    {
        activityInterface.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                m_previewNavView.setVisibility(magnificationActive ? View.VISIBLE : View.GONE);
                m_tvMagnification.setVisibility(magnificationActive ? View.VISIBLE : View.GONE);
                m_vHist.setVisibility(magnificationActive ? View.GONE : View.VISIBLE);
                setLeftViewVisibility(!magnificationActive);
            }
        });

    }*/

    //##############CameraEx.PreviewMagnificationListener###############
    @Override
    public void onChanged(boolean enabled, int magFactor, int magLevel, Pair coords, CameraEx cameraEx) {
        Log.d(TAG,"onChanged enabled:" + String.valueOf(enabled) + " magFactor:" + String.valueOf(magFactor) + " magLevel:" +
                String.valueOf(magLevel) + " x:" + coords.first + " y:" + coords.second + "\n");
        //*
        if (enabled)
        {
            //log("m_curPreviewMagnificationMaxPos: " + String.valueOf(m_curPreviewMagnificationMaxPos) + "\n");
            m_curPreviewMagnification = magLevel;
            m_curPreviewMagnificationFactor = ((float)magFactor / 100.0f);
            m_curPreviewMagnificationMaxPos = 1000 - (int)(1000.0f / m_curPreviewMagnificationFactor);
            magnification.setText(String.format("\uE012 %.2fx", (float)magFactor / 100.0f));
            previewNavView.update(coords, m_curPreviewMagnificationFactor);
        }
        else
        {
            previewNavView.update(null, 0);
            m_curPreviewMagnification = 0;
            m_curPreviewMagnificationMaxPos = 0;
            m_curPreviewMagnificationFactor = 0;
        }
    }

    @Override
    public void onInfoUpdated(boolean b, Pair pair, CameraEx cameraEx) {

    }

    private void movePreviewVertical(int delta)
    {
        int newY = m_curPreviewMagnificationPos.second + delta;
        if (newY > m_curPreviewMagnificationMaxPos)
            newY = m_curPreviewMagnificationMaxPos;
        else if (newY < -m_curPreviewMagnificationMaxPos)
            newY = -m_curPreviewMagnificationMaxPos;
        m_curPreviewMagnificationPos = new Pair<Integer, Integer>(m_curPreviewMagnificationPos.first, newY);
        activityInterface.getCamera().setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
    }

    private void movePreviewHorizontal(int delta)
    {
        int newX = m_curPreviewMagnificationPos.first + delta;
        if (newX > m_curPreviewMagnificationMaxPos)
            newX = m_curPreviewMagnificationMaxPos;
        else if (newX < -m_curPreviewMagnificationMaxPos)
            newX = -m_curPreviewMagnificationMaxPos;
        m_curPreviewMagnificationPos = new Pair<Integer, Integer>(newX, m_curPreviewMagnificationPos.second);
        activityInterface.getCamera().setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
    }

    @Override
    public boolean onUpperDialChanged(int value) {
        return false;
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
        if (m_curPreviewMagnification != 0)
        {
            movePreviewVertical((int)(-500.0f / m_curPreviewMagnificationFactor));
            return true;
        }
        return false;
    }

    @Override
    public boolean onDownKeyDown() {
        return false;
    }

    @Override
    public boolean onDownKeyUp() {
        if (m_curPreviewMagnification != 0)
            movePreviewVertical((int)(500.0f / m_curPreviewMagnificationFactor));
        return false;
    }

    @Override
    public boolean onLeftKeyDown() {
        return false;
    }

    @Override
    public boolean onLeftKeyUp() {
        if (m_curPreviewMagnification != 0)
        {
            movePreviewHorizontal((int)(-500.0f / m_curPreviewMagnificationFactor));
            return true;
        }
        return false;
    }

    @Override
    public boolean onRightKeyDown() {
        return false;
    }

    @Override
    public boolean onRightKeyUp() {
        if (m_curPreviewMagnification != 0)
        {
            movePreviewHorizontal((int)(500.0f / m_curPreviewMagnificationFactor));
            return true;
        }
        return false;
    }

    @Override
    public boolean onEnterKeyDown() {
        return false;
    }

    @Override
    public boolean onEnterKeyUp() {
        if (m_curPreviewMagnification == 200)
        {
            m_curPreviewMagnification = 0;
            m_curPreviewMagnificationPos = new Pair<Integer, Integer>(0, 0);
        }
        else if (m_curPreviewMagnification == 0)
        {
            m_curPreviewMagnification = 100;
        }
        else if (m_curPreviewMagnification == 100)
            m_curPreviewMagnification = 200;
        activityInterface.getCamera().setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
        return false;
    }

    @Override
    public boolean onFnKeyDown() {
        return false;
    }

    @Override
    public boolean onFnKeyUp() {
        activityInterface.getCamera().stopPreviewMagnification();
        activityInterface.loadFragment(MainActivity.FRAGMENT_CAMERA_UI);
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
}
