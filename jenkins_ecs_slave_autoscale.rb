#!/usr/bin/env ruby

require 'bundler/setup'
require 'cloudformation-ruby-dsl/cfntemplate'
require 'cloudformation-ruby-dsl/spotprice'
require 'cloudformation-ruby-dsl/table'

template do

  value :AWSTemplateFormatVersion => '2010-09-09'

  value :Description => 'ECS Autoscale Template - Jenkins Specific (1-Sep-2016)'

  parameter 'ClusterName',
            :Description => 'The ECS Cluster Name',
            :Type => 'String'

  parameter 'SecurityGroup',
            :Description => 'Enter Security Group',
            :Type => 'String'

  parameter 'JNLPPORT',
            :Description => 'Enter Port for JNLP Slaves to connect to',
            :Type => 'String'

  parameter 'VPC',
            :Description => 'The VPC for this cluster',
            :Type => 'String'

  parameter 'ECSSubnetA',
            :Description => 'Enter CIDR Block to Create (e.g.; 0.0.0.0/24)',
            :Type => 'String',
            :Default => ''

  parameter 'ECSSubnetB',
            :Description => 'Enter CIDR Block to Create (e.g.; 0.0.0.0/24)',
            :Type => 'String',
            :Default => ''

  parameter 'KeyName',
            :Description => 'The SSH Key for the cluster nodes',
            :Type => 'String'

  parameter 'AutoMin',
            :Type => 'String',
            :Default => '1',
            :AllowedValues => [ '1', '2', '3', '4', '5', '6', '7', '8', '9' ],
            :Description => 'Select minimum instance number for Auto Scaling'

  parameter 'AutoMax',
            :Type => 'String',
            :Default => '1',
            :AllowedValues => [ '1', '2', '3', '4', '5', '6', '7', '8', '9' ],
            :Description => 'Select maximum instance number for Auto Scaling'

  parameter 'AutoDesired',
            :Type => 'String',
            :Default => '1',
            :AllowedValues => [ '1', '2', '3', '4', '5', '6', '7', '8', '9' ],
            :Description => 'Select desired instance number for Auto Scaling'

  parameter 'InstanceType',
            :Type => 'String',
            :Default => 't2.micro',
            :AllowedValues => [
                't2.micro',
                't2.small',
                't2.medium',
                'm3.medium',
                'm3.large',
                'm3.xlarge',
                'm3.2xlarge',
                'c3.large',
                'c3.xlarge',
                'c3.2xlarge',
                'c3.4xlarge',
                'c3.8xlarge',
                'c4.large',
                'c4.xlarge',
                'c4.2xlarge',
                'c4.4xlarge',
                'c4.8xlarge',
                'g2.2xlarge',
                'r3.large',
                'r3.xlarge',
                'r3.2xlarge',
                'r3.4xlarge',
                'r3.8xlarge',
                'i2.xlarge',
                'i2.2xlarge',
                'i2.4xlarge',
                'i2.8xlarge',
                'd2.xlarge',
                'd2.2xlarge',
                'd2.4xlarge',
                'd2.8xlarge',
                'hi1.4xlarge',
                'hs1.8xlarge',
                'cr1.8xlarge',
                'cc2.8xlarge',
                'cg1.4xlarge',
            ],
            :ConstraintDescription => 'must be a valid EC2 instance type.'

  value :Metadata => {
      :'AWS::CloudFormation::Interface' => {
          :ParameterGroups => [
              {
                  :Label => { :default => 'EC2 Config' },
                  :Parameters => [ 'InstanceType', 'KeyName', 'ClusterName' ],
              },
              {
                  :Label => { :default => 'Autoscale Config' },
                  :Parameters => [ 'AutoMin', 'AutoMax', 'AutoDesired' ],
              },
              {
                  :Label => { :default => 'VPC Config' },
                  :Parameters => [ 'VPC', 'ECSSubnetA', 'ECSSubnetB' ],
              },
              {
                  :Label => { :default => 'Jenkins Config' },
                  :Parameters => [ 'JNLPPORT' ],
              },
          ],
      },
  }

  mapping 'AWSInstanceType2Arch',
          :'t2.micro' => { :Arch => 'HVM64' },
          :'t2.small' => { :Arch => 'HVM64' },
          :'t2.medium' => { :Arch => 'HVM64' },
          :'t2.large' => { :Arch => 'HVM64' },
          :'m1.small' => { :Arch => 'HVM64' },
          :'m1.medium' => { :Arch => 'HVM64' },
          :'m1.large' => { :Arch => 'HVM64' },
          :'m1.xlarge' => { :Arch => 'HVM64' },
          :'m2.xlarge' => { :Arch => 'HVM64' },
          :'m2.2xlarge' => { :Arch => 'HVM64' },
          :'m2.4xlarge' => { :Arch => 'HVM64' },
          :'m3.medium' => { :Arch => 'HVM64' },
          :'m3.large' => { :Arch => 'HVM64' },
          :'m3.xlarge' => { :Arch => 'HVM64' },
          :'m3.2xlarge' => { :Arch => 'HVM64' },
          :'m4.large' => { :Arch => 'HVM64' },
          :'m4.xlarge' => { :Arch => 'HVM64' },
          :'m4.2xlarge' => { :Arch => 'HVM64' },
          :'m4.4xlarge' => { :Arch => 'HVM64' },
          :'m4.10xlarge' => { :Arch => 'HVM64' },
          :'c1.medium' => { :Arch => 'HVM64' },
          :'c1.xlarge' => { :Arch => 'HVM64' },
          :'c3.large' => { :Arch => 'HVM64' },
          :'c3.xlarge' => { :Arch => 'HVM64' },
          :'c3.2xlarge' => { :Arch => 'HVM64' },
          :'c3.4xlarge' => { :Arch => 'HVM64' },
          :'c3.8xlarge' => { :Arch => 'HVM64' },
          :'c4.large' => { :Arch => 'HVM64' },
          :'c4.xlarge' => { :Arch => 'HVM64' },
          :'c4.2xlarge' => { :Arch => 'HVM64' },
          :'c4.4xlarge' => { :Arch => 'HVM64' },
          :'c4.8xlarge' => { :Arch => 'HVM64' },
          :'g2.2xlarge' => { :Arch => 'HVM64' },
          :'g2.8xlarge' => { :Arch => 'HVM64' },
          :'r3.large' => { :Arch => 'HVM64' },
          :'r3.xlarge' => { :Arch => 'HVM64' },
          :'r3.2xlarge' => { :Arch => 'HVM64' },
          :'r3.4xlarge' => { :Arch => 'HVM64' },
          :'r3.8xlarge' => { :Arch => 'HVM64' },
          :'i2.xlarge' => { :Arch => 'HVM64' },
          :'i2.2xlarge' => { :Arch => 'HVM64' },
          :'i2.4xlarge' => { :Arch => 'HVM64' },
          :'i2.8xlarge' => { :Arch => 'HVM64' },
          :'d2.xlarge' => { :Arch => 'HVM64' },
          :'d2.2xlarge' => { :Arch => 'HVM64' },
          :'d2.4xlarge' => { :Arch => 'HVM64' },
          :'d2.8xlarge' => { :Arch => 'HVM64' },
          :'hi1.4xlarge' => { :Arch => 'HVM64' },
          :'hs1.8xlarge' => { :Arch => 'HVM64' },
          :'cr1.8xlarge' => { :Arch => 'HVM64' },
          :'cc2.8xlarge' => { :Arch => 'HVM64' }

  mapping 'AWSRegionArch2AMI',
          :'us-east-1' => { :HVM64 => 'ami-dc6aa9b1' },
          :'us-west-2' => { :HVM64 => 'ami-fa76b29a' },
          :'us-west-1' => { :HVM64 => 'ami-524b0f32' },
          :'eu-west-1' => { :HVM64 => 'ami-e9d9419a' },
          :'eu-central-1' => { :HVM64 => 'ami-e626ce89' },
          :'ap-northeast-1' => { :HVM64 => 'ami-dd9e4cbe' },
          :'ap-northeast-2' => { :HVM64 => 'ami-d4408bba' },
          :'ap-southeast-1' => { :HVM64 => 'ami-ad41b5cc' },
          :'ap-southeast-2' => { :HVM64 => 'ami-79361e1a' },
          :'sa-east-1' => { :HVM64 => 'ami-add144c1' }

  resource 'ECSAutoScalingGroup', :Type => 'AWS::AutoScaling::AutoScalingGroup', :Properties => {
      :AvailabilityZones => get_azs,
      :VPCZoneIdentifier => [
          ref('ECSSubnetA'),
          ref('ECSSubnetB'),
      ],
      :LaunchConfigurationName => ref('ECSLaunchConfiguration'),
      :Cooldown => '30',
      :MinSize => ref('AutoMin'),
      :MaxSize => ref('AutoMax'),
      :DesiredCapacity => ref('AutoDesired'),
      :Tags => [
          {
              :PropagateAtLaunch => 'true',
              :Key => 'Name',
              :Value => join('-', ref('ClusterName'), 'Node'),
          },
          {
              :PropagateAtLaunch => 'true',
              :Key => 'ECS_Cluster',
              :Value => ref('ClusterName'),
          },
      ],
  }

  resource 'ECSLaunchConfiguration', :Type => 'AWS::AutoScaling::LaunchConfiguration', :Properties => {
      :KeyName => ref('KeyName'),
      :IamInstanceProfile => ref('IAMInstanceProfile'),
      :ImageId => find_in_map('AWSRegionArch2AMI', aws_region, find_in_map('AWSInstanceType2Arch', ref('InstanceType'), 'Arch')),
      :AssociatePublicIpAddress => 'false',
      :InstanceType => ref('InstanceType'),
      :SecurityGroups => [ ref('SecurityGroup') ],
      :EbsOptimized => 'false',
      :InstanceMonitoring => 'true',
      :UserData => base64(
          join('',
               "#!/bin/bash -v\n",
               "yum update -y\n",
               "mkdir -p /etc/ecs\n",
               'echo \'ECS_CLUSTER=',
               ref('ClusterName'),
               "' > /etc/ecs/ecs.config\n",
          )
      ),
  }

  resource 'IAMRole', :Type => 'AWS::IAM::Role', :Properties => {
      :AssumeRolePolicyDocument => {
          :Statement => [
              {
                  :Effect => 'Allow',
                  :Principal => { :Service => [ 'ec2.amazonaws.com' ] },
                  :Action => [ 'sts:AssumeRole' ],
              },
          ],
      },
      :Path => '/',
      :Policies => [
          {
              :PolicyName => join('-', aws_stack_name, 'ecs', 'nodes', 'role'),
              :PolicyDocument => {
                  :Statement => [
                      {
                          :Effect => 'Allow',
                          :Action => [
                              'ecs:CreateCluster',
                              'ecs:DeregisterContainerInstance',
                              'ecs:DiscoverPollEndpoint',
                              'ecs:Poll',
                              'ecs:RegisterContainerInstance',
                              'ecs:Submit*',
                              'elasticloadbalancing:Describe*',
                              'elasticloadbalancing:DeregisterInstancesFromLoadBalancer',
                              'elasticloadbalancing:RegisterInstancesWithLoadBalancer',
                              'ec2:Describe*',
                              'ec2:AuthorizeSecurityGroupIngress',
                          ],
                          :Resource => [ '*' ],
                      },
                  ],
              },
          },
      ],
  }

  resource 'IAMInstanceProfile', :Type => 'AWS::IAM::InstanceProfile', :Properties => {
      :Path => '/',
      :Roles => [ ref('IAMRole') ],
  }

end.exec!
