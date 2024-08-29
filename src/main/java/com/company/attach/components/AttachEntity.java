package com.company.attach.components;

import com.company.base.BaseEntity;
import com.company.taxi.components.TaxiEntity;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "attach")
public class AttachEntity extends BaseEntity {

    @Id
    private String id;
    private String originalName;
    private String path;
    private Long size;
    private String extension;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id",
            insertable = false,
            updatable = false)
    private TaxiEntity taxi;

    @Column(name = "chat_id")
    private Long chatId;

}
