import jenkins.model.*
import hudson.model.*

import com.nirima.jenkins.plugins.docker.*
import com.nirima.jenkins.plugins.docker.launcher.*
import com.nirima.jenkins.plugins.docker.strategy.*

def docker_settings = [:]
docker_settings =
[
    [
        name: 'Docker-Cloud',
        serverUrl: 'http://replaceDockerIP:9800',
        containerCapStr: '20',
        connectionTimeout: 5,
        readTimeout: 15,
        credentialsId: '',
        version: '',
        templates: [
            [
                image: 'hearstat/jenkins-build-base',
                labelString: 'base docker',
                remoteFs: '',
                credentialsId: 'jenkins-docker-server',
                idleTerminationMinutes: '5',
                sshLaunchTimeoutMinutes: '1',
                jvmOptions: '',
                javaPath: '',
                memoryLimit: 0,
                memorySwap: 0,
                cpuShares: 0,
                prefixStartSlaveCmd: '',
                suffixStartSlaveCmd: '',
                instanceCapStr: '10',
                dnsString: '',
                dockerCommand: '',
                lxcConfString: '',
                environmentsString: '',
                volumesString: '',
                volumesFromString: '',
                bindPorts: '22',
                bindAllPorts: false,
                privileged: false,
                tty: false,
                macAddress: ''
            ]
        ]
    ]
]

def dockerClouds = []
docker_settings.each { cloud ->

  def templates = []
  // cloud.templates.each { template ->
  //     def dockerTemplateBase =
  //         new DockerTemplateBase(
  //            template.image,
  //            template.dnsString,
  //            template.dockerCommand,
  //            template.volumesString,
  //            template.volumesFromString,
  //            template.environmentsString,
  //            template.lxcConfString,
  //            template.hostname,
  //            template.memoryLimit,
  //            template.memorySwap,
  //            template.cpuShares,
  //            template.bindPorts,
  //            template.bindAllPorts,
  //            template.privileged,
  //            template.tty,
  //            template.macAddress
  //     )
  //
  //     def dockerTemplate =
  //       new DockerTemplate(
  //         dockerTemplateBase,
  //         template.labelString,
  //         template.remoteFs,
  //         template.remoteFsMapping,
  //         template.instanceCapStr
  //       )
  //
  //     def dockerComputerSSHLauncher = new DockerComputerSSHLauncher(
  //         new hudson.plugins.sshslaves.SSHConnector(22, template.credentialsId, null, null, null, null, null )
  //     )
  //
  //     dockerTemplate.setLauncher(dockerComputerSSHLauncher)
  //
  //     dockerTemplate.setMode(Node.Mode.NORMAL)
  //     dockerTemplate.setNumExecutors(1)
  //     dockerTemplate.setRemoveVolumes(true)
  //     dockerTemplate.setRetentionStrategy(new DockerOnceRetentionStrategy(10))
  //     dockerTemplate.setPullStrategy(DockerImagePullStrategy.PULL_LATEST)
  //
  //     templates.add(dockerTemplate)
  // }

  dockerClouds.add(
    new DockerCloud(cloud.name,
                    templates,
                    cloud.serverUrl,
                    cloud.containerCapStr,
                    cloud.connectTimeout ?: 15, // Well, it's one for the money...
                    cloud.readTimeout ?: 15,    // Two for the show
                    cloud.credentialsId,
                    cloud.version
    )
  )
}

Jenkins.instance.clouds.addAll(dockerClouds)
println 'Configured docker cloud.'
