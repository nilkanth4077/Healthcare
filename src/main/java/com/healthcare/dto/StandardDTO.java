package com.healthcare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandardDTO<T> {

    private int statusCode;
    private String message;
    private T data;
    private Map<String, Object> metadata;

}
