# autoConfigExample
example of adding a network to saved network and connecting to it, then pointing to a specific URL


spotifyUsersCache = /sdcard/android/data/com.spotify.music/files/spotifycache/users

get and post are encapsulated into a AuthenticationManager class (will add tokens and profiles later)

should use WifiManager class to get list of saved network profiles(or one currently connected to) and use Opal API to set Wifi config to Opal
then switch to that, and then open Spotify via shell command (or write spotifyAPI client using spotify web API) (maybe??)
