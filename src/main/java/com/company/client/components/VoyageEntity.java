package com.company.client.components;

import com.company.base.BaseIdEntity;
import com.company.components.Address;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "voyage")
public class VoyageEntity extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id",
            insertable = false,
            updatable = false)
    private ClientEntity client;

    @Column(name = "client_id")
    private Long clientId;

    @Enumerated(EnumType.STRING)
    private VoyageState voyageState;

    private String fromTo;
    private String voyageType;
    private String data;
}
