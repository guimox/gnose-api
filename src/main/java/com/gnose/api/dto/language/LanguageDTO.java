package com.gnose.api.dto.language;

public class LanguageDTO {
    private String name;

    public LanguageDTO(String language) {
        this.name = language;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
