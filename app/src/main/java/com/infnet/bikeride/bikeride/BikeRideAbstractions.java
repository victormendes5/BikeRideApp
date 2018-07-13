package com.infnet.bikeride.bikeride;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BikeRideAbstractions {

    Activity activity;

    public BikeRideAbstractions (Activity context) {
        this.activity = context;
    }

    public void connectVariableToViewIdAndOnClickMethod(Object... data) {

        for (int i = 0; i<data.length; i += 3) {

            String        vName           = (String) data[i];
            int           viewId          = (int) data[i+1];
            final String  methodToInvoke  = (String) data[i+2];

            try {
                Field field = activity.getClass().getDeclaredField(vName);
                field.setAccessible(true);
                field.set(activity, this.activity.findViewById(viewId));
            }

            catch (NoSuchFieldException e) {
                e.printStackTrace();
            }

            catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            View v = this.activity.findViewById(viewId);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (methodToInvoke.equals("")) return;

                    try {
                        Method sentMethod = activity.getClass().getDeclaredMethod(methodToInvoke);
                        sentMethod.setAccessible(true);
                        sentMethod.invoke(activity);
                    }

                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
