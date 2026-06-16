package com.blockads.vpn.data

object DnsProviders {
    val providers =
        mapOf(
            "AdGuard DNS" to "94.140.14.14",
            "NextDNS" to "45.90.28.0",
            "Cloudflare" to "1.1.1.1",
            "Cloudflare Malware" to "1.1.1.2",
            "Google DNS" to "8.8.8.8",
            "Quad9" to "9.9.9.9",
            "OpenDNS" to "208.67.222.222",
            "CleanBrowsing" to "185.228.168.9",
            "Control D" to "76.76.2.0",
            "Alternate DNS" to "76.76.19.19",
            "Mullvad DNS" to "194.242.2.2",
        )

    fun getIpByName(name: String): String {
        return providers[name] ?: "94.140.14.14"
    }

    fun getNameByIp(ip: String): String {
        return providers.entries.find { it.value == ip }?.key ?: "Unknown DNS"
    }
}
