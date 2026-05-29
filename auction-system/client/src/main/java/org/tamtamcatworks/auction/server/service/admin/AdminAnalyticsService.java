package org.tamtamcatworks.auction.server.service.admin;

import org.springframework.stereotype.Service;

import org.tamtamcatworks.auction.shared.response.AdminDashboardResponse;

@Service
public class AdminAnalyticsService {

    public AdminDashboardResponse
    getDashboardAnalytics() {

        return new AdminDashboardResponse(

                120,

                34,

                5,

                15420.50
        );
    }
}