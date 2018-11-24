package com.junction2018.eurybus.movesenseclient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

public class MathUtils {

    public static Vector CalculateTotalAcceleration(List<Vector> inputs) {
        Vector result = new Vector();
        for (Vector element: inputs) {
            result.x += Math.sqrt(Math.abs(element.x));
            result.y += Math.sqrt(Math.abs(element.y));
            result.z += Math.sqrt(Math.abs(element.z));
            //result.y += Math.abs(element.y);
        }
        return result;
    }

    public static class Vector {
        public float x;
        public float y;
        public float z;
        public float g;

        @Override
        public String toString() {
            return x + ", " + y +", " + z;
        }

        public Vector(){
            this.x = 0f;
            this.y = 0f;
            this.z = 0f;
            this.g = 0f;
        }

        // Ugly hack to prevent Gyro error marginals appearing in application data
        public Vector(float x, float y, float z) {
            if (Math.abs(x) > 15 && Math.abs(y) > 15 && Math.abs(z) > 15){
                this.x = x;
                this.y = y;
                this.z = z;
            }
            else {
                this.x = 0;
                this.y = 0;
                this.z = 0;
            }
        }

        public String toJSON(){
            Gson gson = new Gson();
            return gson.toJson(this);
        }

    }
}
