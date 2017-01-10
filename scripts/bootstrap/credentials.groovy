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
Boolean foundGithub=false
Boolean foundSlack=false
Boolean foundGithubToken=false

// Username & Password Credentials
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
    println 'Added Docker Cloud Credentials'
}

system_creds.getCredentials().each{
    if('github-login-creds'.equals(it.getId())) {
        foundGithub=true
    }
}
if(!foundGithub) {
    Map<Domain, List<Credentials>> domainCredentialsMap = system_creds.getDomainCredentialsMap()
    UsernamePasswordCredentialsImpl creds =
        new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,
                                            'github-login-creds',
                                            'Github Login Credentials',
                                            'replaceGitLogin',
                                            'replaceGitToken')
    domainCredentialsMap[Domain.global()].add(creds)
    system_creds.save()
    println 'Added Github Login Credentials'
}

// Secret Text or Token Credentials
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
    println 'Added Slack Token'
}

system_creds.getCredentials().each{
    if('github-token'.equals(it.getId())) {
        foundGithubToken=true
    }
}
if(!foundGithubToken) {
  secretText = new StringCredentialsImpl(CredentialsScope.GLOBAL,
                                         'github-token',
                                         'Github API Token',
                                         Secret.fromString("replaceGitToken"))
    store.addCredentials(domain, secretText)
    println 'Added Github Token'
}
