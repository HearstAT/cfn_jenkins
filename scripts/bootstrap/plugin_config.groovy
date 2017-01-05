import jenkins.*
import jenkins.model.*
import hudson.security.*
import com.cloudbees.plugins.credentials.*
import com.nirima.jenkins.plugins.docker.DockerCloud
import com.nirima.jenkins.plugins.docker.DockerTemplate
import net.sf.json.JSONArray
import net.sf.json.JSONObject
import org.kohsuke.stapler.StaplerRequest

// Variable Set
instance = Jenkins.getInstance()
git_desc = instance.getDescriptor("hudson.plugins.git.GitSCM")

//Git Configuration
git_desc.setGlobalConfigName("Jenkins")
git_desc.setGlobalConfigEmail("replaceGitEmail")

git_desc.save()

//Slack Configuration
def slack = jenkins.getDescriptorByType(jenkins.plugins.slack.SlackNotifier.DescriptorImpl)
slack.teamDomain = "replaceSlackTeam"
slack.authTokenCredentialId = "slack-integration-token"
slack.sendAs = "Jenkins"
slack.room = "replaceSlackRoom"
slack.save()

//Docker Configuration
JSONObject docker_settings = new JSONObject()
docker_settings.putAll([
  name: 'docker-build-server',
  serverUrl: 'http://replaceDockerIP:9800',
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
