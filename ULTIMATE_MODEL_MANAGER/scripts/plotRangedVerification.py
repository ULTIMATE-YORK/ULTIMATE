import pandas as pd 
import matplotlib.pyplot as plt
import sys


import pandas as pd 
import matplotlib.pyplot as plt
# This import registers the 3D projection, but is otherwise unused.
from mpl_toolkits.mplot3d import Axes3D  # noqa: F401 unused import
import sys


file = str(sys.argv[1])
x = str(sys.argv[2])
y = str(sys.argv[3])
z = str(sys.argv[4])

f = pd.read_csv(file, header=0, delimiter="\t")

if (z == ""):

    plt.scatter(f[x], f[y])
    plt.xlabel(x)
    plt.ylabel(y)
    plt.show()


else:
    paretoFront = str(sys.argv[1])

    f = pd.read_csv(paretoFront, header=0, delimiter="\t")
    f = f.apply(pd.to_numeric, errors='coerce')

    fig = plt.figure()
    ax = fig.add_subplot(111, projection='3d')
    
    ax.scatter(f[x], f[y], f[z])
    ax.set_xlabel(x)
    ax.set_ylabel(y)
    ax.set_zlabel(z)

    plt.show()

print("Done")