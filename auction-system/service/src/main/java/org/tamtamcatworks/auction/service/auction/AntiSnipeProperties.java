package org.tamtamcatworks.auction.service.auction;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auction.anti-snipe")
public record AntiSnipeProperties(int windowSeconds, int extensionSeconds) {}
