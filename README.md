# smartsign-service
Junior Design Project: Android Accessibility Service that enables users to look up words in sign language
The Android app allows users to translate text into sign langauge by typing in a word or sharing the word with the app.
The app uses the Georgia Tech Smart Sign Dictionary Database to perform the word lookups.  This project exists due to a project
request from Georgia Tech's Dr. Harley Hamilton, and the project is for him.

Instructions:
  --To install, simply put the .apk file on your android device and click on it.  Your device should immediately ask if you want to 
    install it.  However, because it won't be from the Play Store, your device will see it as a security risk, so you may receive a 
    special prompt asking if you're sure that you want to install it.
  --To open the source code, use the github url to open pull down the source code in the editor of your choice.
  
Release Version 1.0.0:
  --Features:
    --Lookup from text to signlanguage using Smartsign Dictionary Database
    --Show list of possible translations
    --Perform lookup of text shared with the app through the Android device.
    --Open seperate app to show translation as a video upon clicking/selecting which translation the user wants to view.
  --Known Bugs:
    --The listview looses all items if the phone's orientation is changed or if the listview needs to be refreshed for any other reason.
    The only fix is to press the search button again/research the word.
    --The images in the listview may take a while to load depending on internet speed.  So in rare cases the image displayed may be
    the wrong image, but after a second or less the image should update to the correct one.
