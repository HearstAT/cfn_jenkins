import jenkins.*
import jenkins.model.*

// Variable Set
instance = Jenkins.getInstance()

//Slack Configuration
def slack = instance.getDescriptorByType(jenkins.plugins.slack.SlackNotifier.DescriptorImpl)
slack.teamDomain = "replaceSlackTeam"
slack.tokenCredentialId = "slack-integration-token"
slack.sendAs = "Jenkins"
slack.room = "replaceSlackRoom"
slack.save()
