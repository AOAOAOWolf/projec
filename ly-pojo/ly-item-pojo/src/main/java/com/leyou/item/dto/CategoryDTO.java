package com.leyou.item.dto;

import com.leyou.item.entity.Category;
import lombok.Data;

import java.util.List;

@Data
public class CategoryDTO {
    private String name;
    private List<Category> list;
}
