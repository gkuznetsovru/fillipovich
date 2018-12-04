package ru.ovod.foto2;

import java.util.Date;

public class Inspection {
    private Integer _inspectionid;
    private Integer number;
    private Integer orderid;
    private Integer issync;
    private String date;
    private String model;
    private String vin;
    private Integer photoCo;


    public Inspection(Integer _inspectionid, Integer number, Integer orderid, Integer issync, String date, String model, String vin) {
        this._inspectionid = _inspectionid;
        this.number = number;
        this.orderid = orderid;
        this.issync = issync;
        this.date = date;
        this.model = model;
        this.vin = vin;
    }

    public Integer get_inspectionid() {
        return _inspectionid;
    }

    public void set_inspectionid(Integer _inspectionid) {
        this._inspectionid = _inspectionid;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getOrderid() {
        return orderid;
    }

    public void setOrderid(Integer orderid) {
        this.orderid = orderid;
    }

    public Integer getIssync() {
        return issync;
    }

    public void setIssync(Integer issync) {
        this.issync = issync;
    }

    public String getDate() { return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Integer getPhotoCo() {
        return photoCo;
    }

    public void setPhotoCo(Integer photoCo) {
        this.photoCo = photoCo;
    }
}
