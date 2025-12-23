package com.example.AzureTestProject.Api.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "sequence_counter")
public class SequenceCounter {

    @Id
    private Long id = 1L;

    @Column(name = "last_val")
    private long lastValue;

    @Version
    private Long version;


    public SequenceCounter(Long id, long lastValue) {
        this.id = id;
        this.lastValue = lastValue;
    }

}
