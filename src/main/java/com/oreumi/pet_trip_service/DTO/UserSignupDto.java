package com.oreumi.pet_trip_service.DTO;

import lombok.Data;

@Data
public class UserSignupDto {
    
    private String email;
    private String password;
    private String passwordConfirm;
    private String nickname;
    
}