package com.jobportal.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "blacklistedTokens")
public class BlacklistedToken {

    @Id
    private String token;
    private Date expiryDate;

    public BlacklistedToken() {}

    public BlacklistedToken(String token, Date expiryDate) {
        this.token = token;
        this.expiryDate = expiryDate;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
}
