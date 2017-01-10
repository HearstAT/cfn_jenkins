import jenkins.*
import jenkins.model.*

// Variable Set
instance = Jenkins.getInstance()
git_desc = instance.getDescriptor("hudson.plugins.git.GitSCM")

//Git Configuration
git_desc.setGlobalConfigName("Jenkins")
git_desc.setGlobalConfigEmail("replaceGitEmail")

git_desc.save()
