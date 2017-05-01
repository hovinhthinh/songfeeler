# songfeeler
[![Build Status](https://travis-ci.org/hovinhthinh/songfeeler.svg?branch=master)](https://travis-ci.org/hovinhthinh/songfeeler)

music recogition system (like Shazam, SoundHound and other systems)

## androidapp

built on Android Studio,
apk can be found in folder app/build/outputs/apk,
after installing, change the address in setting to the address of detector

## webserver

```
rails s -p 80 -b 0.0.0.0
```

use administrator privilege if required

## detector

```
bash build
bash run
```
## configurations
Edit these files to change the deployed address/port of components:
`detector/conf/server-config.xml`
```
    <!-- address of deployed webserver -->
    <entry key="web-server.address">127.0.0.1</entry> 
    <entry key="web-server.port">80</entry>
```
and:
`webserver/rb/config.rb`
```
# address of deployed detector
$DETECTOR_HOST = "localhost" # in most case, should change this only
$DETECTOR_PORT = 8999
# hard-coded log-in account of webserver (should be fixed soon)
$USER = "admin"
$PASS = "admin"
```
