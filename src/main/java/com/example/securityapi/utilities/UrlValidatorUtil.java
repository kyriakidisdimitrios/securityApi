package com.example.securityapi.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.IDN;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * SSRF guard for user-supplied URLs.
 *  - allow only http/https
 *  - forbid credentials in URL
 *  - allow only common web ports
 *  - resolve DNS and reject internal/loopback/link-local/private/multicast/etc.
 *
 * New helpers:
 *  - explainIfBlocked(url) -> reason string or null if allowed
 *  - isSafeUrl(url)        -> true if allowed (public)
 */
public final class UrlValidatorUtil {
    private static final Logger log = LoggerFactory.getLogger(UrlValidatorUtil.class);

    private static final Set<String> ALLOWED_SCHEMES =
            new HashSet<>(Arrays.asList("http", "https"));

    // -1 = default port (http/https). Allow a couple of dev TLS ports too.
    private static final Set<Integer> ALLOWED_PORTS =
            new HashSet<>(Arrays.asList(-1, 80, 443, 8443, 9443));

    private static final Set<String> BLOCKED_HOSTNAMES =
            new HashSet<>(Arrays.asList(
                    "localhost",
                    "localhost.localdomain",
                    "metadata.google.internal",
                    "metadata",
                    "169.254.169.254" // common cloud metadata IP (also caught by IP checks)
            ));

    private UrlValidatorUtil() {}

    /** Return null if URL is allowed (public); otherwise a short reason string. */
    public static String explainIfBlocked(String url) {
        if (url == null || url.isBlank()) {
            return "URL is missing";
        }

        final URI uri;
        try {
            uri = URI.create(url.trim());
        } catch (IllegalArgumentException e) {
            return "URL is not valid";
        }

        // Scheme
        String scheme = safeLower(uri.getScheme());
        if (!ALLOWED_SCHEMES.contains(scheme)) {
            return "Only http/https are allowed";
        }

        // Host + no userinfo
        String rawHost = uri.getHost();
        String rawUserInfo = uri.getRawUserInfo(); // may be null
        if (rawHost == null) {
            return "URL has no host";
        }
        if (rawUserInfo != null && !rawUserInfo.isEmpty()) {
            return "Credentials in URL are not allowed";
        }

        // Hostname normalization (punycode) + simple blocklist
        String host = IDN.toASCII(rawHost, IDN.ALLOW_UNASSIGNED).toLowerCase();
        if (host.isBlank()) {
            return "Hostname is empty";
        }
        if (BLOCKED_HOSTNAMES.contains(host)) {
            return "Hostname is blocked";
        }

        // Port allowlist
        int port = uri.getPort();
        if (!ALLOWED_PORTS.contains(port)) {
            return "Port is not allowed";
        }

        // DNS resolution → must be public/routable (blocks “internal websites”)
        try {
            InetAddress[] addrs = InetAddress.getAllByName(host);
            if (addrs == null || addrs.length == 0) {
                return "Hostname did not resolve";
            }
            for (InetAddress addr : addrs) {
                if (!isPublicRoutable(addr)) {
                    return "Target resolves to internal/private address";
                }
            }
        } catch (Exception ex) {
            log.debug("DNS resolution failed for host {}", host, ex);
            return "Hostname did not resolve";
        }

        // Everything OK ⇒ public URL allowed
        return null;
    }

    /** Convenience: true if the URL is allowed (public). */
    public static boolean isSafeUrl(String url) {
        return explainIfBlocked(url) == null;
    }

    // ===== helpers =====

    private static String safeLower(String s) { return s == null ? null : s.toLowerCase(); }

    private static boolean isPublicRoutable(InetAddress ip) {
        if (ip.isAnyLocalAddress()) return false;      // 0.0.0.0 / ::
        if (ip.isLoopbackAddress()) return false;      // 127.0.0.0/8, ::1
        if (ip.isLinkLocalAddress()) return false;     // 169.254.0.0/16, fe80::/10
        if (ip.isSiteLocalAddress()) return false;     // 10/8, 172.16/12, 192.168/16
        if (ip.isMulticastAddress()) return false;     // 224.0.0.0/4, ff00::/8

        // IPv6 unique local fc00::/7
        if (ip instanceof Inet6Address ipv6) {
            byte[] b = ipv6.getAddress();
            int top = b[0] & 0xFE; // mask lowest bit
            if (top == (byte) 0xFC) return false; // fc00::/7
        }

        // RFC 6598 CGNAT 100.64.0.0/10
        if (isIpv4InRange(ip, 100, 64, 0, 0, 100, 127, 255, 255)) return false;

        return true;
    }

    private static boolean isIpv4InRange(InetAddress addr,
                                         int aMin, int bMin, int cMin, int dMin,
                                         int aMax, int bMax, int cMax, int dMax) {
        byte[] b = addr.getAddress();
        if (b.length != 4) return false;
        int ip = ((b[0] & 0xFF) << 24) | ((b[1] & 0xFF) << 16) | ((b[2] & 0xFF) << 8) | (b[3] & 0xFF);
        int min = ((aMin & 0xFF) << 24) | ((bMin & 0xFF) << 16) | ((cMin & 0xFF) << 8) | (dMin & 0xFF);
        int max = ((aMax & 0xFF) << 24) | ((bMax & 0xFF) << 16) | ((cMax & 0xFF) << 8) | (dMax & 0xFF);
        return ip >= min && ip <= max;
    }
}
