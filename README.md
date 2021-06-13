# CFIApp
 Android application to extract CFI curves in real time. My diploma thesis at Electrical and Computer Engineering, AUTh.
 
 ## Supported features
 - Connection with the Skale smart bluetooth weight scale for real time meal weight sampling, through the Skale SDK.
 
 - Real time extraction of the cumulative food intake curve of the meal, in Python with the use of Chaquopy. Involves various signal processing techniques.
 - Control mode: The user can consume three control meals to establish a baseline eating behaviour and setup the application's training mode.
 - Training mode: The user can modify his cumulative food intake curve in real time, based on a reference curve of healthy eating behaviour. The reference curve is tuned based on the parameters extracted from the control mode meals.
 
 - Calculation of important food intake indicators after the end of the meal, like food intake deceleration, total food intake, average bite size, etc.
 - Characterisation of meal eating behaviour as accelerated (very high risk), linear (high risk) or decelerated (low risk), and personalised advice based on the recorded meal.

### Work in progress
