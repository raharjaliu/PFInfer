import Model
import random
import math
import matplotlib.pylab as pl

class Simulation:

    m = Model.Model()

    def __init__(self, inputmodel):
        self.m = inputmodel
        
    def run_Simulation (self, time):
        timestep = 0
        reactions = []
        
        timeline = [0]
        timegata = [self.m.species['Gata1']]
        timepu = [self.m.species['Pu1']]
        
        for key in self.m.propensities:
            reactions.append(key)
        while timestep < time:
            combined_propensities = 0
            propensities = []
            for tag in reactions:
                propensity = self.m.get_propensity(tag)
                propensities.append(propensity)
                combined_propensities = combined_propensities + propensity
            r1 = random.random()
            r2 = random.random()
            step = - ((1.0/combined_propensities)*math.log(r1))
            current_reaction = 0
            chosen = ''
            for i in range(len(reactions)):
                current_reaction = current_reaction + propensities[i]
                if (r2*combined_propensities < current_reaction):
                    chosen = reactions[i]
                    break
            self.m.execute_reaction(chosen, step)
            timestep = timestep + step
            
            timeline.append(timestep)
            timegata.append(self.m.species['Gata1'])
            timepu.append(self.m.species['Pu1'])
        
        pl.plot(timeline,timegata,'r')
        pl.plot(timeline,timepu, 'g')
        pl.xlim(0.0, time)
        pl.show()
            