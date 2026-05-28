package org.tamtamcatworks.auction.service.auction;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "auction.anti-snipe")
@Component
public record AntiSnipeProperties(int windowSeconds, int extensionSeconds) {}
