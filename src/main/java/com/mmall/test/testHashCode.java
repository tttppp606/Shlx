package com.mmall.test;

import com.mmall.pojo.Category;

/**
 * Created by tttppp606 on 2019/1/29.
 */
public class testHashCode {
    public static void main(String[] args) {
        Category category = new Category();

        Category category1 = new Category();

        System.out.println(category.equals(category1));

    }
}
