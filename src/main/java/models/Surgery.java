package models;

import java.util.Objects;

public class Surgery {

    String code;
    Float lat;
    Float lon;
    String postCode;
    Integer listSize;

    public Surgery() {}

    public Surgery(String code, String postCode, int listSize, float lat, float lon) {
        this.code = code;
        this.postCode = postCode;
        this.listSize = listSize;
        this.lat = lat;
        this.lon = lon;
    }

    public static class SurgeryBuilder {

        Surgery surgery = new Surgery();

        public SurgeryBuilder(String code) {
            surgery.code = code;
        }

        public SurgeryBuilder withPostCode(String postCode) {
            surgery.postCode = postCode;
            return this;
        }

        public SurgeryBuilder withLat(Float lat) {
            surgery.lat = lat;
            return this;
        }

        public SurgeryBuilder withLon(Float lon) {
            surgery.lon = lon;
            return this;
        }

        public SurgeryBuilder withListSize(Integer listSize) {
            surgery.listSize = listSize;
            return this;
        }

        public Surgery build() {
            Objects.requireNonNull(surgery.code);
            Objects.requireNonNull(surgery.postCode);
            Objects.requireNonNull(surgery.lat);
            Objects.requireNonNull(surgery.lon);
            Objects.requireNonNull(surgery.listSize);
            return surgery;
        }
    }

    public String getCode() {
        return code;
    }

    public Surgery setCode(String code) {
        this.code = code;
        return this;
    }

    public float getLat() {
        return lat;
    }

    public Surgery setLat(float lat) {
        this.lat = lat;
        return this;
    }

    public float getLon() {
        return lon;
    }

    public Surgery setLon(float lon) {
        this.lon = lon;
        return this;
    }

    public String getPostCode() {
        return postCode;
    }

    public Surgery setPostCode(String postCode) {
        this.postCode = postCode;
        return this;
    }

    public int getListSize() {
        return listSize;
    }

    public Surgery setListSize(int listSize) {
        this.listSize = listSize;
        return this;
    }
}
