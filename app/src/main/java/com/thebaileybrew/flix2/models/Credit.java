package com.thebaileybrew.flix2.models;


public class Credit {
    private  String creditCharacterName;
    private String creditActorName;
    private String creditPath;

    public String getCreditCharacterName() {
        return creditCharacterName;
    }

    public void setCreditCharacterName(String creditCharacterName) {
        this.creditCharacterName = creditCharacterName;
    }

    public String getCreditActorName() {
        return creditActorName;
    }

    public void setCreditActorName(String creditActorName) {
        this.creditActorName = creditActorName;
    }

    public String getCreditPath() {
        return creditPath;
    }

    public void setCreditPath(String creditPath) {
        this.creditPath = creditPath;
    }
}
