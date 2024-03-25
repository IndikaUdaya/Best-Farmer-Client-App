package com.indikaudaya.bestfarmer_v1.dto;

public class SignupDTO {

    private String email;
    private String mobile;
    private String password;
    private boolean status;
    private String  userType;

    public SignupDTO() {
    }

    public SignupDTO(String email, String mobile, String password,boolean status,String userType) {
        this.email = email;
        this.mobile = mobile;
        this.password = password;
        this.status=status;
        this.userType=userType;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
