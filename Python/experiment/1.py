import matplotlib.pyplot as plt
import pandas as pd



time = []
size = []
with open("dbSize.csv", "r+") as f:
    data = f.readlines()
print(len(data))

for line in data:
    line = line.rstrip("\n")
    line = line.split(",")
    time.append(float(line[0]))
    size.append(float(line[1]))


with open("dbCount.csv", "r+") as f:
    data = f.readlines()
print(len(data))

time2 = []
count = []
for line in data:
    line = line.rstrip("\n")
    line = line.split(",")
    time2.append(float(line[0]))
    count.append(float(line[1]))


d = {}
d["time(s)"] = time
d["database size(byte)"] = size
e ={}
e["time(s)"] = time2
e["entry count"] = count

df = pd.DataFrame.from_dict(d)
df1 = pd.DataFrame.from_dict(e)

print(df)
print(df1)

fig, ax = plt.subplots()
ax1 = ax.twinx()

df.plot(ax = ax, x = "time(s)", y="database size(byte)", figsize = (10,5), legend = False, title="Time verses Database size and Entry count", ylabel="Database size(byte)")
df1.plot(ax = ax1, x= "time(s)", y="entry count", legend=False, style=["--"], ylabel="Entry count")

line1, label1 = ax.get_legend_handles_labels()
line2, label2 = ax1.get_legend_handles_labels()
ax.legend(line1+line2, label1+label2, loc=0)
plt.show()

ax.figure.savefig("a.png")