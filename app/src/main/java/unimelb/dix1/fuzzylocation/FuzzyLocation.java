package unimelb.dix1.fuzzylocation;

import android.util.Log;


public class FuzzyLocation {
    private Double fuzzyLatitude;
    private Double fuzzyLongitude;
    private final Double KM_PER_DEGREE = 111.0;

    public FuzzyLocation(){
        super();
        fuzzyLatitude = 0.0;
        fuzzyLongitude = 0.0;
    }
    //max offset is the privacy level
    // in a rectangle
    public void doFuzzy(Double latitude, Double longitude,int offsetDistance ){
        Double Latitude_Offset_Degree = offsetDistance / KM_PER_DEGREE;
        Double Longitude_Offset_Degree = offsetDistance / (KM_PER_DEGREE*Math.cos(latitude));
        fuzzyLatitude = latitude - Latitude_Offset_Degree + (Latitude_Offset_Degree*Math.random()*2);
        fuzzyLongitude = longitude - Longitude_Offset_Degree + (Longitude_Offset_Degree*Math.random()*2);
    }

    //in a ring
    public void doFuzzy2(Double latitude, Double longitude,int maxOffset ){
        double alpah = Math.random()*360.0;
        //Log.d("fuzzy2","alpha"+String.valueOf(alpah));
        int minOffset = maxOffset - 1;
        double padding = 0.15;
        //randomOffset is between minOffset and MaxOffset
        double randomOffset = minOffset + padding + Math.random();

        Double Latitude_Offset_Degree = randomOffset*Math.sin(alpah)/ KM_PER_DEGREE;
        Double Longitude_Offset_Degree = randomOffset*Math.cos(alpah)/(KM_PER_DEGREE*Math.cos(latitude));
        fuzzyLatitude = latitude + Latitude_Offset_Degree;
        fuzzyLongitude = longitude + Longitude_Offset_Degree;

//        Log.d("fuzzy2","random offset"+String.valueOf(randomOffset));
//        Log.d("fuzzy2","cos "+String.valueOf(Math.cos(alpah)));
//        String latitude1 =  String.valueOf(fuzzyLatitude);
//        String longitude1 = String.valueOf(fuzzyLongitude);
//        String txt = "La:"+latitude1+"; Lo:"+longitude1;
//        Log.d("fuzzy2",txt);
    }

    public Double getFuzzyLongitude() {
        return fuzzyLongitude;
    }

    public Double getFuzzyLatitude() {
        return fuzzyLatitude;
    }
}
