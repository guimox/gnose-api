package com.gnose.api.dto.category;

public class CategoryDTO {
    private String name;

    public CategoryDTO(String category) {
        this.name = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
