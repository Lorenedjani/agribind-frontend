package cm.agribind.usermanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cooperative_managers")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
public class CooperativeManager extends User {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cooperative_id", nullable = false)
    private Cooperative cooperative;

    // Additional fields specific to CooperativeManager can be added here
}
