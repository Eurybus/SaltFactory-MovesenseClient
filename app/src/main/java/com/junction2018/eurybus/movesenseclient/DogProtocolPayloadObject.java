package com.junction2018.eurybus.movesenseclient;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class DogProtocolPayloadObject {
    @SerializedName("will_to_live")
    int picture_id;
    @SerializedName("service_allowed")
    boolean service_allowed;

    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public String toString() {
        return "DogProtocolPayloadObject{" +
                "picture_id=" + picture_id +
                ", service_allowed=" + service_allowed +
                '}';
    }
}
