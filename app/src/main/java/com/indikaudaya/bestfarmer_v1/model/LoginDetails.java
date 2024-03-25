package com.indikaudaya.bestfarmer_v1.model;

import com.indikaudaya.bestfarmer_v1.dto.ProductDTO;
import com.indikaudaya.bestfarmer_v1.dto.UserDTO;

public class LoginDetails {

    public static boolean isSigning;
    public static boolean isSeller;

    private static UserDTO userDTO;
    private static FirebaseUserModel firebaseUserModel;

    public LoginDetails(UserDTO userDTO) {
        LoginDetails.userDTO = userDTO;
    }

    public LoginDetails() {
    }

    public static FirebaseUserModel getFirebaseUserModel() {
        return firebaseUserModel;
    }

    public static void setFirebaseUserModel(FirebaseUserModel firebaseUserModel) {
        LoginDetails.firebaseUserModel = firebaseUserModel;
    }

    public UserDTO getUserDTO() {
        return userDTO;
    }

}
