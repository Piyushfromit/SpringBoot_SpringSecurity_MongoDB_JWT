package com.influencer.model;



public record LoginResponseDTO(

        String status ,
        String jwtToken,
        Object data


) {}
