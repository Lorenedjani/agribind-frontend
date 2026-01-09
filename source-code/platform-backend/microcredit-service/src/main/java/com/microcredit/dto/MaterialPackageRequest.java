package com.microcredit.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;


@Data
public class MaterialPackageRequest {
    private String packageName;
    private List<MaterialItemDTO> items;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<MaterialItemDTO> getItems() {
        return items;
    }

    public void setItems(List<MaterialItemDTO> items) {
        this.items = items;
    }
}