package com.scriptuotyper.domain.log;

import com.scriptuotyper.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_menu_access_log", indexes = {
        @Index(columnList = "user_id, accessed_at"),
        @Index(columnList = "accessed_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMenuAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "menu_name", nullable = false, length = 50)
    private String menuName;

    @Column(name = "path", nullable = false, length = 200)
    private String path;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @CreatedDate
    @Column(name = "accessed_at", nullable = false, updatable = false)
    private LocalDateTime accessedAt;

    @Builder
    public UserMenuAccessLog(User user, String menuName, String path, String ipAddress) {
        this.user = user;
        this.menuName = menuName;
        this.path = path;
        this.ipAddress = ipAddress;
    }
}
