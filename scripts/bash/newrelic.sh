#!/bin/bash -xev

################################
# USAGE:
#
# ./newrelic.sh license_key
#
# ./newrelic.sh j3u90djfsjfa8u
################################

##################################################
# Script Argument Set
##################################################

LICENSE_KEY=${1}

#######################################################
# Setup Newrelic Source List & Install System Monitor
#######################################################

echo 'deb http://apt.newrelic.com/debian/ newrelic non-free' >> /etc/apt/sources.list.d/newrelic.list || error_exit "Failed to add New Relic to sources"
wget -O- https://download.newrelic.com/548C16BF.gpg | apt-key add - || error_exit "Failes to add New Relic GPG Source Key"
apt-get update && apt-get install newrelic-sysmond || error_exit "Failed to install new relic system monitor"

nrsysmond-config --set license_key=${LICENSE_KEY} || error_exit "Failed to set New Relic License on System Monitor"

##################################################
# Enable and Start all New Relic Agents
##################################################

systemctl enable newrelic-sysmond || error_exit "Failed to enable New Relic System Monitor"

systemctl start newrelic-sysmond || error_exit "Failed to Restart New Relic System Monitor"
