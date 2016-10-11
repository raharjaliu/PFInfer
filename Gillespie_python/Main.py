
import argparse
import Model
import TreeGenerator
import os.path
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()
parser.add_argument('-m', action='store', dest='modelfile', help='Model file')
parser.add_argument('-o', action='store', dest='outputfile', help='Output file name')
parser.add_argument('-t', action='store', dest='simulationtime', help='Simulation time')
parser.add_argument('-v', nargs='?', action='store', dest='interval', help='Interval time')
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
    tag = neighbor.attrib['name']
    for neighbor2 in neighbor.iter('Change'):
        changemap[neighbor2.attrib['name']] = neighbor2.attrib['expression']
    m1.add_reaction(tag, changemap)

for neighbor in modelroot.iter('Propensity'):
    m1.add_propensity(neighbor.attrib['name'], str(neighbor.attrib['expression']))


treenumber = 3
divisions = 4


generator = TreeGenerator.TreeGenerator(m1, float(simulationtime), outputpath, divisions, treenumber)

if (interval is not None):
        generator.set_interval(interval)

generator.generate() 


        
