# autoConfigExample
example of adding a network to saved network and connecting to it, then pointing to a specific URL


spotifyUsersCache = /sdcard/android/data/com.spotify.music/files/spotifycache/users

when loaded, adds MatsyaAP network info to saved networks and points to the gateway server address (192.168.4.1)

should use WifiManager class to get list of saved network profiles(or one currently connected to) and use Opal API to set Wifi config to Opal
then switch to that, and then open Spotify via shell command (or write spotifyAPI client using spotify web API)
