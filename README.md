# Real-time monitoring of patients using the mobile cloud
Author: Zhang Yixin   
Supervisor: Dr. Dan Grigoras    
Second reader: Dr. John Herbert    

## The structure of directories
In the Android directory, there are codes for three clients:   
- bulletinBoard: Bulletin board    
- doctor: doctor-client
- dummtPatient: patient-client

In the Python directory, there are codes for two python programs:    
- server: the server
- experiment: original experiment data and programs used to plot the graph used in the thesis. The .pcapng file can be opened by WireShark. Use the following filter to view the network traffic related to the project: `tcp.port == 6122 || udp.port == 6122`.
