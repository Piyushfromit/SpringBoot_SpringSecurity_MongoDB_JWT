package com.influencer.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "database_sequences")
public class DatabaseSequence {

    @Id
    private String id;
    private int seq;

}
