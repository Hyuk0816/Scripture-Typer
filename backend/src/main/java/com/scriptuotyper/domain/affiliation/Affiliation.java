package com.scriptuotyper.domain.affiliation;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "affiliations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Affiliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MainAffiliation mainAffiliation;

    @Column
    private String subAffiliation;

    @Column(nullable = false, unique = true)
    private String displayName;

    @Builder
    public Affiliation(MainAffiliation mainAffiliation, String subAffiliation, String displayName) {
        this.mainAffiliation = mainAffiliation;
        this.subAffiliation = subAffiliation;
        this.displayName = displayName;
    }
}
