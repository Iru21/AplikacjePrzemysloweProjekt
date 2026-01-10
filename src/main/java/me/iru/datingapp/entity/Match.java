package me.iru.datingapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matches", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user1_id", "user2_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @Column(nullable = false, updatable = false)
    private LocalDateTime matchedAt;

    @Column(nullable = false)
    private Boolean isActive;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        matchedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }


    public boolean hasUser(User user) {
        return user != null && (user.equals(user1) || user.equals(user2));
    }

    public User getOtherUser(User user) {
        if (user == null) {
            return null;
        }
        if (user.equals(user1)) {
            return user2;
        }
        if (user.equals(user2)) {
            return user1;
        }
        return null;
    }
}

