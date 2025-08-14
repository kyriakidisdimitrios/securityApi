package com.example.securityapi.utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.IDN;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.util.Set;
/**
 * S.S.R.F guard for user-supplied URLs.
 *  - allow only http/https
 *  - forbid credentials in URL
 *  - allow only common web ports
 *  - resolve DNS and reject internal/loopback/link-local/private/multicast/etc.

 * Helpers:
 *  - explainIfBlocked(url) → reason string, or null if allowed
 *  - isSafeUrl(url) → true if allowed (public)
 */
public final class UrlValidatorUtil {
    private static final Logger log = LoggerFactory.getLogger(UrlValidatorUtil.class);
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    // -1 = default port (http/https). Include a couple of common dev TLS ports.
    private static final Set<Integer> ALLOWED_PORTS = Set.of(-1, 80, 443, 8443, 9443);
    // Optional “never fetch” hostnames (defense-in-depth)
    private static final Set<String> BLOCKED_HOSTNAMES = Set.of(
            "localhost",
            "localhost.localdomain",
            "metadata.google.internal",
            "metadata",
            "169.254.169.254" // cloud metadata IP (also caught by IP checks)
    );
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
        final String scheme = toLower(uri.getScheme());
        if (!ALLOWED_SCHEMES.contains(scheme)) {
            return "Only http/https are allowed";
        }

        // Host + no userinfo
        final String rawHost = uri.getHost();
        final String rawUserInfo = uri.getRawUserInfo(); // maybe null
        if (rawHost == null) {
            return "URL has no host";
        }
        if (rawUserInfo != null && !rawUserInfo.isEmpty()) {
            return "Credentials in URL are not allowed";
        }
        // Hostname normalization (punycode) + simple blocklist
        final String host = IDN.toASCII(rawHost, IDN.ALLOW_UNASSIGNED).toLowerCase();
        if (host.isBlank()) {
            return "Hostname is empty";
        }
        if (BLOCKED_HOSTNAMES.contains(host)) {
            return "Hostname is blocked";
        }
        // Port allowlist
        final int port = uri.getPort();
        if (!ALLOWED_PORTS.contains(port)) {
            return "Port is not allowed";
        }
        // DNS resolution → must be public/routable (blocks “internal websites”)
        try {
            InetAddress[] inetAddress = InetAddress.getAllByName(host);
            if (inetAddress == null || inetAddress.length == 0) {
                return "Hostname did not resolve";
            }
            for (InetAddress addr : inetAddress) {
                if (!isPublicRoutable(addr)) {
                    return "Target resolves to internal/private address";
                }
            }
        } catch (Exception ex) {
            log.debug("DNS resolution failed for host {}", host, ex);
            return "Hostname did not resolve";
        }
        // Allowed
        return null;
    }
    /** Convenience: true if the URL is allowed (public). */
    public static boolean isSafeUrl(String url) {
        return explainIfBlocked(url) == null;
    }
    // ===== helpers =====
    private static String toLower(String s) {
        return s == null ? null : s.toLowerCase();
    }
    /**
     * Reject loopback, any-local, link-local, site-local (RFC1918), multicast,
     * IPv6 Unique Local (fc00::/7), and IPv4 C.GN.A.T 100.64.0.0/10.
     */
    private static boolean isPublicRoutable(InetAddress ip) {
        if (ip.isAnyLocalAddress()) return false;      // 0.0.0.0 / ::
        if (ip.isLoopbackAddress()) return false;      // 127.0.0.0/8, ::1
        if (ip.isLinkLocalAddress()) return false;     // 169.254.0.0/16, fe80::/10
        if (ip.isSiteLocalAddress()) return false;     // 10/8, 172.16/12, 192.168/16
        if (ip.isMulticastAddress()) return false;     // 224.0.0.0/4, ff00::/8
        // IPv6 ULA fc00::/7 (compare as int to avoid sign issues)
        if (ip instanceof Inet6Address ipv6) {
            byte[] b = ipv6.getAddress();
            int firstByte = b[0] & 0xFF;
            int masked = firstByte & 0xFE; // top 7 bits
            if (masked == 0xFC) return false; // fc00::/7
        }
        // IPv4 C.G.NA.T 100.64.0.0/10
        return !ipv4InCidr(ip);
    }
    /** Returns true if the given InetAddress (IPv4) is inside the CIDR. */
    private static boolean ipv4InCidr(InetAddress addr) {
        final byte[] b = addr.getAddress();
        if (b.length != 4) return false;
        final String[] parts = "100.64.0.0/10".split("/");
        if (parts.length != 2) return false;
        final String[] oct = parts[0].split("\\.");
        if (oct.length != 4) return false;
        final int prefix;
        try {
            prefix = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }
        if (prefix < 0 || prefix > 32) return false;

        final int ip =
                (Byte.toUnsignedInt(b[0]) << 24)
                        | (Byte.toUnsignedInt(b[1]) << 16)
                        | (Byte.toUnsignedInt(b[2]) << 8)
                        |  Byte.toUnsignedInt(b[3]);
        final int base =
                (parseOctet(oct[0]) << 24)
                        | (parseOctet(oct[1]) << 16)
                        | (parseOctet(oct[2]) << 8)
                        |  parseOctet(oct[3]);

        // Build mask using 64-bit to avoid shift-edge warnings
        final int mask = (prefix == 0) ? 0 : (int) (0xFFFFFFFFL << (32 - prefix));
        return (ip & mask) == (base & mask);
    }
    private static int parseOctet(String s) {
        try {
            final int v = Integer.parseInt(s);
            return (v < 0 || v > 255) ? 0 : v;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}