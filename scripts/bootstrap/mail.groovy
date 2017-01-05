import jenkins.model.*

def instance = Jenkins.getInstance()
def jenkinsLocationConfiguration = JenkinsLocationConfiguration.get()
def mail_desc = instance.getDescriptor("hudson.tasks.Mailer")

//Admin Email Configuration
jenkinsLocationConfiguration.setAdminAddress("Admin <replaceAdminEmail>")
jenkinsLocationConfiguration.save()

//Mail Configuration
mail_desc.setSmtpAuth("replaceMailUser", "replaceMailPassword")
mail_desc.setReplyToAddress("replaceAdminEmail")
mail_desc.setSmtpHost("replaceMailHost")
mail_desc.setUseSsl(true)
mail_desc.setSmtpPort("replaceMailPort")
