package com.company.taxi.components;

import com.company.attach.components.AttachEntity;
import com.company.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cascade;

import java.util.Set;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "taxi")
public class TaxiEntity extends BaseEntity {


    @Id
    private Long chatId;

    @Column
    private boolean status;

    private String fromTo;

    @OneToMany(mappedBy = "taxi", fetch = FetchType.EAGER)
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    Set<AttachEntity> attaches;

    @Enumerated(EnumType.STRING)
    private TaxiState taxiState;

    private int duration = 0;

    public boolean getStatus() {
        return this.status;
    }

    @Override
    public void setVisibility(Boolean visibility) {
        super.setVisibility(visibility);
        if (!visibility) {
            for (AttachEntity attach : attaches) {
                attach.setVisibility(false);
            }
        }
    }

    public void setDuration(int d) {
        duration += duration + d;
    }


    public void ban() {
        duration = 0;
        status = false;
    }
}
