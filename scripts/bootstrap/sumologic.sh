#!/bin/bash -xev

################################
# USAGE:
#
# ./sumologic.sh system access_id access_key
#
# ./sumologic.sh proxy af893rjnfiuadf fc90u3eiorjqjfnasu8v89
################################

##################################################
# Script Argument Set
##################################################

SYSTEM=${1}
ACCESS_ID=${2}
ACCESS_KEY=${3}

##################################################
# Get and Install Collector
##################################################

wget -q -O /tmp/collector.deb https://collectors.sumologic.com/rest/download/deb/64 || error_exit "Failed to get Sumologic Collector"
dpkg -i /tmp/collector.deb || error_exit "Failed to Install Sumologic Collector"
rm -f /tmp/collector.deb || error_exit "Failed to remove Sumologic Collector"

##################################################
# Set User Properties
##################################################

mkdir -p /opt/SumoCollector/config

cat > '/opt/SumoCollector/config/user.properties' << EOF
name=${HOSTNAME}
accessid=${ACCESS_ID}
accesskey=${ACCESS_KEY}
sources=/opt/SumoCollector/sources.json
ephemeral=true
EOF

##################################################
# Set Collector Sources
##################################################
if [ ${SYSTEM} == 'master' ]; then
cat > '/opt/SumoCollector/sources.json' << EOF
{
  "api.version": "v1",
  "sources": [
    {
      "name": "Messages",
      "sourceType": "LocalFile",
      "automaticDateParsing": true,
      "multilineProcessingEnabled": false,
      "useAutolineMatching": true,
      "forceTimeZone": false,
      "timeZone": "UTC",
      "category": "OS/Linux/System",
      "pathExpression": "/var/log/messages"
    },
    {
      "name": "Mail",
      "sourceType": "LocalFile",
      "automaticDateParsing": true,
      "multilineProcessingEnabled": false,
      "useAutolineMatching": true,
      "forceTimeZone": false,
      "timeZone": "UTC",
      "category": "OS/Linux/System",
      "pathExpression": "/var/log/mail.log"
    },
    {
      "name": "Secure",
      "sourceType": "LocalFile",
      "automaticDateParsing": true,
      "multilineProcessingEnabled": false,
      "useAutolineMatching": true,
      "forceTimeZone": false,
      "timeZone": "UTC",
      "category": "OS/Linux/Security",
      "pathExpression": "/var/log/secure"
    },
    {
      "name": "Syslog File",
      "sourceType": "LocalFile",
      "automaticDateParsing": true,
      "multilineProcessingEnabled": false,
      "useAutolineMatching": true,
      "forceTimeZone": false,
      "timeZone": "UTC",
      "category": "OS/Linux/System",
      "pathExpression": "/var/log/syslog"
    },
    {
      "name": "Jenkins Log",
      "sourceType": "LocalFile",
      "automaticDateParsing": true,
      "multilineProcessingEnabled": false,
      "useAutolineMatching": true,
      "forceTimeZone": false,
      "timeZone": "UTC",
      "category": "Jenkins/Master",
      "pathExpression": "/var/log/jenkins/*.log"
    }
  ]
}
EOF
else
cat > '/opt/SumoCollector/sources.json' << EOF
{
  "api.version": "v1",
  "sources": [
    {
      "name": "Messages",
      "sourceType": "LocalFile",
      "automaticDateParsing": true,
      "multilineProcessingEnabled": false,
      "useAutolineMatching": true,
      "forceTimeZone": false,
      "timeZone": "UTC",
      "category": "OS/Linux/System",
      "pathExpression": "/var/log/messages"
    },
    {
      "name": "Mail",
      "sourceType": "LocalFile",
      "automaticDateParsing": true,
      "multilineProcessingEnabled": false,
      "useAutolineMatching": true,
      "forceTimeZone": false,
      "timeZone": "UTC",
      "category": "OS/Linux/System",
      "pathExpression": "/var/log/mail.log"
    },
    {
      "name": "Secure",
      "sourceType": "LocalFile",
      "automaticDateParsing": true,
      "multilineProcessingEnabled": false,
      "useAutolineMatching": true,
      "forceTimeZone": false,
      "timeZone": "UTC",
      "category": "OS/Linux/Security",
      "pathExpression": "/var/log/secure"
    },
    {
      "name": "Syslog File",
      "sourceType": "LocalFile",
      "automaticDateParsing": true,
      "multilineProcessingEnabled": false,
      "useAutolineMatching": true,
      "forceTimeZone": false,
      "timeZone": "UTC",
      "category": "OS/Linux/System",
      "pathExpression": "/var/log/syslog"
    },
    {
      "name": "Docker Logs",
      "sourceType": "LocalFile",
      "automaticDateParsing": true,
      "multilineProcessingEnabled": false,
      "useAutolineMatching": true,
      "forceTimeZone": false,
      "timeZone": "UTC",
      "category": "Jenkins/Build",
      "pathExpression": "/var/lib/docker/containers/*/*.log"
    }
  ]
}
EOF
fi

##################################################
# Reload and Start Collector
##################################################

systemctl --system daemon-reload && systemctl restart collector.service || error_exit "Failed to start Sumo Collector Service"
