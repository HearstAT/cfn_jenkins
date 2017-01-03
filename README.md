# Jenkins Cloudformation

Our Jenkins configuration

## Info
-   Builds out Jenkins Master
-   Builds out Docker Host box for running Jenkins Jobs
-   Uses Groovy Scripts to Configure Jenkins, Credentials, and Plugins

## Diagram
-   [ ] Create diagram for build out

## Requirements
-   Existing VPC
    -   IP Scheme of 172.33.0.0/16 or modify template to support whichever
    -   SSH Security Group (Will lookup existing groups in AWS, make sure one exists)
-   Route53 Hosted Domain/Zone; [Guide](http://docs.aws.amazon.com/Route53/latest/DeveloperGuide/CreatingHostedZone.html)
-   Existing SSL Certificate (See [SSL Setup](#ssl-setup) Section for more info)

## Usage

### Getting Started

#### Params and Options
-   [ ] Add params and where to find info

#### Optional Setup

-   Restoring Setups; See the [Restore/Backup](#restorebackup-options) Section
    -   **Important:**  If intending to do external knife ec backups, check this section before setting up.
-   NewRelic Setup; See the [NewRelic](#new-relic) Section
-   Sumologic Setup; See the [Sumologic](#sumologic) Section

##### Mail Server Setup
**WIP** Placeholder only

-   MailUsername; admin
-   MailPassword; password
-   MailHost; Such as mailgun.org
-   MailPort; Mail server port

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
    -   Utilize an existing bucket to sync down any Jenkins Configuration and/or Secrets
    -   Create a new Bucket and add a `jenkins` and `secrets` folder

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
