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
@Table(name = "user_api_log", indexes = {
        @Index(columnList = "user_id, requested_at"),
        @Index(columnList = "requested_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserApiLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "request_uri", nullable = false, length = 500)
    private String requestUri;

    @Column(name = "response_status", nullable = false)
    private int responseStatus;

    @Column(name = "execution_time_ms", nullable = false)
    private Long executionTimeMs;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @CreatedDate
    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Builder
    public UserApiLog(User user, String httpMethod, String requestUri,
                      int responseStatus, Long executionTimeMs,
                      String errorMessage, String ipAddress) {
        this.user = user;
        this.httpMethod = httpMethod;
        this.requestUri = requestUri;
        this.responseStatus = responseStatus;
        this.executionTimeMs = executionTimeMs;
        this.errorMessage = errorMessage;
        this.ipAddress = ipAddress;
    }
}
