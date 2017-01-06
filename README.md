# Jenkins Cloudformation

Our Jenkins configuration

## Info
-   Builds out Jenkins Master
-   Builds out Docker Host box for running Jenkins Jobs
-   Uses Groovy Scripts to Configure Jenkins, Credentials, and Plugins

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
    -   GHAdminCreds; Enter you Github Username and [Personal Access Token](https://github.com/settings/tokens) in this format `ghuser:token`
        -   See [Restore/Backup Options](#restorebackup-options) Section for more info
    -   AdditionalBucket; Add access to another S3 bucket via IAM Profile
-   Jenkins Configuration
    -   MasterInstanceType; Select Instance Size for Jenkins Master (only has a single executor for small backup  jobs)
    -   JenkinsSubdomain; Choose subdomain to create in
    -   JenkinsVersion; Select which version of Jenkins you want to install (>= 2.x Only Available)
    -   JenkinsLogLevel; Select which log level you want Jenkins to log at
-   Plugins Configuration
    -   GitEmail; [Git Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Git+Plugin): Enter Git Commit email
    -   GithubAdmins; [Github Oauth](https://wiki.jenkins-ci.org/display/JENKINS/GitHub+OAuth+Plugin): Enter a comma delimited list of Github Usernames to act as Jenkins Admins (i.e.; octobob, turkeydaniels, catarang)
    -   GithubOrg; [Github Oauth](https://wiki.jenkins-ci.org/display/JENKINS/GitHub+OAuth+Plugin): Enter Organization to use for Jenkins Authorization
    -   GithubClientID; [Github Oauth](https://wiki.jenkins-ci.org/display/JENKINS/GitHub+OAuth+Plugin): Github Auth Integration ClientID
    -   GithubClientSecret; [Github Oauth](https://wiki.jenkins-ci.org/display/JENKINS/GitHub+OAuth+Plugin): Github Auth Integration Client Secret
    -   SlackTeam; [Slack Notification](https://wiki.jenkins-ci.org/display/JENKINS/Slack+Plugin): Enter Slack Team to Connect To
    -   SlackRoom; [Slack Notification](https://wiki.jenkins-ci.org/display/JENKINS/Slack+Plugin): Enter Default Slack Room to Post to
    -   SlackToken; [Slack Notification](https://wiki.jenkins-ci.org/display/JENKINS/Slack+Plugin): Enter Jenkins-CI Integration Token from Slack
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
