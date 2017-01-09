import jenkins.*
import jenkins.model.*
import hudson.security.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import org.jenkinsci.plugins.plaincredentials.*
import org.jenkinsci.plugins.plaincredentials.impl.*
import hudson.util.Secret

// Variable Set
instance = Jenkins.getInstance()
domain = Domain.global()
store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

//Configure Credentials
SystemCredentialsProvider system_creds = SystemCredentialsProvider.getInstance()
Boolean foundDocker=false
Boolean foundSlack=false
Boolean foundGithub=false

system_creds.getCredentials().each{
    if('jenkins-docker-server'.equals(it.getId())) {
        foundDocker=true
    }
}
if(!foundDocker) {
    Map<Domain, List<Credentials>> domainCredentialsMap = system_creds.getDomainCredentialsMap()
    UsernamePasswordCredentialsImpl creds =
        new UsernamePasswordCredentialsImpl(CredentialsScope.SYSTEM,
                                            'jenkins-docker-server',
                                            'Jenkins slave docker container credentials.',
                                            'jenkins',
                                            'jenkins')
    domainCredentialsMap[Domain.global()].add(creds)
    system_creds.save()
    println 'Added docker cloud credentials.'
}

system_creds.getCredentials().each{
    if('slack-integration-token'.equals(it.getId())) {
        foundSlack=true
    }
}
if(!foundSlack) {
  secretText = new StringCredentialsImpl(CredentialsScope.GLOBAL,
                                         'slack-integration-token',
                                         'Slack Integration Token',
                                         Secret.fromString("replaceSlackToken"))
    store.addCredentials(domain, secretText)
    println 'Added slack token'
}

system_creds.getCredentials().each{
    if('github-token'.equals(it.getId())) {
        foundGithub=true
    }
}
if(!foundGithub) {
  secretText = new StringCredentialsImpl(CredentialsScope.GLOBAL,
                                         'github-token',
                                         'Github API Token',
                                         Secret.fromString("replaceGitToken"))
    store.addCredentials(domain, secretText)
    println 'Added github token'
}
