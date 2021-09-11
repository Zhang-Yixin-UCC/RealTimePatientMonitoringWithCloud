# Real-time monitoring of patients using the mobile cloud
Author: Zhang Yixin   
Supervisor: Dan Grigoras    
Second reader: John Herbert    

## The structure of directories
In the Android directory, there are codes for three clients:   

- Path for bulletin board: `\Android\bulletinBoard`
- Path for doctor-client: `\Android\doctor`
- Path for patient-client: `\Android\dummyPatient`

In the Python directory, there are codes for two python programs:
    
- Path for the server: `\Python\server`
- Path for the experiment data and programs used for plotting graph (uncommented): `\Python\experiment`

The python programs contains the virtual environment used to run the programs. The virtual environment locates in the `\venv` directory in the program's directory. 

## Important files
- SQLite database file: `\Python\server\HIS.db`. No username and password needed.
- Experiment original data:
	- `\Python\experiment\cap.pcapng`: This is file that contains all loopback network packets during the experiment (unrelated network traffic included). Open this file using wireshark. Filter the experiment related data using the filter expression `tcp.port == 6122 || udp.port == 6122`.
	- `\Python\experiment\dbCount.csv`: This file contains time vs. the count of the database entries data. Column 1 is the time in seconds, while column 2 is the count.
	- `\Python\experiment\dbCount.csv`: This file contains time vs. the size of the database file. Column 1 is the time in seconds, while column 2 is the size in Bytes.
	- `\Python\experiment\statisticTime.csv`: This file contains the count of entries of one patient vs. the time used in executing the `/getPatientStatistic` API. Column 1 is the count while column 2 is the time in seconds.
