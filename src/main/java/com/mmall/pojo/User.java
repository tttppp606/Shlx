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
public class User {
    private Integer id;

    private String username;

    private String password;

    private String email;

    private String phone;

    private String question;

    private String answer;

    private Integer role;

    private Date createTime;

    private Date updateTime;


//    public void setUsername(String username) {
//        this.username = username == null ? null : username.trim();
//    }
//
//    public void setPassword(String password) {
//        this.password = password == null ? null : password.trim();
//    }
//
//    public void setEmail(String email) {
//        this.email = email == null ? null : email.trim();
//    }
//
//    public void setPhone(String phone) {
//        this.phone = phone == null ? null : phone.trim();
//    }
//
//    public void setQuestion(String question) {
//        this.question = question == null ? null : question.trim();
//    }
//
//    public void setAnswer(String answer) {
//        this.answer = answer == null ? null : answer.trim();
//    }
}