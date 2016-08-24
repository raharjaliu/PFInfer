
import argparse
import Model
import Simulation
import copy

import time
import numpy as np
import matplotlib.pylab as pl

parser = argparse.ArgumentParser()
parser.add_argument('-m', action='store', dest='modelfile', help='Model file')
parser.add_argument('-o', action='store', dest='outputfile', help='Output file name')
parser.add_argument('-t', action='store', dest='simulationtime', help='Simulation time')
parser.add_argument('-v', action='store', dest='interval', help='Interval time')
results = parser.parse_args()

modelpath = results.modelfile
outputpath = results.outputfile
simulationtime = results.simulationtime
interval = results.interval

m1 = Model.Model()

m1.add_species('Gata1', 1.0)
m1.add_species('Pu1', 1.0)

m1.add_rate('Kpg', 12.0)
m1.add_rate('Kpp', 12.0)
m1.add_rate('Kdg', 0.2)
m1.add_rate('Kdp', 0.2)

m1.add_reaction('ProduceGata1',{'Gata1':'Kpg'})
m1.add_reaction('ProducePu1',{'Pu1':'Kpg'})
m1.add_reaction('DegradeGata1',{'Gata1':'-Kdg*Gata1'})
m1.add_reaction('DegradePu1',{'Pu1':'-Kdp*Pu1'})

m1.add_rate('KInhibitGata', 2)
m1.add_rate('KInhibitPu', 2)

m1.add_propensity('ProduceGata1', 'Kpg *(Pu1**-2/(Pu1**-2 + KInhibitGata**-2))')
m1.add_propensity('ProducePu1', 'Kpp*(Gata1**-2/(Gata1**-2 + KInhibitPu**-2))')
m1.add_propensity('DegradeGata1', 'Kdg*Gata1')
m1.add_propensity('DegradePu1', 'Kdp*Pu1')


s1 = Simulation.Simulation(copy.deepcopy(m1))
trajectory = s1.run_Simulation(float(simulationtime))
intervaltrajectory =  s1.get_interval(trajectory, float(interval))

outfile = open(outputpath, 'w')

steps = len(intervaltrajectory['time'])
print(steps)

outfile.close()

'''
times = []
for i in range(50):
    s2 = Simulation.Simulation(copy.deepcopy(m1))
    t1 = time.time()
    s2.run_Simulation(simulationtime)           
    t2 = time.time()
    times.append(t2-t1)

print(np.mean(times))

pl.plot(intervaltrajectory['time'],intervaltrajectory['Gata1'],'r')
pl.plot(intervaltrajectory['time'],intervaltrajectory['Pu1'], 'g')
pl.xlim(0.0, intervaltrajectory['time'][-1])
pl.show()
'''

