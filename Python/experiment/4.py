import numpy as np
from sklearn.linear_model import LinearRegression
import matplotlib.pyplot as plt
import pandas as pd

np.set_printoptions(suppress=True)


size = []
with open("dbSize.csv", "r+") as f:
    data = f.readlines()
print(len(data))

for line in data:
    line = line.rstrip("\n")
    line = line.split(",")
    size.append(float(line[1]))


with open("dbCount.csv", "r+") as f:
    data = f.readlines()

count = []
for line in data:
    line = line.rstrip("\n")
    line = line.split(",")
    count.append(float(line[1]))

d = {}
d["count of entries"] = count
d["database size(byte)"] = size




model = LinearRegression().fit((np.array(count).reshape(-1,1)), np.array(size))
print(model.score((np.array(count).reshape(-1,1)), np.array(size)))
print(model.intercept_)
print(model.coef_)

size2 = []
for c in count:
    size2.append(model.coef_[0] * c + model.intercept_)
d[f"s = {model.coef_[0]:.8f}c + {model.intercept_:.8f}, coefficient = {model.score((np.array(count).reshape(-1,1)), np.array(size)):.8f}"] = size2
df = pd.DataFrame.from_dict(d)
print(df)
for index, row in df.iterrows():
    if row["database size(byte)"] == 184320.0:
        df = df.drop(index = index)



fig, ax = plt.subplots()

df.plot(ax = ax, x = "count of entries", figsize = (10,5), title="Count of entries vs. size of the database", ylabel="Database size(byte)",style = ["-","--"])
plt.show()

ax.figure.savefig("d.png")