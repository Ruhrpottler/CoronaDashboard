### Corona Dashboard App
<img src="https://github.com/Ruhrpottler/CoronaDashboard/assets/59085591/eb6cbbd7-96f5-47bb-b84d-cc7d14cf77c7" alt="Logo" width="100"/>

### Requirements

Android 8.0 (Oreo) or higher

### Basics

Enter the name of your city and get suggestions for Germany:

<img src="https://github.com/Ruhrpottler/CoronaDashboard/assets/59085591/d7614b06-be3d-4b6c-a400-0fd209dbd5fc" alt="Enter Dortmund" width="200"/>

<img src="https://github.com/Ruhrpottler/CoronaDashboard/assets/59085591/af62a834-893c-4727-97fc-c6efef91365f" alt="Enter Munich" width="200"/>

Get the most important corona information for the given city, especially the 7 days incidence value:

<img src="https://github.com/Ruhrpottler/CoronaDashboard/assets/59085591/69e215ce-c2f2-44a0-8b33-39b8570bbf27" alt="Data for Dortmund"  width="200"/>

![image](https://github.com/Ruhrpottler/CoronaDashboard/assets/59085591/5d67def8-e210-4d52-bb0d-647c5af61a17)


### Notifications

For the requested cities, you will get notifications if the 7 days incidence values exceeded the important threshold which are specified by the German govermnent. 

<img src="https://github.com/Ruhrpottler/CoronaDashboard/assets/59085591/9c8a0a45-b6f4-40e4-8c85-052ac8aa3940" alt="Notification"  width="200"/>

<img src="https://github.com/Ruhrpottler/CoronaDashboard/assets/59085591/56198e3a-eb32-42a3-9b0a-6900804c4c15" alt="Smartwatch notification"  width="200"/>

### Offline capability

The app provides offline functionality. If you press the button to request the data and have no connection, you will get notified with a toast message and an icon on the right side:

<img src="https://github.com/Ruhrpottler/CoronaDashboard/assets/59085591/24a2d0b3-7940-4996-84c2-78efbc42e099" alt="Dortmund offline request"  width="200"/>

The app will search the last entry in the cache. The icon will stay there to show that the data is maybe not up to date.

<img src="https://github.com/Ruhrpottler/CoronaDashboard/assets/59085591/f65792e9-6953-4981-88f2-de62e53b5c25" alt="Dortmund Data offline"  width="200"/>

To provide this functionality, the app has a local cache.

<img src="https://github.com/Ruhrpottler/CoronaDashboard/assets/59085591/2d279edb-28fb-4c7f-9dac-4bc3121fc2b5" alt="Diagramm"  width="600"/>

Every time when the user requests the data while he has connection, the cache will be updated.

![image](https://github.com/Ruhrpottler/CoronaDashboard/assets/59085591/3247f995-985a-4435-9bd9-48acbdd95744)

### Data source

The data is obtained by the [ArcGIS REST API](https://services7.arcgis.com/).
