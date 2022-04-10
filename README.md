# autoConfigExample
example of adding a network to saved network and connecting to it, then pointing to a specific URL


/system/reboot is not a valid endpoint despite the documentation.
https://feelfreelinux.github.io/euphonium/http/main/
(is this bug?)

1.wifi must be set in two steps, POST to plugins/wifi with with credentials
2. post to wifi/connect with ssid and password as keys, but any/no value (this will call reboot and device will connect to wifi)

spotifyUsersCache = /sdcard/android/data/com.spotify.music/files/spotifycache/users

get and post are encapsulated into a AuthenticationManager class (will add tokens and profiles later)

should use WifiManager class to get list of saved network profiles(or one currently connected to) and use Opal API to set Wifi config to Opal
then switch to that, and then open Spotify via shell command (or write spotifyAPI client using spotify web API) (maybe??)
