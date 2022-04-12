# autoConfigExample
example REST config app. able to look for an SSID and connect to it.
send GET and POST to specified URL
Dynamic use of parameter (no parameter send  if key is blank)

/system/reboot is not a valid endpoint despite the documentation.
POST to /wifi/connect to reboot device
(is this bug?)


1.wifi must be set in two steps, POST to plugins/wifi with with credentials
2. post to wifi/connect with ssid and password as keys, but any/no value (this will call reboot and device will connect to wifi)

handset notes -- 
user profiles are stored in 
spotifyUsersCache = /sdcard/android/data/com.spotify.music/files/spotifycache/users
can scrape folder for stored spotify users and ask which spotify user

get and post functions are encapsulated into a AuthenticationManager class (needs refactored) 

to add -- 
##--android AccountManager to create user profile and store in device for 'profile' switching between different accounts/storing login credentials 
