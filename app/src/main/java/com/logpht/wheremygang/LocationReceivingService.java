package com.logpht.wheremygang;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Long on 28/04/2018.
 */

public class LocationReceivingService extends LocationServices {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }
}
