package com.mmall.pojo;

import lombok.*;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Category {
    private Integer id;

    private Integer parentId;

    private String name;

    private Boolean status;

    private Integer sortOrder;

    private Date createTime;

    private Date updateTime;






/**
 * 重写了equals和hashcode方法，认为只要id一样，两个category对象就一样，用于加入set集合
    @Override
    public boolean equals(Object o) {
        //同一个对象，true
        if (this == o) return true;
        //被比较对象为null，并且本类与null的类不同，fasle
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id != null ? id.equals(category.id) : category.id == null;

    }
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }*/
}