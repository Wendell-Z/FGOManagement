package com.fgo.management.model.progress;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class SignIn extends BoostingProgress {

    private int days;

    private int daysOfSignedIn;

    private String lastSignInTime;

}
