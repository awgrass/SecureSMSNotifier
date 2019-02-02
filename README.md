# SecureSMSNotifier #

## ACN Task 2: _SecureSMSNotifier_ ##

*Authors:* Alexander Grass & Alex Maestrini  
*App version:* 0.1.0  

### What is this repository for? ###

* This is a git repository for the second assignment of Advanced Computer Network. 
* The task was to create a Secure SMS Notifier, who's intercepting the phones SMS and sending them in a secure way to the desktop.
* The repository contians the server (/SecureSMSServer) and the client (/SecureSMSClient) application.


### Setup ### 


#### How to compile? ####

The recommendet JDK version for this project is Java SE Development Kit 9.0.4.  
The recommendet IDE is Intellij version 2017.3.3 Ultimate.


#### How to run? ####

There is a executable JAR in the path /SecureSMSServer/out/artifacts/SecureSMSServer.jar.  
The recommendet IDE is Android Studio version 3.0.1.


### Workflow ###

* On opening the server app, it jumps to the QR code window.
* Once the QR code is scanned with the mobile app, the server automatically switches to message view.
* The phone connects to the desktop on SMS receive and sends the received SMS using an AEAD scheme (AES-GCM).


### Good to know ###

* Scanning the ip twice is not possible.
* Long press on saved server enables edit mode, normal press shows more details.
* The server generates a new QR code for each new ip address he switches to.


### DEBUG MODE ###

There is a DEBUG mode hidden in the application. This hidden DEBUG mode can be activated by clicking 7 times in a row on the about dialog.  
Once this mode is activated, there is a additional menu item, which allows to send a fake SMS to the current mobile device, to trigger the application.  
In this mode, those incomming applications are producing more debug outputs and Toast messages.  


### QR Code Content ###

* The QR code for now should contain a string that has the following format:
  * ServerName|Key|IP|Port|Type
  * ServerName: String
  * Key: Encryption Key
  * IP: XXX.XXX.X.XXX
  * Port: 0 ... 65535
  * Type: 1 = Mac; 0 = Windows, Linux, Rest
  
  *Example: MyNotebook|X4Eg5jKo0Xw4|192.168.1.69|6232|1*

  ![QR Code Example](QR_Example.png "QR Code Example")
  

### Presentation ###

Under the following link the presentation for this application can be found:

[Presentation](Presentation.pdf "Presentation")

Copyright © 2018.
All rights reserved.
