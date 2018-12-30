# Camera Backend

## Setup
### OpenCV
To get the OpenCV lib files, you must compile from source, Look [here](https://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html) for installation instructions.
The installation of OpenCV from source via brew takes a very long time 4-6 hours.
Copy over the haar files from your installation dir:
```bash
cp /path/to/opencv/share/OpenCV/haarcascades/* /path/to/repo/src/main/resources/haar
```

## Running in dev
### IntelliJ Idea
To run in IntelliJ, you must direct it to your OpenCV jars, to do this go to `Project Structure`.\
Under `Project Settings` got to `Modules` click `main` and then `Dependencies` in the tabs to the right.\
Click the plus at the bottom of the window. Select the location of your OpenCV.jar file.\
Once selected, double click OpenCV in the list of dependencies, click the plus in the popped up window, and then select your native library 
> For Mac this is the file with the `.dylib` file type

Close the window and go to the configurations window (top right of the main window), click `Edit configurations...`  and in `Active Profiles` enter the following:
```
local, h2
```

To run, just click the play button next to the configurations dropdown.