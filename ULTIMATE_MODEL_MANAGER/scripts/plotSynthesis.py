import pandas as pd 
import matplotlib.pyplot as plt
import sys


import pandas as pd 
import matplotlib.pyplot as plt
# This import registers the 3D projection, but is otherwise unused.
from mpl_toolkits.mplot3d import Axes3D  # noqa: F401 unused import
import sys


file = str(sys.argv[1])

f = pd.read_csv(file, header=0, delimiter="\t")

num_variables = f.shape[1]

if (num_variables == 2):

    plt.scatter(f.iloc[:,0], f.iloc[:,1])# s=area, c=colors, alpha=0.5)
    plt.xlabel(f.columns[0])
    plt.ylabel(f.columns[1])
    plt.show()


elif(num_variables == 3):

    fig = plt.figure()
    ax = fig.add_subplot(111, projection='3d')
    ax.scatter(f.iloc[:,0], f.iloc[:,1], f.iloc[:,2])

    ax.set_xlabel(f.columns[0])
    ax.set_ylabel(f.columns[1])
    ax.set_zlabel(f.columns[2])

    plt.show()

print("Done")