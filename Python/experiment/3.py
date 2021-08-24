import numpy as np
from sklearn.linear_model import LinearRegression
import matplotlib.pyplot as plt
import pandas as pd


np.set_printoptions(suppress=True)
x = []
y = []
with open("statisticTime.csv", "r+") as f:
    data = f.readlines()
for line in data:
    line = line.rstrip("\n")
    line = line.split(",")
    if float(line[1]) <= 0.054:
        x.append(float(line[0]))
        y.append(float(line[1]))

x = np.array(x).reshape((-1,1))
y = np.array(y)

model = LinearRegression().fit(x, y)
print(model.score(x, y))
print(model.intercept_)
print(model.coef_)

count = []
time = []
with open("statisticTime - 副本.csv","r+") as f:
    data = f.readlines()

for line in data:
    line = line.strip("\n")
    line = line.split(",")
    if float(line[1]) <= 0.054:
        count.append(float(line[0]))
        time.append(float(line[1]))
p = {}
p["entry count"] = count
p["time(s)"] = time

y2 = []
for i in count:
    y2.append(model.coef_[0] * i + model.intercept_)
p[f"t = {model.coef_[0]:.8f}c + {model.intercept_:.8f}, coefficient = {model.score(x, y):.8f}"] = y2

df = pd.DataFrame.from_dict(p)

print(df)
fig, ax = plt.subplots()
df.plot(ax =ax, x="entry count", figsize = (10,5),title = "Entry count versus Execution time With Regression", style = ["-","--"])

plt.show()

ax.figure.savefig("3.png")



