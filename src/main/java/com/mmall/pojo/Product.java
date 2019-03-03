package com.mmall.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private Integer id;

    private Integer categoryId;

    private String name;

    private String subtitle;

    private String mainImage;

    private String subImages;

    private String detail;

    private BigDecimal price;

    private Integer stock;

    private Integer status;

    private Date createTime;

    private Date updateTime;





//    public void setName(String name) {
//        this.name = name == null ? null : name.trim();
//    }
//
//
//    public void setSubtitle(String subtitle) {
//        this.subtitle = subtitle == null ? null : subtitle.trim();
//    }
//
//
//    public void setMainImage(String mainImage) {
//        this.mainImage = mainImage == null ? null : mainImage.trim();
//    }
//
//
//    public void setSubImages(String subImages) {
//        this.subImages = subImages == null ? null : subImages.trim();
//    }
//
//
//    public void setDetail(String detail) {
//        this.detail = detail == null ? null : detail.trim();
//    }

}