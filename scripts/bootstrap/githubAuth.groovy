import jenkins.model.*
import hudson.security.*
import org.jenkinsci.plugins.GithubSecurityRealm
import org.jenkinsci.plugins.GithubAuthorizationStrategy


def instance = Jenkins.getInstance()

println "--> Setting Up Github Oauth"

//Github Auth Configuration
githubWebUri = 'https://github.com'
githubApiUri = 'https://api.github.com'
clientID = 'replaceClientID'
clientSecret = 'replaceClientSecret'
oauthScopes = 'read:org,user:email'
SecurityRealm github_realm = new GithubSecurityRealm(githubWebUri, githubApiUri, clientID, clientSecret, oauthScopes)
//check for equality, no need to modify the runtime if no settings changed
if(!github_realm.equals(instance.getSecurityRealm())) {
  println "Saving Github Security Realm Configuration"
  instance.setSecurityRealm(github_realm)
  instance.save()
}

//Set Github Users as Admins
String adminUserNames = 'replaceGithubAdmins'
//Participant in Organization
String organizationNames = 'replaceGithubOrg'
//Use Github repository permissions
boolean useRepositoryPermissions = true
//Grant READ permissions to all Authenticated Users
boolean authenticatedUserReadPermission = true
//Grant CREATE Job permissions to all Authenticated Users
boolean authenticatedUserCreateJobPermission = true
//Grant READ permissions for /github-webhook
boolean allowGithubWebHookPermission = true
//Grant READ permissions for /cc.xml
boolean allowCcTrayPermission = false
//Grant READ permissions for Anonymous Users
boolean allowAnonymousReadPermission = false
//Grant ViewStatus permissions for Anonymous Users
boolean allowAnonymousJobStatusPermission = false

AuthorizationStrategy github_authorization = new GithubAuthorizationStrategy(adminUserNames,
    authenticatedUserReadPermission,
    useRepositoryPermissions,
    authenticatedUserCreateJobPermission,
    organizationNames,
    allowGithubWebHookPermission,
    allowCcTrayPermission,
    allowAnonymousReadPermission,
    allowAnonymousJobStatusPermission)

//check for equality, no need to modify the runtime if no settings changed
if(!github_authorization.equals(instance.getAuthorizationStrategy())) {
  println "Saving Github Auth Configuration"
  instance.setAuthorizationStrategy(github_authorization)
  instance.save()
}
