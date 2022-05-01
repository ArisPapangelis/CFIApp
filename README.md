<img src="/CFIApp_screenshots/CFIAppIcon.png" alt="CFIApp Icon" title="CFIApp" height="250"/>

# CFIApp
 Android application to extract CFI curves in real time. My diploma thesis at Electrical and Computer Engineering, AUTh.
 
 ## Supported features
 - Connection with the Skale smart bluetooth weight scale for real time meal weight sampling, through the Skale SDK.
 
 - Real time extraction of the cumulative food intake curve of the meal, in Python with the use of Chaquopy. Involves various signal processing techniques.
 - Control mode: The user can consume three control meals to establish a baseline eating behaviour and setup the application's training mode.
 - Training mode: The user can modify his cumulative food intake curve in real time, based on a reference curve of healthy eating behaviour. The reference curve is tuned based on the parameters extracted from the control mode meals.
 
 - Calculation of important food intake indicators after the end of the meal, like food intake deceleration, total food intake, average bite size, etc.
 - Characterisation of meal eating behaviour as accelerated (very high risk), linear (high risk) or decelerated (low risk), and personalised advice based on the recorded meal.

## Screenshots

#### Main Activity - Profile Tab &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp; Main Activity - Setup Tab 
<img src="/CFIApp_screenshots/MainActivity_ProfileTab.jpg" alt="Profile tab" title="Profile tab" width="300"/> &emsp;&emsp;&emsp;
<img src="/CFIApp_screenshots/MainActivity_SetupTab.jpg" alt="Setup tab" title="Setup tab" width="300"/>

#### Main Activity - Training Tab
<img src="/CFIApp_screenshots/MainActivity_TrainingTab.jpg" alt="Training tab" title="Training tab" width="300"/>

<br></br>

#### Control Mode Activity &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp; Training Mode Activity
<img src="/CFIApp_screenshots/ControlModeActivity.jpg" alt="Control Mode Activity" title="Control Mode Activity" width="300"/> &emsp;&emsp;&emsp;
<img src="/CFIApp_screenshots/TrainingModeActivity.jpg" alt="Training Mode Activity" title="Training Mode Activity" width="300"/> 

<br></br>

#### Plotting Activity - Start
<img src="/CFIApp_screenshots/PlottingActivity_start.jpg" alt="Plotting Activity - Start" title="Plotting Activity - Start" width="650"/>

#### Plotting Activity - Finish
<img src="/CFIApp_screenshots/PlottingActivity_finish.jpg" alt="Plotting Activity - Finish" title="Plotting Activity - Finish" width="650"/> 

<br></br>

#### Indicators Activity
<img src="/CFIApp_screenshots/IndicatorsActivity.jpg" alt="Indicators Activity" title="Indicators Activity" width="300"/> &emsp;

<br></br>

## Videos
#### Live demonstration of CFIApp
[![CFIApp](https://img.youtube.com/vi/scGcK5S7wrI/0.jpg)](https://www.youtube.com/watch?v=scGcK5S7wrI)

<br></br>

#### Presentation of my diploma thesis (in Greek)
[![CFIApp](https://img.youtube.com/vi/VCsytWwQ2Sg/0.jpg)](https://www.youtube.com/watch?v=VCsytWwQ2Sg)
