package com.infnet.bikeride.bikeride.constants;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.infnet.bikeride.bikeride.R;

public final class Constants {

    // ---> Firebase Child Names
    public final class ChildName {

        public static final String REQUESTS = "Requests";
        public static final String DELIVERIES = "Deliveries";
        public static final String AVAILABLE_BIKERS = "AvailableBikers";
    }

    // ---> View Ids
    public final class ViewId {

        public static final int REQUESTS_LIST = R.id.newRequestsList;
        public static final int MAP = R.id.map;

        public static final int NAVIGATION_DRAWER_GROUPVIEW_ID = R.id.main_drawer_groupview;
        public static final int CUSTOM_TOOLBAR_ID = R.id.customToolbar;
    }

    // ---> Layout Ids
    public final class LayoutId {

        public static final int NAVIGATION_DRAWER_LAYOUT_FILE_ID = R.layout.main_drawer_layout;
    }

    // ---> Timeouts
    public final class Timeouts {

        public static final int REQUEST_LONG  = 20000;
        public static final int REQUEST_SHORT = 10000;

    }

    // ---> Keys
    public final class Keys {

        public static final String GOOGLE_API = "AIzaSyBNHqa3hUDjRRmSz7vW4t_3q4eE34JMTH8";
    }

    // ---> LatLng Boundaries
    public final class Boundaries {

        public final LatLngBounds BRAZIL = new LatLngBounds(
                new LatLng(-33.391381, -72.187674),
                new LatLng(4.438078, -32.812675));
    }

    // ---> Mocked user / biker name
    public final class MockedIds {

        public static final String User = "CurrentRequestingUser";
        public static final String Biker = "CurrentRequestingBiker";
    }

}
