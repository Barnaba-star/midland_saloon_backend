package com.midland.saloon.Saloon.Dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SaloonStaffDTO {
    private String uid;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String saloonServiceUID;
    private String saloonCategory;
    private String description;
    private String gender;
    private String StoreOpenUID;
}
