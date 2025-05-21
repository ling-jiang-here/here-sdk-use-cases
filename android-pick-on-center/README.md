<!-- The HelloMap example app shows how the HERE SDK can be integrated into your project and how to display a map.

Build instructions:
-------------------

1) Copy the AAR file of the HERE SDK for Android to your app's `app/libs` folder.

Note: If your AAR version is different than the version shown in the _Developer Guide_, you may need to adapt the source code of the example app.

2) Open Android Studio and sync the project.

Please do not forget: To run the app, you need to add your HERE SDK credentials in the `MainActivity.java` file. More information can be found in the _Get Started_ section of the _Developer Guide_. -->

This demo is built on the Hellp Map demo app to show how to pick the surrounding map content or items with a tap on the screen.  

The default behavior or the issue is, only the content or items on the right or below the tap location are picked. This is expected because the searching area is a rectangle with the tap location as its left-top corner.  
![image](https://github.com/user-attachments/assets/176cc956-3fec-4090-bc0a-582a83145dff)

The solution is relocating the searching area with the tap location at the center of the rectangle searching area.  
![image](https://github.com/user-attachments/assets/16816ae9-7fbd-422e-a656-8bc3fd3fc593)

Look at the Logcat and filter the log with `package:mine Polygon picked:` the rectangle on screen was only selected with you tap on its top and left. Now, no matter on which side of the rectangle, you can have the rectangle selected.  
![image](https://github.com/user-attachments/assets/cd9d0134-e404-456d-b9b6-aa5a779277ec)
