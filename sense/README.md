# Camera based Real-time Analytics on Movements of People (CRAMP)
We are developing a system for generating realtime analytics on movements of people in a multiple camera monitored environment.
* Ground Mapped Human Movement Analytics.
* Aggregrated Analytical Maps.
  - Human Density Maps
  - Head Direction Maps
  - Speed Bound Maps
* Per Person Level Analytics using Short Term Re-Identification and Tracking.

_CRAMP_SENSE_ is the Camera Module of system which is responsible for
* Detecting New Persons
* Mapping of detected persons to world space. (Ground Place Mapping)
* Tracking of Persons (for purpose of detecting new persons)
* Obtain snapshots (for re-id purpose) of newly detected persons.
* Communicating results to Analytics Core.

## Running the Code
1) Obtain and Configure OpenPose System.
  - https://github.com/CMU-Perceptual-Computing-Lab/openpose
2) Compile and Configure [OpenPersonDetectorAPI](OpenPersonDetectorAPI)
3) Set LD_LIBRARY_PATH env variable as `[OPENPOSE_CAFFE_HOME]/build/lib/:[CUDA_HOME]/lib64:[OPENPOSE_HOME]build/lib:[Python3.5_Path]/dist-packages`
4) Obtain [test videos](test_videos).
5) Run
  - LivePreview.py to obtain Live Preview of Detection, Mapping and Tracking.
  - CamServer to run Camera Module Server.
  
## Contributors
* Madhawa Vidanapathirana - madhawa.13@cse.mrt.ac.lk
* Imesha Sudasingha - imesha.13@cse.mrt.ac.lk
* Pasindu Kanchana - pasindukanchana.13@cse.mrt.ac.lk 
* Jayan Vidanapathirana - jayancv.13@cse.mrt.ac.lk
