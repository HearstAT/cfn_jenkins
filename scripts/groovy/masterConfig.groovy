import jenkins.model.*
import hudson.model.*

def instance = Jenkins.getInstance()

//Master Executors Lock to 1
instance.setNumExecutors(1)
instance.setlabelString('master')
instance.setMode(hudson.model.Node.Mode.EXCLUSIVE)

instance.save()
