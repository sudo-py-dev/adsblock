# Proguard rules for BlockAds VPN

# Ignore warnings for sun.net.spi.nameservice from dnsjava because it's not present on Android
-dontwarn sun.net.spi.nameservice.**
-dontwarn org.xbill.DNS.**
-dontwarn org.slf4j.**

# Remove the greedy keep rule so R8 can actually shrink and obfuscate our app code!
# Android framework handles entry points (Activity, Service) automatically.
