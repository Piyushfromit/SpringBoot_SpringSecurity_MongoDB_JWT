package com.influencer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocialMediaPlatform {

    private String platformName;          // e.g., Instagram, YouTube, Twitter
    private String handleUrl;            // e.g., https://instagram.com/username
    private String username;             // e.g., @username or channel name
    private Long followerCount;          // Follower or subscriber count
    private List<String> category;            // e.g., Travel, Fashion, Technology




//    private String engagementRate;      // Engagement rate (e.g., "4.5%")
//    private String status;             // ACTIVE, INACTIVE, DELETED
//    private LocalDateTime lastUpdated;  // When the follower count was last updated




}
