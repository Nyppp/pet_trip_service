package com.oreumi.pet_trip_service.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSignupDTO {
    
    private String email;
    private String password;
    private String passwordConfirm;
    private String nickname;
    
}