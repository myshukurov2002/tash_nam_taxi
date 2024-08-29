package com.company.client.components;

import com.company.base.BaseEntity;
import com.company.components.Address;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "client")
public class ClientEntity extends BaseEntity {

    @Id
    private Long chatId;

    @Enumerated(EnumType.STRING)
    private ClientState state;

    @OneToMany(mappedBy = "client")
    private List<VoyageEntity> voyages;

    private String fromTo;
}
