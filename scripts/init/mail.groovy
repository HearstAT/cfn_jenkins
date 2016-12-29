import jenkins.model.*

def instance = Jenkins.getInstance()
def jenkinsLocationConfiguration = JenkinsLocationConfiguration.get()
def mail_desc = instance.getDescriptor("hudson.tasks.Mailer")

//Admin Email Configuration
jenkinsLocationConfiguration.setAdminAddress("Admin <${AdminEmail}>")
jenkinsLocationConfiguration.save()

//Mail Configuration
mail_desc.setSmtpAuth("${MailUser}", "${MailPassword}")
mail_desc.setReplyToAddress("${AdminEmail}")
mail_desc.setSmtpHost("${MailHost}")
mail_desc.setUseSsl(true)
mail_desc.setSmtpPort("${MailPort}")
mail_desc.setCharset("[character set]")
