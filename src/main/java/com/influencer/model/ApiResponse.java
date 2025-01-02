package com.influencer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {

    private LocalDateTime timestamp;
    private int status;
    private String message;
    private Object data;



}
