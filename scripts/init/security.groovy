import jenkins.model.*
import hudson.security.*

def instance = Jenkins.getInstance()

//Privacy Configuration
if(Jenkins.instance.isUsageStatisticsCollected()) {
    println "Disabling anonymous usage statistics"
    Jenkins.instance.setNoUsageStatistics(true)
    hasConfigBeenUpdated = true
}

//Disable CLI access over /cli URL
def removal = { lst ->
    lst.each { x ->
        if(x.getClass().name.contains("CLIAction")) {
            lst.remove(x)
        }
    }
}

removal(instance.getExtensionList(RootAction.class))
removal(instance.actions)
