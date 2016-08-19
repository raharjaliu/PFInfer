import Model
import Model2
import Simulation
import Simulation2
import time
import copy
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

m2 = Model2.Model2()

m2.add_species('Gata1', 1.0)
m2.add_species('Pu1', 1.0)

m2.add_rate('Kpg', 12.0)
m2.add_rate('Kpp', 12.0)
m2.add_rate('Kdg', 0.2)
m2.add_rate('Kdp', 0.2)

m2.add_reaction('ProduceGata1',{'Gata1':'Kpg'})
m2.add_reaction('ProducePu1',{'Pu1':'Kpg'})
m2.add_reaction('DegradeGata1',{'Gata1':'-Kdg*Gata1'})
m2.add_reaction('DegradePu1',{'Pu1':'-Kdp*Pu1'})

m2.add_rate('KInhibitGata', 2)
m2.add_rate('KInhibitPU', 2)

m2.add_propensity('ProduceGata1', 'Kpg *(Pu1**-2/(Pu1**-2 + KInhibitGata**-2))')
m2.add_propensity('ProducePu1', 'Kpp*(Gata1**-2/(Gata1**-2 + KInhibitPU**-2))')
m2.add_propensity('DegradeGata1', 'Kdg*Gata1')
m2.add_propensity('DegradePu1', 'Kdp*Pu1')

s2 = Simulation2.Simulation2(copy.deepcopy(m2))

print(s2.propensity_update)

'''
oldtime = []
for i in range(100):
    s1 = Simulation.Simulation(copy.deepcopy(m1))
    t1 = time.time()
    s1.run_Simulation(10000)
    t2 = time.time()
    oldtime.append(t2-t1)

newtime = []
for i in range(100):
    s2 = Simulation.Simulation(copy.deepcopy(m2))
    t1 = time.time()
    s2.run_Simulation(10000)
    t2 = time.time()
    newtime.append(t2-t1)
    
oldtime.sort()
newtime.sort()

print(oldtime)
print(newtime)

pl.hist(oldtime)
pl.hist(newtime)
pl.show()
'''
