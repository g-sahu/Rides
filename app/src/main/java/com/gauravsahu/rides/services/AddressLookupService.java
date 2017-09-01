package com.gauravsahu.rides.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.gauravsahu.rides.utilities.Constants;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by gasahu on 29-Jul-17.
 */

public class AddressLookupService extends IntentService {
    protected ResultReceiver addressReceiver;

    public AddressLookupService() {
        super("AddressLookupService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        List<Address> addresses;
        Address address;
        String comma = ", ", addressLine1, addressLine2, finalAddress = "";

        try {
            LatLng lastLocation = intent.getParcelableExtra(Constants.KEY_LAST_LOCATION);
            addressReceiver = intent.getParcelableExtra(Constants.KEY_RECEIVER);
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            addresses = geocoder.getFromLocation(lastLocation.latitude, lastLocation.longitude, 1);

            address = addresses.get(0);
            int addressLines = address.getMaxAddressLineIndex();

            if(addressLines > 1) {
                addressLine1 = address.getAddressLine(0);
                addressLine2 = address.getAddressLine(1);

                if(addressLine1 != null && !addressLine1.isEmpty()) {
                    finalAddress = finalAddress + addressLine1 + comma;
                } else if(!finalAddress.isEmpty()) {
                    finalAddress = finalAddress + comma;
                }

                if(addressLine2 != null && !addressLine2.isEmpty()) {
                    finalAddress = finalAddress + addressLine2;
                }
            }

            //Sending constructed address to AddressLookupResultReceiver
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_RESULT_DATA, finalAddress);
            addressReceiver.send(Constants.KEY_SUCCESS, bundle);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
