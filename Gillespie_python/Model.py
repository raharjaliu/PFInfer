class Model:
    
    species = {}
    rates = {}
    reactions = {}   
    propensities = {}
    
    def add_species(self, tag, concentration):
        if (tag in self.species):
            print ('Warning: ' + tag + ' overwriting existing species')
        self.species[tag] = concentration

    def add_rate(self, tag, constant):
        if (tag in self.rates):
            print ('Warning: ' + tag + ' overwriting existing rate')
        self.rates[tag] = constant
        
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
            for name in self.species:
                expression = expression.replace(name,str(self.species[name]))
            for name in self.rates:
                expression = expression.replace(name,str(self.rates[name]))
            outmap[key] = time * eval(expression)
        return outmap
    
    def execute_reaction (self,tag, time):
        changes = self.get_reaction(tag, time)
        for key in changes:
            self.species[key] = self.species[key] + changes[key]
    
    def get_propensity(self, tag):
        if not (tag in self.propensities):
            print ('Warning: ' + tag + ' has no propensity function')
            return
        expression = str(self.propensities[tag])
        for name in self.species:
            expression = expression.replace(name,str(self.species[name]))
        for name in self.rates:
            expression = expression.replace(name,str(self.rates[name]))
        return eval(expression)
            
        
        
        
        
        
        
        