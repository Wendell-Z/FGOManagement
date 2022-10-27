package com.fgo.management.model;

import lombok.*;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserToken {

    @EqualsAndHashCode.Include
    private String account;

    @EqualsAndHashCode.Include
    private String token;

    @EqualsAndHashCode.Exclude
    private long loginTime;

    public UserToken(String account, String token) {
        this.account = account;
        this.token = token;
    }
}
