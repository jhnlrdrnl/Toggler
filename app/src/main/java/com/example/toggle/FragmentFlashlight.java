package com.example.toggle;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.IOException;

public class FragmentFlashlight extends Fragment {

    SwitchMaterial flashlightSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View flashlightView = inflater.inflate(R.layout.fragment_flashlight, container, false);

        flashlightSwitch = flashlightView.findViewById(R.id.flashlightSwitch);
        flashlightSwitch.setOnClickListener(view -> setFlashlightSwitch(flashlightSwitch.isChecked()));

        return flashlightView;
    }

    private void setFlashlightSwitch(boolean input) {
        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                CameraManager cameraManager = (CameraManager) getActivity().getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
                String cameraId;
                try {
                    cameraId = cameraManager.getCameraIdList()[0];
                    cameraManager.setTorchMode(cameraId, input);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
            else {
                Camera camera = Camera.open();
                Camera.Parameters p = camera.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(p);

                SurfaceTexture texture = new SurfaceTexture(0);
                try {
                    camera.setPreviewTexture(texture);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (input) {
                    camera.startPreview();
                }
                else {
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.stopPreview();
                    camera.release();
                }
            }
        }
        else {
            Toast.makeText(
                    getActivity(),
                    "No flashlight found on this device",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
