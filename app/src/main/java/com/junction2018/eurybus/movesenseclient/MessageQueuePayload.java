package com.junction2018.eurybus.movesenseclient;

import com.google.gson.Gson;

public class MessageQueuePayload {

    public MathUtils.Vector ArrayVec;
    public Long Timestamp;
    public float TotalVec;

    @Override
    public String toString() {
        calculateTotal();
        return "MessageQueuePayload{" +
                "ArrayVec=" + ArrayVec +
                ", timestamp=" + Timestamp +
                '}';
    }

    public MessageQueuePayload(MathUtils.Vector arrayVec, Long timestamp) {
        ArrayVec = arrayVec;
        this.Timestamp = timestamp;
    }

    private void calculateTotal() {
        this.TotalVec = ArrayVec.x + ArrayVec.y + ArrayVec.z;
    }
    public String toJSON(){
        calculateTotal();
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
