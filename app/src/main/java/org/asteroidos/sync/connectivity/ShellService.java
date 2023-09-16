/*
 * AsteroidOSSync
 * Copyright (c) 2023 AsteroidOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.asteroidos.sync.connectivity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import org.asteroidos.sync.MainActivity;
import org.asteroidos.sync.R;
import org.asteroidos.sync.asteroid.IAsteroidDevice;
import org.asteroidos.sync.fragments.ShellFragment;
import org.asteroidos.sync.services.SynchronizationService;
import org.asteroidos.sync.utils.AsteroidUUIDS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ShellService implements IConnectivityService {
    private static final String NOTIFICATION_CHANNEL_ID = "shellservice_channel_id_01";
    private static final String TAG = "ShellService";
    private final Context mCtx;
    private final IAsteroidDevice mDevice;
    private final SynchronizationService mSynchronizationService;
    private ShellTermReceiver mSReceiver;

    public ShellService(Context ctx, IAsteroidDevice device) {
        mDevice = device;
        mCtx = ctx;

        mSynchronizationService = (SynchronizationService)device;
        device.registerCallback(AsteroidUUIDS.SHELL_TERMINAL_RECV, data -> {
            if (data == null) return;
            mSynchronizationService.handleShellOutput(data);
        });
    }

    @Override
    public void sync() {
        if (mSReceiver == null) {
            {
                mSReceiver = new ShellTermReceiver();
                IntentFilter filter = new IntentFilter();
                filter.addAction("org.asteroidos.sync.SHELL_TERM_LISTENER");
                mCtx.registerReceiver(mSReceiver, filter);
            }
        }
    }

    @Override
    public void unsync() {
        if (mSReceiver != null) {
            try {
                mCtx.unregisterReceiver(mSReceiver);
            } catch (IllegalArgumentException ignored) {
            }
            mSReceiver = null;
        }
    }

    @Override
    public HashMap<UUID, Direction> getCharacteristicUUIDs() {
        HashMap<UUID, Direction> chars = new HashMap<>();
        chars.put(AsteroidUUIDS.SHELL_TERMINAL_SEND, Direction.TO_WATCH);
        chars.put(AsteroidUUIDS.SHELL_TERMINAL_RECV, Direction.FROM_WATCH);
        return chars;
    }

    @Override
    public UUID getServiceUUID() {
        return AsteroidUUIDS.SHELL_SERVICE_UUID;
    }

    class ShellTermReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra("data");
            mDevice.send(AsteroidUUIDS.SHELL_TERMINAL_SEND, data, ShellService.this);
        }
    }
}
