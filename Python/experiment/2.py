import matplotlib.pyplot as plt
import pandas as pd

count = []
time = []
with open("statisticTime.csv","r+") as f:
    data = f.readlines()

for line in data:
    line = line.strip("\n")
    line = line.split(",")
    count.append(float(line[0]))
    time.append(float(line[1]))
p = {}
p["entry count"] = count
p["time(s)"] = time
df = pd.DataFrame.from_dict(p)

fig, ax = plt.subplots()
df.plot(ax =ax, x="entry count", y="time(s)",figsize = (10,5),title = "Entry count versus Execution time", ylabel="Execution time(s)")
plt.show()

ax.figure.savefig("b.png")