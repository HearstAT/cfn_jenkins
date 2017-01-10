#!/bin/bash -xev

##########################################################
# Upgrade OS & Install Dependencies
##########################################################

apt-get update && apt-get -y upgrade
apt-get install -y wget curl python-setuptools python-pip git apt-transport-https

##########################################################
# Global Variable Set
##########################################################

export DEBIAN_FRONTEND=noninteractive
export INSTANCE_ID=$(curl -s http://169.254.169.254/latest/meta-data/instance-id)
export STACKNAME='${AWS::StackName}'
export HOSTNAME="${JenkinsSubdomain}.${HostedZone}"
export GITCLONE='${GitClone}'
export GITREPO="${!GITCLONE##*/}"
export JJBREPO='${JJBRepo}'
export JJBSEDSAFE=$(echo ${!JJBREPO} | sed 's/\//\\\//g')
export JJBTOKEN=$(openssl rand -hex 16)
export GHCREDS='${GithubCreds}'

##########################################################
# Install cfn bootstraping tools
##########################################################

if [ -z $(command -v cfn-signal) ]; then
    easy_install https://s3.amazonaws.com/cloudformation-examples/aws-cfn-bootstrap-latest.tar.gz
fi

if [ -z $(command -v aws) ]; then
  sleep 5
  pip install awscli
fi

##########################################################
# Helper function to set wait timer
##########################################################

error_exit()
{
  cfn-signal -e 1 -r "$1" "${MasterWaitHandle}"
  exit 1
}

export -f error_exit

##########################################################
# Set Hostname and Hosts File
##########################################################

hostname ${!HOSTNAME} || error_exit 'Failed to set hostname'
echo "${!HOSTNAME}" > /etc/hostname || error_exit 'Failed to set hostname file'

cat > '/etc/hosts' << EOF
127.0.0.1 ${!HOSTNAME} ${!HOSTNAME%%.*} jenkins.${!HOSTNAME%%.*} localhost
::1 localhost6.localdomain6 localhost6
EOF

##########################################################
# Jenkins User Add
##########################################################

if [ -z $(getent passwd jenkins) ]; then
  useradd -d "/var/lib/jenkins" -m -s /bin/bash jenkins || error_exit "Failed to create Jenkins User"
fi

##########################################################
# Add Jenkins Repo & Install
##########################################################

## Dependencies
apt-get install -y ca-certificates-java daemon dbus default-jre-headless \
fontconfig-config fonts-dejavu-core java-common libavahi-client3 libxi6 \
libavahi-common-data libavahi-common3 libcap-ng0 libcups2 libdbus-1-3 libexpat1 \
libfontconfig1 libfreetype6 libjpeg-turbo8 libjpeg8 liblcms2-2 libnspr4 libnss3 \
libnss3-nssdb libpcsclite1 libpng12-0 libx11-6 libx11-data libxau6 libxcb1 libxdmcp6 libxext6 \
libxrender1 libxtst6 net-tools openjdk-8-jre-headless psmisc ucf x11-common || error_exit "Jenkins Install: Failed to install dependencies"

## User war download to control version
mkdir -p /var/run/jenkins/ /var/lib/jenkins /usr/share/jenkins /var/log/jenkins || error_exit "Jenkins Install: Failed to create Jenkins directories"
wget "http://mirrors.jenkins.io/war-stable/${JenkinsVersion}/jenkins.war" -qO /usr/share/jenkins/jenkins.war || error_exit "Jenkins Install: Failed to download Jenkins war"
chmod -R 755 /var/run/jenkins/ /var/lib/jenkins /usr/share/jenkins /var/log/jenkins || error_exit "Jenkins Install: Failed to change Jenkins directory mode"
chown -R jenkins:jenkins /var/run/jenkins/ /var/lib/jenkins /usr/share/jenkins /var/log/jenkins || error_exit "Jenkins Install: Failed to change Jenkins directory owner"

##########################################################
# Jenkins Log & Service Configuration
##########################################################

## Systemd Service File
cat > '/lib/systemd/system/jenkins.service' << 'EOF'
[Unit]
Description=jenkins
After=network.target

[Service]
Type=simple
User=jenkins
Group=jenkins
Environment=JENKINS_HOME=/var/lib/jenkins
ExecStartPre=/bin/rm -f /var/run/jenkins/jenkins.pid
ExecStart=/usr/bin/java -Djava.awt.headless=true -jar /usr/share/jenkins/jenkins.war --httpPort=8080 --httpListenAddress=0.0.0.0
ExecReload=/bin/kill -s HUP $MAINPID
TimeoutStartSec=0
Restart=always
RestartSec=20
Delegate=yes
KillMode=process

[Install]
WantedBy=multi-user.target
EOF

systemctl enable jenkins || error_exit "Jenkins Install: Failed to enable Jenkins"

##########################################################
# Clone Script Repo
##########################################################

if [ ! -d "/root/${!GITREPO%.*}" ]; then
  cd /root && git clone ${!GITCLONE} || error_exit "Get Groovy Scripts: Failed to Clone Scripts from Repo"
else
  cd /root/${!GITREPO%.*} && git pull || error_exit "Get Groovy Scripts: Failed pull updates"
fi

if [ '${GitBranch}' != 'master' ]; then
  cd /root/${!GITREPO%.*} && git checkout ${GitBranch} || error_exit "Get Groovy Scripts: Failed to switch to ${GitBranch} branch"
fi

##########################################################
# Jenkins Sync & Bootstrap
##########################################################

## Swap Placeholders with Params
### Github Oauth Config Script
sed -i 's/replaceClientID/${GithubClientID}/g' /root/${!GITREPO%.*}/scripts/groovy/github_auth.groovy || error_exit "Groovy Bootstrap: Failed to replace Github Oath Params"
sed -i 's/replaceClientSecret/${GithubClientSecret}/g' /root/${!GITREPO%.*}/scripts/groovy/github_auth.groovy || error_exit "Groovy Bootstrap: Failed to replace Github Oath Params"
sed -i 's/replaceGithubOrg/${GithubOrg}/g' /root/${!GITREPO%.*}/scripts/groovy/github_auth.groovy || error_exit "Groovy Bootstrap: Failed to replace Github Oath Params"
sed -i 's/replaceGithubAdmins/${GithubAdmins}/g' /root/${!GITREPO%.*}/scripts/groovy/github_auth.groovy || error_exit "Groovy Bootstrap: Failed to replace Github Oath Params"
### Github Server Config (Pending)
### Credentials Config Script
sed -i 's/replaceSlackToken/${SlackToken}/g' /root/${!GITREPO%.*}/scripts/groovy/credentials.groovy || error_exit "Groovy Bootstrap: Failed to replace Slack Credential Params"
sed -i "s/replaceGitLogin/${!GHCREDS%:*}/g" /root/${!GITREPO%.*}/scripts/groovy/credentials.groovy || error_exit "Groovy Bootstrap: Failed to replace Github Credential Params"
sed -i "s/replaceGitToken/${!GHCREDS##*:}/g" /root/${!GITREPO%.*}/scripts/groovy/credentials.groovy || error_exit "Groovy Bootstrap: Failed to replace Github Credential Params"
### Git Config Script
sed -i 's/replaceGitEmail/${GitEmail}/g' /root/${!GITREPO%.*}/scripts/groovy/git.groovy || error_exit "Groovy Bootstrap: Failed to replace Git Params"
### Slack Config Script
sed -i 's/replaceSlackTeam/${SlackTeam}/g' /root/${!GITREPO%.*}/scripts/groovy/slack.groovy || error_exit "Groovy Bootstrap: Failed to replace Slack Params"
sed -i 's/replaceSlackRoom/${SlackRoom}/g' /root/${!GITREPO%.*}/scripts/groovy/slack.groovy || error_exit "Groovy Bootstrap: Failed to replace Slack Params"
### Docker Config Script
sed -i 's/replaceDockerIP/${DOCKERIP}/g' /root/${!GITREPO%.*}/scripts/groovy/docker.groovy || error_exit "Groovy Bootstrap: Failed to replace Docker Params"
### Mail Config
sed -i 's/replaceAdminEmail/${AdminEmail}/g' /root/${!GITREPO%.*}/scripts/groovy/mail.groovy || error_exit "Groovy Bootstrap: Failed to replace Mail Params"
sed -i 's/replaceMailUser/${MailUser}/g' /root/${!GITREPO%.*}/scripts/groovy/mail.groovy || error_exit "Groovy Bootstrap: Failed to replace Mail Params"
sed -i 's/replaceMailPassword/${MailPassword}/g' /root/${!GITREPO%.*}/scripts/groovy/mail.groovy || error_exit "Groovy Bootstrap: Failed to replace Mail Params"
sed -i 's/replaceMailHost/${MailHost}/g' /root/${!GITREPO%.*}/scripts/groovy/mail.groovy || error_exit "Groovy Bootstrap: Failed to replace Mail Params"
sed -i 's/replaceMailPort/${MailPort}/g' /root/${!GITREPO%.*}/scripts/groovy/mail.groovy || error_exit "Groovy Bootstrap: Failed to replace Mail Params"
### JJB Job Config
sed -i "s/replaceJJBJobs/${!JJBSEDSAFE}/g" /root/${!GITREPO%.*}/scripts/jobs/jjb-sync/config.xml || error_exit "Groovy Bootstrap: Failed to replace JJB Job Repo URL"
sed -i "s/replaceToken/${!JJBTOKEN}/g" /root/${!GITREPO%.*}/scripts/jobs/jjb-sync/config.xml || error_exit "Groovy Bootstrap: Failed to replace JJB Job Repo External Token"

systemctl start jenkins || error_exit "Jenkins Bootstrap: Failed to start Jenkins"

until $(curl --output /dev/null --silent --head --fail 127.0.0.1:8080/login); do
  printf '.'
  sleep 5
done

if [ ! -f /var/lib/jenkins/github_bootstrap ]; then
  ADMINCREDS="admin:$(cat /var/lib/jenkins/secrets/initialAdminPassword)"
else
  ADMINCREDS='${GithubCreds}'
fi

crumb=`curl --user "${!ADMINCREDS}" "127.0.0.1:8080/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,%22:%22,//crumb)"`
curl -H $crumb --user "${!ADMINCREDS}" --data-urlencode "script=$(</root/${!GITREPO%.*}/scripts/groovy/plugins.groovy)" 127.0.0.1:8080/scriptText || error_exit "Jenkins Bootstrap: Failed to run Groovy Plugins Install"
## Pull In Backup Data
if [ -n ${ExistingBucketName} ] && [ ${RestoreBackup} == 'true' ]; then
  aws s3 sync s3://${ExistingBucketName}/jenkins /var/lib/jenkins || error_exit "Jenkins Sync: Failed to sync existing data"
  chmod -R 755 /var/run/jenkins/ /var/lib/jenkins /usr/share/enkins /var/log/jenkins || error_exit "Jenkins Install: Failed to change Jenkins directory mode"
  chown -R jenkins:jenkins /var/run/jenkins/ /var/lib/jenkins /usr/share/jenkins /var/log/jenkins || error_exit "Jenkins Install: Failed to change Jenkins directory owner"
  if [ -f /var/lib/jenkins/github_bootstrap ]; then
    ADMINCREDS='${GithubCreds}'
  fi

systemctl restart jenkins || error_exit "Jenkins Bootstrap: Failed to restart Jenkins (Existing Restore)"

fi

sleep 5

until $(curl --output /dev/null --silent --head --fail 127.0.0.1:8080/login); do
  printf '.'
  sleep 5
done

if [ ! -f /var/lib/jenkins/github_bootstrap ]; then
  crumb=`curl --user "${!ADMINCREDS}" "127.0.0.1:8080/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,%22:%22,//crumb)"`
  curl -H $crumb --user "${!ADMINCREDS}" --data-urlencode "script=$(</root/${!GITREPO%.*}/scripts/groovy/github_auth.groovy)" 127.0.0.1:8080/scriptText || error_exit "Jenkins Bootstrap: Failed to run Groovy Github Oauth Config"
  touch /var/lib/jenkins/github_bootstrap
  systemctl restart jenkins || error_exit "Jenkins Bootstrap: Failed to restart Jenkins (Github Auth)"
  ADMINCREDS='${GithubCreds}'
fi

crumb=`curl --user "${!ADMINCREDS}" "127.0.0.1:8080/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,%22:%22,//crumb)"`

for script in $(ls /root/${!GITREPO%.*}/scripts/groovy/); do
  if [ "${!script}" != 'github_auth.groovy' ] || [ "${!script}" != 'plugins.groovy' ]; then
    curl -H $crumb --user "${!ADMINCREDS}" --data-urlencode "script=$(</root/${!GITREPO%.*}/scripts/groovy/${!script})" 127.0.0.1:8080/scriptText || error_exit "Jenkins Bootstrap: Failed to run Groovy Script; $!{script}"
  fi
done

systemctl stop jenkins || error_exit "Failed to stop Jenkins"

cat > '/var/lib/jenkins/jenkins.install.InstallUtil.lastExecVersion' << EOF
${JenkinsVersion}
EOF

cat > '/var/lib/jenkins/jenkins.install.UpgradeWizard.state' << EOF
${JenkinsVersion}
EOF

aws s3 sync /var/lib/jenkins/ s3://${BUCKET}/jenkins \
    --exclude "*" \
    --exclude "*/*" \
    --include "hudson.*.xml" \
    --include "jenkins*.xml" \
    --include "config.xml" \
    --include "credentials.xml" \
    --include "*.key" \
    --include "*secrets/*"

mkdir -p /var/lib/jenkins/data || error_exit "Jenkins Bootstrap: Failed to create Jenkins data directory"

cat > '/var/lib/jenkins/data/log.properties' << EOF
handlers=java.util.logging.ConsoleHandler
jenkins.level=${JenkinsLogLevel}
java.util.logging.ConsoleHandler.level=${JenkinsLogLevel}
EOF

cp -f /root/${!GITREPO%.*}/scripts/xml/* /var/lib/jenkins/ || error_exit "Jenkins Bootstrap: Failed to Copy XML files in place"
cp -rf /root/${!GITREPO%.*}/scripts/jobs /var/lib/jenkins/ || error_exit "Jenkins Bootstrap: Failed to Copy JJB Seed/Sync job"

chmod -R 755 /var/lib/jenkins || error_exit "Jenkins Bootstrap: Failed to change Jenkins directory mode"
chown -R jenkins:jenkins /var/lib/jenkins || error_exit "Jenkins Bootstrap: Failed to change Jenkins directory owner"

systemctl start jenkins || error_exit "Jenkins Bootstrap: Failed to start Jenkins"

##########################################################
# Run JJB Seed
##########################################################

until $(curl --output /dev/null --silent --head --fail 127.0.0.1:8080/login); do
  printf '.'
  sleep 5
done

curl -X POST https://${JenkinsSubdomain}.${HostedZone}/job/jjb-sync/build?token=${!JJBTOKEN}

##########################################################
# Clone Script Repo
##########################################################

if [ ! -d "/root/${!GITREPO%.*}" ]; then
  cd /root && git clone ${!GITCLONE} || error_exit "Get Groovy Scripts: Failed to Clone Scripts from Repo"
else
  cd /root/${!GITREPO%.*} && git pull || error_exit "Get Groovy Scripts: Failed pull updates"
fi

if [ '${GitBranch}' != 'master' ]; then
  cd /root/${!GITREPO%.*} && git checkout ${GitBranch} || error_exit "Get Groovy Scripts: Failed to switch to ${GitBranch} branch"
fi

##########################################################
# NewRelic Config if Enabled
##########################################################

if [ -n "${NewRelicLicense}" ]; then
  bash /root/${!GITREPO%.*}/scripts/bash/newrelic.sh ${NewRelicLicense} ${NewRelicAppName}
fi

##########################################################
# Sumologic Config if Enabled
##########################################################

if [ -n "${SumologicAccessID}" ]; then
  bash /root/${!GITREPO%.*}/scripts/bash/sumologic.sh master ${SumologicAccessID} ${SumologicAccessKey}
fi

##########################################################
# Send Success Signal to CFN Wait Handle
##########################################################

/usr/local/bin/cfn-signal -e 0 -r 'Server setup complete' "${MasterWaitHandle}"

##########################################################
# Reboot After Success for all updates made
##########################################################

apt-get update && apt-get -y upgrade
reboot
