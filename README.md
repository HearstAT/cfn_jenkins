# Jenkins Cloudformation

Our Jenkins Configuration

## Info
-   Builds out Jenkins Master that's managed via Systemd
-   Builds out Docker Host box for running Jenkins Jobs
-   Uses Groovy Scripts to Configure Jenkins, Credentials, and Plugins; See [Plugins](#plugins) Sections for specifics
    -   Configures Docker Templates for using Docker as Job Servers
    -   Configures Github & Git Plugins w/ Credentials
    -   Configures Slack Notification Plugin
-   Utilizes [Jenkins Job Builder](http://docs.openstack.org/infra/jenkins-job-builder/) for Job Templates and Creates a Seed/Sync Job to Restore/Build Jobs
    -   See [Jenkins Job Builder](#jenkins-job-builder) Section for specifics

## Diagram
![Alt text](Jenkins_Build_Out.png?raw=true "Overview Diagram")

## Requirements
-   Existing VPC
    -   IP Scheme of 172.33.0.0/16 or modify template to support whichever
    -   SSH Security Group (Will lookup existing groups in AWS, make sure one exists)
-   Route53 Hosted Domain/Zone; [Guide](http://docs.aws.amazon.com/Route53/latest/DeveloperGuide/CreatingHostedZone.html)
-   Existing SSL Certificate (See [SSL Setup](#ssl-setup) Section for more info)

## Usage

### Getting Started

#### Params and Options
-   Domain Configuration
    -   HostedZone; A Domain Setup in Route53 [Guide](http://docs.aws.amazon.com/Route53/latest/DeveloperGuide/CreatingHostedZone.html)
    -   SSLCertificateARN; See the [SSL Setup](#ssl-setup) Section  
-   Redeploy/Existing Install Configuration
    -   ExistingBucketName; See [Restore/Backup Options](#restorebackup-options) Section for more info
    -   RestoreBackup; See [Restore/Backup Options](#restorebackup-options) Section for more info
-   Jenkins Configuration
    -   MasterInstanceType; Select Instance Size for Jenkins Master (only has a single executor for small backup  jobs)
    -   JenkinsSubdomain; Choose subdomain to create in
    -   JenkinsVersion; Select which version of Jenkins you want to install (>= 2.x Only Available)
    -   JJBRepo; Enter url for Jenkins Job Builder Templates to Seed/Sync to Jenkins. (See [Jenkins Job Builder](#jenkins-job-builder) Section for more info)
-   Github Configuration (See [Github Section](#github) for more Info)
    -   GithubAdmins; [Github Oauth](https://wiki.jenkins-ci.org/display/JENKINS/GitHub+OAuth+Plugin): Enter a comma delimited list of Github Usernames to act as Jenkins Admins (i.e.; octobob, turkeydaniels, catarang)
    -   GithubCreds; Enter you Github Username and [Personal Access Token](https://github.com/settings/tokens) in this format `ghuser:token`
            -   See [Restore/Backup Options](#restorebackup-options) Section for more info specific information how this affects restoring backups
    -   GithubOrg; [Github Oauth](https://wiki.jenkins-ci.org/display/JENKINS/GitHub+OAuth+Plugin): Enter Organization to use for Jenkins Authorization
    -   GitEmail; [Git Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Git+Plugin): Enter Git Commit email
    -   GithubClientID; [Github Oauth](https://wiki.jenkins-ci.org/display/JENKINS/GitHub+OAuth+Plugin): Github Auth Integration ClientID
    -   GithubClientSecret; [Github Oauth](https://wiki.jenkins-ci.org/display/JENKINS/GitHub+OAuth+Plugin): Github Auth Integration Client Secret
-   Slack Configuration (See [Slack Section](#slack) for more Info)
    -   SlackTeam; [Slack Notification](https://wiki.jenkins-ci.org/display/JENKINS/Slack+Plugin): Enter Slack Team to Connect To
    -   SlackRoom; [Slack Notification](https://wiki.jenkins-ci.org/display/JENKINS/Slack+Plugin): Enter Default Slack Room to Post to
    -   SlackToken; [Slack Notification](https://wiki.jenkins-ci.org/display/JENKINS/Slack+Plugin): Enter Jenkins-CI Integration Token from Slack
-   S3 Configuration
    -   IAMBucketAccess; True/False Option. Select true to give Jenkins Master access to all S3 Buckets (work around until I figure out something better)
-   Mail Configuration
    -   AdminEmail; Email Address to Jenkins email from
    -   MailUser; admin
    -   MailPassword; password
    -   MailHost; Such as mailgun.org
    -   MailPort; Mail server port
-   Jenkins Build Host Configuration
    -   BuildInstanceType; Select instance size for Docker/Build Host
    -   DockerVersion; Select version of Docker to install (>= 11.x)
-   Network Configuration
    -   KeyName; Select SSH Key
    -   VPC; See [Requirements](#requirements) for more info
    -   SSHSecurityGroup; See [Requirements](#requirements) for more info
    -   CIDRA; See [Requirements](#requirements) for more info
-   Sumologic Configuration (Optional)
    -   SumologicAccessID; See [Sumologic](#sumologic) for more info
    -   SumologicAccessKey; See [Sumologic](#sumologic) for more info
-   NewRelic Configuration (Optional)
    -   NewRelicAppName; See [NewRelic](#new-relic) for more info
    -   NewRelicLicense; See [NewRelic](#new-relic) for more info
-   Development Options
    -   GitClone; See [Development/Forked/Custom Selections](#developmentforkedcustom-selections) for more info
    -   GitBranch; See [Development/Forked/Custom Selections](#developmentforkedcustom-selections) for more info

#### Optional Setup

-   Restoring Setups; See the [Restore/Backup](#restorebackup-options) Section
    -   **Important:**  If intending to do external knife ec backups, check this section before setting up.
-   NewRelic Setup; See the [NewRelic](#new-relic) Section
-   Sumologic Setup; See the [Sumologic](#sumologic) Section

##### Development/Forked/Custom Selections
Options that don't need to be bothered with except in development situations.

See [Contributing](#contributing) for how to contribute to this project
-   GitClone; If using a Forked repo or different repo just for scripts
-   GitBranch; sets branch to pull scripts from
    -   Affects which branch the scripts folders in pulled from

### Jenkins Job Builder
This build out comes completely integrated with the [Jenkins Job Builder](http://docs.openstack.org/infra/jenkins-job-builder/) (JJB) from Openstack

-   Creates a JJB Sync Job that will pull down a repo (Set by the JJBRepo Param) and run `jenins-jobs update` against that repo to seed a new instance
    -   It's also setup to auto-update jenkins on any git pushes to the repo post-build

#### Example Job (Using Job Templates)
Create Multiple Jobs that require the same build Process

```yaml
---
- project:
    name: 'Website'
    # Iterates over this data to create identical jobs with website specific info
    websites:
      - Karate_Bob:
          gitrepo: 'https://github.com/mygit/karatebob.git'
          branch: 'master'
          bucket: 'karatebob.com'
          region: 'us-east-1'
          s3-profile: 's3-iam'
      - Awesome_Sauce:
          gitrepo: 'https://github.com/mygit/awesomesauce.git'
          branch: 'master'
          bucket: 'awesomesauce.com'
          region: 'us-east-1'
          s3-profile: 's3-iam'
    jobs:
      - '{name}-{websites}'

# Templates that gets re-used with data from above
- job-template:
    name: '{name}-{websites}'
    description: 'S3 Website Publisher For {websites} - '
    project-type: freestyle
    defaults: global
    display-name: '{name} {websites}'
    block-downstream: false
    block-upstream: false
    node: master
    build-discarder:
      numToKeep: 10
    wrappers:
      - ansicolor:
          colormap: xterm
    triggers:
      - github
    scm:
      - git:
          url: '{gitrepo}'
          credentials-id: 'github-login-creds'
          branches:
            - {branch}
          browser: githubweb
          browser-url: '{gitrepo}'
          timeout: 5
    publishers:
      - s3:
          s3-profile: '{s3-profile}'
          entries:
            - destination-bucket: '{bucket}'
              source-files: '**/*'
              storage-class: STANDARD
              bucket-region: '{region}'
              upload-on-failure: true
              upload-from-slave: false
              managed-artifacts: false
              s3-encryption: false
              flatten: false
```

#### Backup to S3 Job
JJB Template that updates daily to the Jenkins S3 sync bucket if there are any changes

```yaml
---
- job:
    name: 'jenkins-backup'
    display-name: 'Jenkins Backup'
    description: 'Jenkins Backup Job Trigger by Config.xml updates - '
    project-type: freestyle
    defaults: global
    disabled: false
    concurrent: false
    quiet-period: 5
    block-downstream: false
    block-upstream: false
    retry-count: 3
    node: 'master'
    build-discarder:
      numToKeep: 10
    wrappers:
      - ansicolor:
          colormap: xterm
      - inject:
          properties-file: '/var/lib/jenkins/variables'
    triggers:
      - timed: "@midnight"
    builders:
      - shell: |
          for xml in $(ls ${JENKINS_HOME}/*.xml); do
            aws s3 cp $xml s3://${JENKINS_BUCKET}/jenkins
          done

          aws s3 cp ${JENKINS_HOME}/secrets s3://${JENKINS_BUCKET}/jenkins --recursive
```

### Properties File

During Build there is a properties (variables) created that can be used to inject some addition info into builds

-   File: `/var/lib/jenkins/variables`
    -   `JENKINS_BUCKET` (Jenkins Sync Bucket, Existing or Created depend on params)
    -   `STACKNAME` (CFN Stack Name)
    -   `MASTER_INSTANCE_ID` (Instance ID of Master)

### Plugins

#### Github
-   Github Oauth
    -   Changes Jenkins Oauth to a Specific Github Org
        -   All and only the folks in this org can log into Jenkins
    -   Admins are set via GithubAdmins param (must be part of Org)
    -   Requires a Github Client ID and Secret to function
        -   See the [Github](https://wiki.jenkins-ci.org/display/JENKINS/GitHub+OAuth+Plugin) Plugin Wiki for more info
-   Github Server
    -   Sets up a github server with the credentials seeded with the [credentials](scripts/bootstrap/credentials.groovy) script (GithubCreds param)

#### Slack
Setups the Global Slack plugin with the following
-   Team Domain set via SlackTeam param
-   Set Token Credential ID
    -   Utilizes the credentials plugin to store the slack token via [credentials](scripts/bootstrap/credentials.groovy) script
-   Sends as Jenkins
-   Default room set via SlackRoom param

#### Warnings
Custom Warnings parser configured for Chef Foodcritic

See the [Warnings](scripts/xml/hudson.plugins.warnings.WarningsPublisher.xml) file for more info

#### S3 Publisher
There is a profile created to allow S3 access via IAM Profile on the Jenkins Master

See the [S3 Publisher](scripts/xml/hudson.plugins.s3.S3BucketPublisher.xml) file for more info

IAMBucketAccess and ExistingBucketName params affect this setup

### SSL Setup
To simplify the setup only the ELB (Public Side) is setup with SSL.

Learn how to upload certificates to be used with this [Guide](http://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_server-certs.html)

To Get an existing SSL Cert in AWS Follow this [Guide](http://docs.aws.amazon.com/cli/latest/reference/iam/get-server-certificate.html)

or run this command `aws iam get-server-certificate --server-certificate-name`

You'll be looking for something that starts with: `arn:aws:iam::`

You can also use the new [AWS Certificate Manager Service](http://docs.aws.amazon.com/acm/latest/userguide/gs-acm.html) to create and manage certificates

### Restore/Backup Options
-   Backup
    -   Data is synced to a S3 Bucket created at build time
-   Restore
    -   Enter existing bucket name and set restore backup to true, will sync previously backed up data during build
    -   **Important** If having the Github Oauth Previously Configured, you will need a [Personal Access Token](https://github.com/settings/tokens) so the build process can run scripts against Jenkins

## New Relic
We utilize New Relic as our System Monitor, this is setup only if conditions are met

**If New Relic License Key Param is Filled Out**

Installed via [newrelic.sh](scripts/bootstrap/newrelic.sh)

-   What is Enabled:
    -   New Relic [System Monitor](https://docs.newrelic.com/docs/servers/new-relic-servers-linux/getting-started/new-relic-servers-linux)

## Sumologic
We utilize Sumologic as our Log Management and Analytics platform, this is setup only if conditions are met

**If Sumologic Access Key Param is Filled Out**

Installed via [sumologic.sh](scripts/bootstrap/sumologic.sh)

-   What is Enabled:
    -   Sumologic Collector
    -   Collection Sources
        -   Jenkins Master: Configured log collections for Jenkins and System Logs
        -   Jenkins Build Host: Configured log collections for Docker and System Logs

## Docker Images
This setup pulls the following docker images, if you wish to expand on them please look at the base build image for basic settings.

-   Base Build Image
    -   [Dockerhub](https://hub.docker.com/r/hearstat/jenkins-build-base/)
    -   [Github](https://github.com/HearstAT/docker-jenkins-build-base)
-   Jenkins Job Builder Image
    -   [Dockerhub](https://hub.docker.com/r/hearstat/jenkins-build-jjb/)
    -   [Github](https://github.com/HearstAT/docker-jenkins-build-jjb)
-   Foodcritic (Chef Cookbook) Image
    -   [Dockerhub](https://hub.docker.com/r/hearstat/jenkins-build-foodcritic/)
    -   [Github](https://github.com/HearstAT/docker-jenkins-build-foodcritic)

## Contributing
#### External Contributors
-   Fork the repo on GitHub
-   Clone the project to your own machine
-   Commit changes to your own branch
-   Push your work back up to your fork
-   Submit a Pull Request so that we can review your changes

**NOTE:** Be sure to merge the latest from "upstream" before making a pull request!

#### Internal Contributors
-   Clone the project to your own machine
-   Create a new branch from master
-   Commit changes to your own branch
-   Push your work back up to your branch
-   Submit a Pull Request so the changes can be reviewed

**NOTE:** Be sure to merge the latest from "upstream" before making a pull request!

## Credits
A lot of the work/ideas on this came from researching the following projects.
-   [Jenkins-Bootstrap-Jervis](https://github.com/samrocketman/jenkins-bootstrap-jervis)
-   [Jenkins-Groovy-Examples](https://github.com/glenjamin/jenkins-groovy-examples)
-   [Collection of Jenkins Groovy Scripts](https://gist.github.com/dnozay/e7afcf7a7dd8f73a4e05)

## License
Copyright 2016-2017, Hearst Automation Team

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
