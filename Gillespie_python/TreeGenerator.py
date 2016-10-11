import Model
import Simulation

import matplotlib.pylab as pl

class TreeGenerator:
    
    treesum = -1
    m = Model.Model()
    simulationtime = 0
    outputfile = ''
    
    
    interval = None
    divisons = 0;
    
    def __init__(self, model, timeframe, output, divisions, treesum):
        self.m = model
        self.simulationtime = timeframe
        self.outputfile = output
        self.treesum = treesum
        self.divisons = divisions

    def set_interval (self, inter):
        self.interval = inter
    
    def set_splits (self, split):
        self.divisons = split
        
    def generate(self):
        
        step = self.simulationtime
        if (self.divisons is not 0):
            step = self.simulationtime/float(1+self.divisons)
        
        outfile = open(self.outputfile, 'w') 
        for i in range (self.treesum):
                self.rec_branch_sim(step, self.divisons,1,self.m,i, outfile, 0.0)
        outfile.close()
            
    def rec_branch_sim (self,step,splits,cellnr,model, tree, out,start):
      
        s1 = Simulation.Simulation(model.get_copy())
        trajectory = s1.run_Simulation(float(step),start)
        outputtrajectory = trajectory
        
        if (self.interval is not None):
            intervaltrajectory = s1.get_interval(trajectory, float(self.interval), start)
            outputtrajectory = intervaltrajectory
        
        if(cellnr == 1 and tree == 0):
            firstline = ''
            for key in outputtrajectory:
                name = key
                if (name == "Pu1"):
                    name = "auto_w01"
                if (name == "Gata1"):
                    name = "auto_w02"
                if (name == "absoluteTime"):
                    name = "absoluteTime"
                firstline = firstline + ',' + name
            firstline = firstline + ',cellNr,treeID'  
            firstline = firstline.strip()
            firstline = firstline[1:]
            firstline = firstline + '\n'
            out.write(firstline)
    
        steps = len(outputtrajectory['absoluteTime'])
        
        for i in range(steps):
            line = ''
            for key in outputtrajectory:
                line = line + ',' + str(outputtrajectory[key][i])
            line = line + ',' + str(cellnr)
            line = line + ',' + str(tree)
            line = line.strip()
            line = line[1:]
            line = line + '\n'
            out.write(line)
        
        if (False):
            pl.plot(outputtrajectory['absoluteTime'], outputtrajectory['Gata1'], 'r')
            pl.plot(outputtrajectory['absoluteTime'], outputtrajectory['Pu1'], 'g')
            pl.xlim(start, outputtrajectory['absoluteTime'][-1])
            pl.show()

        start = outputtrajectory['absoluteTime'][-1]

        if (splits is not 0):
            self.rec_branch_sim (step,splits-1,2*cellnr,s1.m.get_copy(), tree, out, start)
            self.rec_branch_sim (step,splits-1,(2*cellnr)+1,s1.m.get_copy(), tree, out, start)
            
