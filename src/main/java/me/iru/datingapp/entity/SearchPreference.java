package me.iru.datingapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "search_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private User.Gender preferredGender;

    @Column(nullable = false)
    private Integer minAge;

    @Column(nullable = false)
    private Integer maxAge;

    @Column
    private Integer maxDistance;

    public static SearchPreference defaultForUser(User user) {
        SearchPreference preference = new SearchPreference();
        preference.setUser(user);
        preference.setPreferredGender(null);

        int userAge = user.getAge();
        preference.setMinAge(Math.max(18, userAge - 5));
        preference.setMaxAge(Math.min(100, userAge + 5));
        preference.setMaxDistance(50);

        return preference;
    }
}

