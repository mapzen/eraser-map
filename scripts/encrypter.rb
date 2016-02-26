require "android-all-4.4_r1-robolectric-0.jar"
$CLASSPATH << 'app/src/main/java'
special_salt = ARGV[0]
to_encode = ARGV[1]
simple_crypt = com.mapzen.erasermap.SimpleCrypt.withSpecialSalt(special_salt)
puts simple_crypt.encode(to_encode)
