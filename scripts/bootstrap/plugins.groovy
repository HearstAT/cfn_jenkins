import jenkins.model.*
import hudson.security.*

def instance = Jenkins.getInstance()

println "--> Installing and Updating Plugins"

pluginManager = instance.pluginManager
updateCenter = instance.updateCenter

pluginManager.plugins.each { plugin ->
  plugin.disable()
}

deployed = false
def activatePlugin(plugin) {
  if (! plugin.isEnabled()) {
    plugin.enable()
    deployed = true
  }

  plugin.getDependencies().each {
    activatePlugin(pluginManager.getPlugin(it.shortName))
  }
}

[
  "git",
  "github-oauth",
  "greenballs",
  "fstrigger",
  "workflow-aggregator",
  "support-core",
  "run-condition",
  "conditional-buildstep",
  "aws-java-sdk",
  "copyartifact",
  "compress-artifacts",
  "copy-to-slave",
  "slave-status",
  "ansicolor",
  "antisamy-markup-formatter",
  "git-changelog",
  "git-client",
  "gravatar",
  "jenkins-multijob-plugin",
  "matrix-auth",
  "matrix-project",
  "matrix-reloaded",
  "parameterized-trigger",
  "plain-credentials",
  "ruby-runtime",
  "run-condition",
  "scm-api",
  "ssh-credentials",
  "ssh-slaves",
  "s3",
  "warnings",
  "slack",
  "disk-usage",
  "embeddable-build-status",
  "docker-plugin",
  "github",
  "ghprb"
].each {
  if (! pluginManager.getPlugin(it)) {
    deployment = updateCenter.getPlugin(it).deploy(true)
    deployment.get()
  }
  activatePlugin(pluginManager.getPlugin(it))
}

if (deployed) {
  instance.restart()
}
