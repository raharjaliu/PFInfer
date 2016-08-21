import Model
import Simulation
import copy

import time
import matplotlib.pylab as pl


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
m1.add_rate('KInhibitPU', 2)

m1.add_propensity('ProduceGata1', 'Kpg *(Pu1**-2/(Pu1**-2 + KInhibitGata**-2))')
m1.add_propensity('ProducePu1', 'Kpp*(Gata1**-2/(Gata1**-2 + KInhibitPU**-2))')
m1.add_propensity('DegradeGata1', 'Kdg*Gata1')
m1.add_propensity('DegradePu1', 'Kdp*Pu1')


s1 = Simulation.Simulation(copy.deepcopy(m1))

simulationtime = 1000
trajectory = s1.run_Simulation(simulationtime)

interval = 20
intervaltrajectory =  s1.get_interval(trajectory, interval)

pl.plot(intervaltrajectory['time'],intervaltrajectory['Gata1'],'r')
pl.plot(intervaltrajectory['time'],intervaltrajectory['Pu1'], 'g')
pl.xlim(0.0, intervaltrajectory['time'][-1])
pl.show()


