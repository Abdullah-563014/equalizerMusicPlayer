/*
 * Created by nguyenvietphu6794@gmail.com on 5/13/18 9:55 AM.
 * Copyright (c) 2018 nguyenvietphu6794@gmail.com. All rights reserved.
 */

package com.nvp.easypermissions;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public abstract class AbstractPermissionListener implements PermissionListener {
    @Override
    public void onPermissionGranted() {
    }

    @Override
    public void onPermissionDenied(@NonNull ArrayList<String> deniedPermissions) {
    }
}
