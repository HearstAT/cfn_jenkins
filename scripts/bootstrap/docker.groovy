import jenkins.model.*
import hudson.model.*

import com.nirima.jenkins.plugins.docker.*
import com.nirima.jenkins.plugins.docker.launcher.*
import com.nirima.jenkins.plugins.docker.strategy.*

import hudson.plugins.sshslaves.SSHConnector

def docker_settings = [:]
docker_settings =
[
    [
        // Docker Cloud
        name: 'Docker-Build-Server',
        serverUrl: 'tcp://replaceDockerIP:9800',
        containerCapStr: '20',
        connectTimeout: 5,
        readTimeout: 15,
        credentialsId: '',
        version: '',
        templates: [
            [
                // Docker Template Base
                image: 'hearstat/jenkins-build-base:debian',
                dnsString: '',
                network: '',
                dockerCommand: '',
                volumesString: '',
                volumesFromString: '',
                environmentsString: '',
                lxcConfString: '',
                hostname: '',
                memoryLimit: 0,
                memorySwap: 0,
                cpuShares: 0,
                bindPorts: '22',
                bindAllPorts: false,
                privileged: false,
                tty: false,
                macAddress: '',
                // Docker Template
                labelString: 'base docker debian',
                remoteFs: '',
                remoteFsMapping: '',
                instanceCapStr: '2',
                // SSH Launcher
                credentialsId: 'jenkins-docker-server'
            ],
            [
                // Docker Template Base
                image: 'hearstat/jenkins-build-base:alpine',
                dnsString: '',
                network: '',
                dockerCommand: '',
                volumesString: '',
                volumesFromString: '',
                environmentsString: '',
                lxcConfString: '',
                hostname: '',
                memoryLimit: 0,
                memorySwap: 0,
                cpuShares: 0,
                bindPorts: '22',
                bindAllPorts: false,
                privileged: false,
                tty: false,
                macAddress: '',
                // Docker Template
                labelString: 'base docker alpine',
                remoteFs: '',
                remoteFsMapping: '',
                instanceCapStr: '2',
                // SSH Launcher
                credentialsId: 'jenkins-docker-server'
            ],
            [
                // Docker Template Base
                image: 'hearstat/jenkins-build-foodcritic',
                dnsString: '',
                network: '',
                dockerCommand: '',
                volumesString: '',
                volumesFromString: '',
                environmentsString: '',
                lxcConfString: '',
                hostname: '',
                memoryLimit: 0,
                memorySwap: 0,
                cpuShares: 0,
                bindPorts: '22',
                bindAllPorts: false,
                privileged: false,
                tty: false,
                macAddress: '',
                // Docker Template
                labelString: 'chef foodcritic',
                remoteFs: '',
                remoteFsMapping: '',
                instanceCapStr: '2',
                // SSH Launcher
                credentialsId: 'jenkins-docker-server'
            ]
        ]
    ]
]

def dockerClouds = []
docker_settings.each { cloud ->

  def templates = []
  cloud.templates.each { template ->
      def dockerTemplateBase =
          new DockerTemplateBase(
             template.image,
             template.dnsString,
             template.network,
             template.dockerCommand,
             template.volumesString,
             template.volumesFromString,
             template.environmentsString,
             template.lxcConfString,
             template.hostname,
             template.memoryLimit,
             template.memorySwap,
             template.cpuShares,
             template.bindPorts,
             template.bindAllPorts,
             template.privileged,
             template.tty,
             template.macAddress
      )

      def dockerTemplate =
        new DockerTemplate(
          dockerTemplateBase,
          template.labelString,
          template.remoteFs,
          template.remoteFsMapping,
          template.instanceCapStr
        )

      def dockerComputerSSHLauncher = new DockerComputerSSHLauncher(
          new SSHConnector(22, template.credentialsId, null, null, null, null, null )
      )

      dockerTemplate.setLauncher(dockerComputerSSHLauncher)

      dockerTemplate.setMode(Node.Mode.NORMAL)
      dockerTemplate.setNumExecutors(1)
      dockerTemplate.setRemoveVolumes(true)
      dockerTemplate.setRetentionStrategy(new DockerOnceRetentionStrategy(10))
      dockerTemplate.setPullStrategy(DockerImagePullStrategy.PULL_LATEST)

      templates.add(dockerTemplate)
  }

  dockerClouds.add(
    new DockerCloud(cloud.name,
                    templates,
                    cloud.serverUrl,
                    cloud.containerCapStr,
                    cloud.connectTimeout,
                    cloud.readTimeout,
                    cloud.credentialsId,
                    cloud.version
    )
  )
}

Jenkins.instance.clouds.replaceBy(dockerClouds)
println 'Configured docker cloud.'
