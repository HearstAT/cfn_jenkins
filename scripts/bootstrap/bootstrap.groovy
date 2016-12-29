import jenkins.model.*
import hudson.security.*
import org.jenkinsci.plugins.GithubSecurityRealm
import org.jenkinsci.plugins.GithubAuthorizationStrategy
import com.cloudbees.plugins.credentials.*
import com.nirima.jenkins.plugins.docker.DockerCloud
import com.nirima.jenkins.plugins.docker.DockerTemplate
import net.sf.json.JSONArray
import net.sf.json.JSONObject
import org.kohsuke.stapler.StaplerRequest

def instance = Jenkins.getInstance()
def jenkinsLocationConfiguration = JenkinsLocationConfiguration.get()
def git_desc = instance.getDescriptor("hudson.plugins.git.GitSCM")


//Github Auth Configuration
String githubWebUri = 'https://github.com'
String githubApiUri = 'https://api.github.com'
String clientID = 'replaceClientID'
String clientSecret = 'replaceClientSecret'
String oauthScopes = 'read:org'
SecurityRealm github_realm = new GithubSecurityRealm(githubWebUri, githubApiUri, clientID, clientSecret, oauthScopes)
//check for equality, no need to modify the runtime if no settings changed
if(!github_realm.equals(instance.getSecurityRealm())) {
  println "Saving Github Security Realm Configuration"
  instance.setSecurityRealm(github_realm)
  instance.save()
}

String adminUserNames = 'admin'
//Participant in Organization
String organizationNames = 'replaceGithubOrg'
//Use Github repository permissions
boolean useRepositoryPermissions = true
//Grant READ permissions to all Authenticated Users
boolean authenticatedUserReadPermission = false
//Grant CREATE Job permissions to all Authenticated Users
boolean authenticatedUserCreateJobPermission = false
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

//Git Configuration
git_desc.setGlobalConfigName("Jenkins")
git_desc.setGlobalConfigEmail("replaceGitEmail")

git_desc.save()

//Slack Configuration
def slack = instance.getExtensionList(jenkins.plugins.slack.SlackNotifier.DescriptorImpl.class)[0]
def params = [
  slackTeamDomain: "replaceSlackTeam",
  slackToken: "repleaceSlackToken",
  slackRoom: "replaceSlackRoom",
  slackBuildServerUrl: "https://jenkins.replaceDomain",
  slackSendAs: "Jenkins"
]
def req = [
  getParameter: { name -> params[name] }
] as org.kohsuke.stapler.StaplerRequest
slack.configure(req, null)
slack.save()

//Privacy Configuration
if(Jenkins.instance.isUsageStatisticsCollected()) {
    println "Disabling anonymous usage statistics"
    Jenkins.instance.setNoUsageStatistics(true)
    hasConfigBeenUpdated = true
}

//Configure Credentials

SystemCredentialsProvider system_creds = SystemCredentialsProvider.getInstance()
Boolean foundDocker=false

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

//Docker Configuration
JSONObject docker_settings = new JSONObject()
docker_settings.putAll([
  name: 'docker-build-server',
  serverUrl: 'replaceServerUrl',
  containerCapStr: '5',
  connectionTimeout: 5,
  readTimeout: 15,
  credentialsId: '',
  version: '',
  templates: [
    [
      image: 'hearstat/jenkins-build-base',
      labelString: 'test',
      remoteFs: '/var/lib/jenkins',
      credentialsId: 'jenkins-docker-server',
      idleTerminationMinutes: '5',
      sshLaunchTimeoutMinutes: '1',
      jvmOptions: '',
      javaPath: '',
      memoryLimit: 0,
      cpuShares: 0,
      prefixStartSlaveCmd: '',
      suffixStartSlaveCmd: '',
      instanceCapStr: '5',
      dnsString: '',
      dockerCommand: '',
      volumesString: '',
      volumesFromString: '',
      hostname: '',
      bindPorts: '22',
      bindAllPorts: false,
      privileged: false,
      tty: false,
      macAddress: ''
    ],
    [
      image: 'hearstat/jenkins-build-chef',
      labelString: 'foodcritic cookbook chef',
      remoteFs: '/var/lib/jenkins',
      credentialsId: 'jenkins-docker-server',
      idleTerminationMinutes: '5',
      sshLaunchTimeoutMinutes: '1',
      jvmOptions: '',
      javaPath: '',
      memoryLimit: 0,
      cpuShares: 0,
      prefixStartSlaveCmd: '',
      suffixStartSlaveCmd: '',
      instanceCapStr: '5',
      dnsString: '',
      dockerCommand: '',
      volumesString: '',
      volumesFromString: '',
      hostname: '',
      bindPorts: '22',
      bindAllPorts: false,
      privileged: false,
      tty: false,
      macAddress: ''
    ],
  ]
])

def bindJSONToList( Class type, Object src) {
  if(type == DockerTemplate){
    ArrayList<DockerTemplate> r = new ArrayList<DockerTemplate>();
    if (src instanceof JSONObject) {
      JSONObject temp = (JSONObject) src;
        r.add(
          new DockerTemplate(
            temp.optString("image"),
            temp.optString("labelString"),
            temp.optString("remoteFs"),
            temp.optString("remoteFsMapping"),
            temp.optString("credentialsId"),
            temp.optString("idleTerminationMinutes"),
            temp.optString("sshLaunchTimeoutMinutes"),
            temp.optString("jvmOptions"),
            temp.optString("javaPath"),
            temp.optInt("memoryLimit"),
            temp.optInt("cpuShares"),
            temp.optString("prefixStartSlaveCmd"),
            temp.optString("suffixStartSlaveCmd"),
            temp.optString("instanceCapStr"),
            temp.optString("dnsString"),
            temp.optString("dockerCommand"),
            temp.optString("volumesString"),
            temp.optString("volumesFromString"),
            temp.optString("environmentsString"),
            temp.optString("lxcConfString"),
            temp.optString("hostname"),
            temp.optString("bindPorts"),
            temp.optBoolean("bindAllPorts"),
            temp.optBoolean("privileged"),
            temp.optBoolean("tty"),
            temp.optString("macAddress")
          )
        );
      }
      if (src instanceof JSONArray) {
        JSONArray json_array = (JSONArray) src;
        for (Object o : json_array) {
          if (o instanceof JSONObject) {
            JSONObject temp = (JSONObject) o;
              r.add(
                new DockerTemplate(
                  temp.optString("image"),
                  temp.optString("labelString"),
                  temp.optString("remoteFs"),
                  temp.optString("remoteFsMapping"),
                  temp.optString("credentialsId"),
                  temp.optString("idleTerminationMinutes"),
                  temp.optString("sshLaunchTimeoutMinutes"),
                  temp.optString("jvmOptions"),
                  temp.optString("javaPath"),
                  temp.optInt("memoryLimit"),
                  temp.optInt("cpuShares"),
                  temp.optString("prefixStartSlaveCmd"),
                  temp.optString("suffixStartSlaveCmd"),
                  temp.optString("instanceCapStr"),
                  temp.optString("dnsString"),
                  temp.optString("dockerCommand"),
                  temp.optString("volumesString"),
                  temp.optString("volumesFromString"),
                  temp.optString("environmentsString"),
                  temp.optString("lxcConfString"),
                  temp.optString("hostname"),
                  temp.optString("bindPorts"),
                  temp.optBoolean("bindAllPorts"),
                  temp.optBoolean("privileged"),
                  temp.optBoolean("tty"),
                  temp.optString("macAddress")
                )
              );
          }
        }
      }
      return r;
  }
  if(type == DockerCloud){
    ArrayList<DockerCloud> r = new ArrayList<DockerCloud>();
    if (src instanceof JSONObject) {
      JSONObject temp = (JSONObject) src;
      r.add(
        new DockerCloud(
          temp.optString("name"),
          bindJSONToList(DockerTemplate.class, temp.optJSONArray("templates")),
          temp.optString("serverUrl"),
          temp.optString("containerCapStr"),
          temp.optInt("connectTimeout", 5),
          temp.optInt("readTimeout", 15),
          temp.optString("credentialsId"),
          temp.optString("version")
        )
      );
    }
    if (src instanceof JSONArray) {
      JSONArray json_array = (JSONArray) src;
      for (Object o : json_array) {
        if (o instanceof JSONObject) {
          JSONObject temp = (JSONObject) src;
          r.add(
            new DockerCloud(
              temp.optString("name"),
              bindJSONToList(DockerTemplate.class, temp.optJSONArray("templates")),
              temp.optString("serverUrl"),
              temp.optString("containerCapStr"),
              temp.optInt("connectTimeout", 5),
              temp.optInt("readTimeout", 15),
              temp.optString("credentialsId"),
              temp.optString("version")
            )
          );
        }
      }
    }
    return r;
  }
}

def req = [
  bindJSONToList: { Class type, Object src ->
    bindJSONToList(type, src)
  }
] as org.kohsuke.stapler.StaplerRequest

if(!Jenkins.instance.clouds.getByName('docker-build-server')) {
  println 'Adding Docker Cloud'
  Jenkins.instance.clouds.addAll(req.bindJSONToList(DockerCloud.class, docker_settings))
}
