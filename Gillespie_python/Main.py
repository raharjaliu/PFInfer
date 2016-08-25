
import argparse
import Model
import Simulation
import copy
import os.path
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()
parser.add_argument('-m', action='store', dest='modelfile', help='Model file')
parser.add_argument('-o', action='store', dest='outputfile', help='Output file name')
parser.add_argument('-t', action='store', dest='simulationtime', help='Simulation time')
parser.add_argument('-v', nargs= '?', action='store', dest='interval', help='Interval time')
results = parser.parse_args()

modelpath = results.modelfile
outputpath = results.outputfile

if(os.path.isfile(outputpath) and os.stat(outputpath).st_size != 0):
    print ('Warning: ' + outputpath + ' is no empty file and is being overwritten')
    
simulationtime = results.simulationtime
interval = results.interval

modeltree = ET.parse(modelpath)
modelroot = modeltree.getroot()

m1 = Model.Model()

for neighbor in modelroot.iter('Species'):
    m1.add_species(neighbor.attrib['name'], float(neighbor.attrib['value']))

for neighbor in modelroot.iter('Constant'):
    m1.add_rate(neighbor.attrib['name'], float(neighbor.attrib['value']))

for neighbor in modelroot.iter('Tunable'):
    m1.add_rate(neighbor.attrib['name'], float(neighbor.attrib['value']))
    
for neighbor in modelroot.iter('Reaction'):
    changemap = {}
    tag = ''
    for neighbor2 in neighbor.iter('Tag'):
        tag = neighbor2.attrib['name']
    for neighbor2 in neighbor.iter('Change'):
        changemap[neighbor2.attrib['name']]= neighbor2.attrib['expression']
    m1.add_reaction(tag,changemap)

for neighbor in modelroot.iter('Propensity'):
    m1.add_propensity(neighbor.attrib['name'], str(neighbor.attrib['expression']))

s1 = Simulation.Simulation(copy.deepcopy(m1))
trajectory = s1.run_Simulation(float(simulationtime))
outputtrajectory = trajectory

if (interval is not None):
    intervaltrajectory =  s1.get_interval(trajectory, float(interval))
    outputtrajectory = intervaltrajectory
    
outfile = open(outputpath, 'w')

firstline =''
for key in outputtrajectory:
    firstline = firstline + '\t' +key
firstline = firstline.strip()
firstline = firstline + '\n'
outfile.write(firstline)

steps = len(outputtrajectory['time'])

for i in range(steps):
    line =''
    for key in outputtrajectory:
        line = line  + '\t' + str(outputtrajectory[key][i])        
    line = line.strip()
    line = line + '\n'
    outfile.write(line)
outfile.close()

'''
import matplotlib.pylab as pl

pl.plot(outputtrajectory['time'],outputtrajectory['Gata1'],'r')
pl.plot(outputtrajectory['time'],outputtrajectory['Pu1'], 'g')
pl.xlim(0.0, outputtrajectory['time'][-1])
pl.show()
'''

