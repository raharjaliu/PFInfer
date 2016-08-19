class Model2:
    
    species = {}
    rates = {}
    reactions = {}   
    propensities = {} 
        
    def add_species(self, tag, concentration):
        if tag in globals():
            print ('Warning: ' + tag + ' overwriting existing variable')
        self.species[tag] = concentration
        globals()[tag] = concentration
        
    def add_rate(self, tag, constant):
        if tag in globals():
            print ('Warning: ' + tag + ' overwriting existing variable')
        self.rates[tag] = constant
        globals()[tag] = constant
    
    def add_reaction(self, tag, expressionmap):
        if (tag in self.reactions):
            print ('Warning: ' + tag + ' overwriting existing reaction')
        self.reactions[tag] = expressionmap
        
    def add_propensity(self, tag, expression):
        if (tag in self.propensities):
            print ('Warning: ' + tag + ' overwriting existing propensity')
        self.propensities[tag] = expression
         
    def get_reaction(self, tag, time):
        outmap = {}
        if not (tag in self.reactions):
            print ('Warning: ' + tag + ' is no reaction')
            return outmap
        expressionmap = self.reactions[tag]
        for key in expressionmap:
            expression = str(expressionmap[key])
            outmap[key] = time * eval(expression)
        return outmap  
    
    def execute_reaction (self,tag, time):
        changes = self.get_reaction(tag, time)
        for key in changes:
            self.species[key] = self.species[key] + changes[key]
            update = {key:self.species[key]}
            globals().update(update)
                       
    def get_propensity(self, tag):
        if not (tag in self.propensities):
            print ('Warning: ' + tag + ' has no propensity function')
            return
        expression = str(self.propensities[tag])
        return eval(expression)
    
    def get_propensity_dependence(self):
        dependencemap = {}                                                                                                             
        for reaction in self.reactions:
            reactionmap = {}
            for name in self.reactions[reaction]:
                for prop in self.propensities:
                    if name in self.propensities[prop]:
                        reactionmap[prop] = True
            dependencemap[reaction]=reactionmap
        return dependencemap
        