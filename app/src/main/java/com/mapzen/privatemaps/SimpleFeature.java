package com.mapzen.privatemaps;

import com.mapzen.pelias.gson.Feature;

import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.MarkerItem;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class SimpleFeature implements Parcelable {
    public static final String TEXT = "text";
    public static final String TYPE = "type";
    public static final String ID = "id";
    public static final String ALPHA3 = "alpha3";
    public static final String LOCALITY = "locality";
    public static final String NEIGHBORHOOD = "neighborhood";
    public static final String COUNTRY_NAME = "admin0";
    public static final String ADMIN1_ABBR = "admin1_abbr";
    public static final String ADMIN1 = "admin1";
    public static final String ADMIN2 = "admin2";
    public static final String LOCAL_ADMIN = "local_admin";
    public static final Parcelable.Creator<SimpleFeature> CREATOR =
            new Parcelable.Creator<SimpleFeature>() {
                @Override
                public SimpleFeature[] newArray(int size) {
                    return new SimpleFeature[size];
                }

                public SimpleFeature createFromParcel(Parcel in) {
                    return SimpleFeature.readFromParcel(in);
                }
            };
    private HashMap<String, String> properties = new HashMap<String, String>();
    private double lat, lon;
    private String hint;

    public static SimpleFeature readFromParcel(Parcel in) {
        SimpleFeature simpleFeature = new SimpleFeature();
        simpleFeature.setLat(in.readDouble());
        simpleFeature.setLon(in.readDouble());
        simpleFeature.setProperty(TEXT, in.readString());
        simpleFeature.setProperty(TYPE, in.readString());
        simpleFeature.setProperty(ID, in.readString());
        simpleFeature.setProperty(ALPHA3, in.readString());
        simpleFeature.setProperty(COUNTRY_NAME, in.readString());
        simpleFeature.setProperty(ADMIN1_ABBR, in.readString());
        simpleFeature.setProperty(ADMIN1, in.readString());
        simpleFeature.setProperty(LOCAL_ADMIN, in.readString());
        simpleFeature.setProperty(NEIGHBORHOOD, in.readString());
        simpleFeature.setProperty(LOCALITY, in.readString());
        simpleFeature.setProperty(ADMIN2, in.readString());
        simpleFeature.setHint(in.readString());
        return simpleFeature;
    }

    public static SimpleFeature fromFeature(Feature feature) {
        SimpleFeature simpleFeature = new SimpleFeature();
        simpleFeature.setProperty(TEXT, feature.getProperties().getText());
        simpleFeature.setProperty(TYPE, feature.getProperties().getType());
        simpleFeature.setProperty(ID, feature.getProperties().getId());
        simpleFeature.setProperty(ADMIN1, feature.getProperties().getAdmin1());
        simpleFeature.setProperty(ADMIN1_ABBR, feature.getProperties().getAdmin1Abbr());
        simpleFeature.setProperty(LOCAL_ADMIN, feature.getProperties().getLocalAdmin());
        simpleFeature.setProperty(NEIGHBORHOOD, feature.getProperties().getNeighborhood());
        simpleFeature.setProperty(LOCALITY, feature.getProperties().getLocality());
        simpleFeature.setProperty(ADMIN2, feature.getProperties().getAdmin2());
        simpleFeature.setLon(feature.getGeometry().getCoordinates().get(0));
        simpleFeature.setLat(feature.getGeometry().getCoordinates().get(1));
        simpleFeature.setHint(feature.getProperties().getText());
        return simpleFeature;
    }

    @Override
    public String toString() {
        return "'" + getProperty(TEXT) + "'[" + getLat() + ", " + getLon() + "]";
    }

    public String getFullLocationString() {
        return " " + getProperty(TEXT) + " [" + getLat() + ", " + getLon() + "]";
    }

    public MarkerItem getMarker() {
        return new MarkerItem(getProperty(ID),
                getTitle(),
                getAddress(),
                new GeoPoint(getLat(), getLon()));
    }

    public GeoPoint getGeoPoint() {
        return new GeoPoint(getLat(), getLon());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(getLat());
        out.writeDouble(getLon());
        out.writeString(getProperty(TEXT));
        out.writeString(getProperty(TYPE));
        out.writeString(getProperty(ID));
        out.writeString(getProperty(ALPHA3));
        out.writeString(getProperty(COUNTRY_NAME));
        out.writeString(getProperty(ADMIN1_ABBR));
        out.writeString(getProperty(ADMIN1));
        out.writeString(getProperty(LOCAL_ADMIN));
        out.writeString(getProperty(NEIGHBORHOOD));
        out.writeString(getProperty(LOCALITY));
        out.writeString(getProperty(ADMIN2));
        out.writeString(getHint());
    }

    public Parcel toParcel() {
        Parcel parcel = Parcel.obtain();
        writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        return parcel;
    }

    @Override
    public boolean equals(Object o) {
        SimpleFeature other = (SimpleFeature) o;
        return getLat() == other.getLat()
                && getLon() == other.getLon()
                && getHint() == other.getHint()
                && getProperty(TEXT).equals(other.getProperty(TEXT));
    }

    public int hashCode() {
        return 0;
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getAdmin() {
        if (getProperty(ADMIN1_ABBR) != null) {
            return getProperty(ADMIN1_ABBR);
        } else if (getProperty(ADMIN1) != null) {
            return getProperty(ADMIN1);
        } else if (getProperty(ALPHA3) != null) {
            return getProperty(ALPHA3);
        } else {
            return "";
        }
    }

    public String getCity() {
        if (getProperty(LOCAL_ADMIN) != null) {
            return getProperty(LOCAL_ADMIN);
        } else if (getProperty(LOCALITY) != null) {
            return getProperty(LOCALITY);
        } else if (getProperty(NEIGHBORHOOD) != null) {
            return getProperty(NEIGHBORHOOD);
        } else if (getProperty(ADMIN2) != null) {
            return getProperty(ADMIN2);
        } else {
            return "";
        }
    }

    public String getSingleLine() {
        return getProperty(TEXT) + ", " + getCity() + ", " + getAdmin();
    }

    public String getTitle() {
        return getProperty(TEXT);
    }

    public String getAddress() {
        return String.format("%s, %s", getCity(), getAdmin());
    }
}
