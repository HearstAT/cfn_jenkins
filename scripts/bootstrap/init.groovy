import jenkins.model.*
import hudson.security.*
import org.jenkinsci.plugins.GithubSecurityRealm
import org.jenkinsci.plugins.GithubAuthorizationStrategy
import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import com.cloudbees.plugins.credentials.SystemCredentialsProvider

def instance = Jenkins.getInstance()

permissions = new hudson.security.GlobalMatrixAuthorizationStrategy()
permissions.add(Jenkins.ADMINISTER, 'admin')
permissions.add(hudson.model.View.READ, 'anonymous')
permissions.add(hudson.model.Item.READ, 'anonymous')
permissions.add(Jenkins.READ, 'anonymous')

instance.authorizationStrategy = permissions

instance.save()

pm = instance.pluginManager
uc = instance.updateCenter

pm.plugins.each { plugin ->
  plugin.disable()
}

deployed = false
def activatePlugin(plugin) {
  if (! plugin.isEnabled()) {
    plugin.enable()
    deployed = true
  }

  plugin.getDependencies().each {
    activatePlugin(pm.getPlugin(it.shortName))
  }
}

[
  "git",
  "github-oauth",
  "greenballs",
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
  if (! pm.getPlugin(it)) {
    deployment = uc.getPlugin(it).deploy(true)
    deployment.get()
  }
  activatePlugin(pm.getPlugin(it))
}

if (deployed) {
  instance.restart()
}
