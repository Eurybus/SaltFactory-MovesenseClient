package com.junction2018.eurybus.movesenseclient;

import com.google.gson.annotations.SerializedName;


public class StateDataResponse {

    @SerializedName("Body")
    public final Body body;

    public StateDataResponse(Body body) {this.body = body; }

    public static class Body {
        @SerializedName("Timestamp")
        public final long timestamp;

        @SerializedName("StateId")
        public final int stateId;

        @SerializedName("NewState")
        public final int newState;

        @SerializedName("Headers")
        public final Headers header;

        public Body(long timestamp, int stateId, int newState, Headers header){
            this.timestamp = timestamp;
            this.stateId = stateId;
            this.newState = newState;
            this.header = header;
        }
    }

    public static class Headers {

    }
}
