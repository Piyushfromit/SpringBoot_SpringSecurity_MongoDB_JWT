package com.fit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "authority")
public class Authority {

    @Id
    private long id;
    private String name;

    @JsonIgnore // Prevent circular references during serialization
    @DBRef(lazy = true) // Use lazy loading
    private User user;


}
