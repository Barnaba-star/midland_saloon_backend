package com.midland.saloon.Uaa.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Where somebody's payroll money is sent.
 *
 * Both parts or neither: an account number with no bank cannot be paid to,
 * and a bank with no account number says nothing.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BankDetailsDTO {
    private String accountNumber;
    private String bankName;
}
