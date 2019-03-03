package com.mmall.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Shipping {
    private Integer id;

    private Integer userId;

    private String receiverName;

    private String receiverPhone;

    private String receiverMobile;

    private String receiverProvince;

    private String receiverCity;

    private String receiverDistrict;

    private String receiverAddress;

    private String receiverZip;

    private Date createTime;

    private Date updateTime;

//    public void setReceiverPhone(String receiverPhone) {
//        this.receiverPhone = receiverPhone == null ? null : receiverPhone.trim();
//    }
//    public void setReceiverMobile(String receiverMobile) {
//        this.receiverMobile = receiverMobile == null ? null : receiverMobile.trim();
//    }
//    public void setReceiverProvince(String receiverProvince) {
//        this.receiverProvince = receiverProvince == null ? null : receiverProvince.trim();
//    }
//    public void setReceiverCity(String receiverCity) {
//        this.receiverCity = receiverCity == null ? null : receiverCity.trim();
//    }
//    public void setReceiverDistrict(String receiverDistrict) {
//        this.receiverDistrict = receiverDistrict == null ? null : receiverDistrict.trim();
//    }
//    public void setReceiverAddress(String receiverAddress) {
//        this.receiverAddress = receiverAddress == null ? null : receiverAddress.trim();
//    }
//    public void setReceiverZip(String receiverZip) {
//        this.receiverZip = receiverZip == null ? null : receiverZip.trim();
//    }
}