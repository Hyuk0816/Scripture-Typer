package com.scriptuotyper.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_login_log",
        indexes = @Index(columnList = "user_id, login_at"))
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(name = "login_at", nullable = false, updatable = false)
    private LocalDateTime loginAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Builder
    public UserLoginLog(User user, String ipAddress) {
        this.user = user;
        this.ipAddress = ipAddress;
    }
}
