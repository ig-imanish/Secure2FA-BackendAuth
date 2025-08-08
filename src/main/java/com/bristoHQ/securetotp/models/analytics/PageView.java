package com.bristoHQ.securetotp.models.analytics;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "page_views")
public class PageView {
    @Id
    private String id;
    private String userId;
    private String page;
    private String ipAddress;
    private String userAgent;
    private Date timestamp;
    private String sessionId;
}
