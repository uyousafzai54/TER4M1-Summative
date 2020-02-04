# TER4M1-Summative-
Grade 12 Robotics Final Project.

Credits to Max Belleville. 

Robotics Summative
Made by: Max Belleville and Umar Yousafzai

Umar: Processing code, Arduino code, hardware
Max: Android App, Arduino code, hardware

Link to Video: https://photos.app.goo.gl/KsKRQA3WbZQjaQ7U9

Project’s used
To aid in developing our project as we first used the processing library ControlP5. That gave us some P5.js functionality in processing meaning we would have access to basic UI elements like buttons and text input fields. (Credit: http://www.sojamo.de/libraries/controlP5/)

As for the mobile side of things we planned on coding it from scratch but as it happens the Bluetooth module we got was not a normal Bluetooth module. It was a JDY-09 whereas a normal version is HC-05 or HC-06.

Max spent about eight hours trying to figure it out. In the interest of time we downloaded some Bluetooth LE code from Github, added messaging, saving, updated UI and changed how the Bluetooth device discovery worked. (Credit to the original BLE code: https://github.com/kai-morich/SimpleBluetoothLeTerminal)

Updates we could include:

●	Fix a minor bug with android app (commands effect how android displays messages)

●	Recreate the Bluetooth low energy code so you are not relying on third party code.

●	Add a music command /play ‘music’ which would either play an mp3 or load up YouTube and open the first video.

●	Add more home automation abilities like /weather to pull information from a weather API and display it on whatever device did /weather.

●	Add some two games like simple tic tac toe or connect four. 
