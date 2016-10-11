import Model
import random
import math


class Simulation:

    m = Model.Model()
    
    reactions = []
    propensities = []
    
    propensity_update = {}
    reactionlistposition = {}
    
    def __init__(self, inputmodel):
        self.m = inputmodel
        self.reactions = []
        self.propensities = []
        for key in self.m.propensities:
            self.reactions.append(key)
            self.propensities.append(self.m.get_propensity(key))
            
        self.propensity_update = inputmodel.get_propensity_dependence()
        for i in range(len(self.reactions)):
            self.reactionlistposition[self.reactions[i]]=i
            
    def update_propensity(self,updatemap):
        for key in updatemap:
            self.propensities[self.reactionlistposition[key]] = self.m.get_propensity(key)
        return
        
    def run_Simulation (self, time, start):
        timestep = start
        
        outmap = {}
        outmap['absoluteTime'] =[timestep] 
        for species in self.m.species:
            outmap[species] = [self.m.species[species]]
            
        while timestep < (time + start):
            combined_propensities = sum(self.propensities)
            r1 = random.random()
            r2 = random.random()
            step = - ((1.0/combined_propensities)*math.log(r1))
            current_reaction = 0
            chosen = ''
            for i in range(len(self.reactions)):
                current_reaction = current_reaction + self.propensities[i]
                if (r2*combined_propensities < current_reaction):
                    chosen = self.reactions[i]
                    break
            self.m.execute_reaction(chosen, step)
            timestep = timestep + step
            
            update = self.propensity_update[chosen]
            self.update_propensity(update)
            
            outmap['absoluteTime'].append(timestep)
            for species in self.m.species:
                outmap[species].append(self.m.species[species])
            print(timestep)
        return outmap  
    
    def get_interval(self, trajectory, interval, start):
        
        outmap = {}
        for key in trajectory:
            outmap[key] = [trajectory[key][0]]
        
        nextinterval = start + interval
        
        for i in range(len(trajectory['absoluteTime'])):
            if trajectory['absoluteTime'][i]>nextinterval:
                nextinterval = nextinterval + interval
                for key in trajectory:
                    outmap[key].append(trajectory[key][i])     
        return outmap
        