import jenkins.model.*

def instance = Jenkins.getInstance()

//Build Server Port Configuration
instance.setSlaveAgentPort(41789)

instance.save()
