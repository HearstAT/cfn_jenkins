import jenkins.model.*

def instance = Jenkins.getInstance()

//Master Executors Lock to 1
instance.setNumExecutors(1)

instance.save()
