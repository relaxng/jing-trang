#!/usr/bin/env python
# -*- coding: utf-8 -*- vim: set fileencoding=utf-8 :

import os
import shutil
import subprocess
import sys
import time

from lxml import etree

ghRelCmd = 'github-release'  # https://github.com/aktau/github-release/
gitCmd = 'git'
gpgCmd = 'gpg'
javaCmd = 'java'
mvnCmd = 'mvn'

snapshotsRepoUrl = 'https://oss.sonatype.org/content/repositories/snapshots/'
stagingRepoUrl = 'https://oss.sonatype.org/service/local/staging/deploy/maven2/'

version = etree.parse('version.xml').xpath('//text()')[0]

buildRoot = '.'
distDir = os.path.join(buildRoot, 'build', 'dist')


def runCmd(cmd):
    print(' '.join(cmd))
    return subprocess.call(cmd)


def removeIfDirExists(dirPath):
    if os.path.exists(dirPath):
        print('Removing %s' % dirPath)
        shutil.rmtree(dirPath)


def ensureDirExists(dirPath):
    if not os.path.exists(dirPath):
        os.makedirs(dirPath)


def findFiles(directory):
    rv = []
    for root, dirs, files in os.walk(directory):
        for filename in files:
            candidate = os.path.join(root, filename)
            if candidate.find('/.git') == -1:
                rv.append(candidate)
    return rv


def clean():
    removeIfDirExists(distDir)


class Release():

    def __init__(self, url):
        self.url = url
        self.setVersion()
        self.releaseDate = time.strftime('%d %B %Y')
        self.buildXml = os.path.join(buildRoot, 'build.xml')
        self.setClasspath()
        self.buildDist()
        self.createOrUpdateGithubData()
        self.uploadToGithub()

    def setClasspath(self):
        self.classpath = os.pathsep.join([
            os.path.join('build', 'jing.jar'),
            os.path.join('lib', 'ant.jar'),
            os.path.join('lib', 'ant-launcher.jar'),
            os.path.join('lib', 'ant-nodeps.jar'),
            os.path.join('lib', 'ant-nodeps.jar'),
            os.path.join('lib', 'ant-trax.jar'),
            os.path.join('lib', 'javacc.jar'),
            os.path.join('lib', 'saxon9.jar'),
            os.path.join('lib', 'maven-ant-tasks-2.1.3.jar'),
        ])

    def reInitDistDir(self):
        removeIfDirExists(distDir)
        ensureDirExists(distDir)

    def setVersion(self):
        self.version = version
        if self.url == snapshotsRepoUrl:
            self.version += '-SNAPSHOT'

    def sign(self):
        for filename in findFiles(distDir):
            runCmd([gpgCmd, '--yes', '-ab', filename])

    def buildDist(self):
        runCmd([javaCmd,
                '-cp', self.classpath, 'org.apache.tools.ant.Main',
                '-f', self.buildXml, 'dist'])

    def createArtifacts(self, artifactId=None):
        self.reInitDistDir()
        runCmd([javaCmd,
                '-cp', self.classpath, 'org.apache.tools.ant.Main',
                '-Dversion=%s' % self.version,
                '-f', 'maven-%s.xml' % artifactId,
                'artifacts'])

    def uploadToCentral(self, artifactId):
        self.createArtifacts(artifactId)
        basename = '%s-%s' % (artifactId, self.version)
        mvnArgs = [
            mvnCmd,
            '-f',
            '%s.pom' % os.path.join(distDir, basename),
            'gpg:sign-and-deploy-file',
            '-Dgpg.executable=%s' % gpgCmd,
            '-DrepositoryId=ossrh',
            '-Durl=%s' % self.url,
            '-DpomFile=%s.pom' % basename,
            '-Dfile=%s.jar' % basename,
            '-Djavadoc=%s-javadoc.jar' % basename,
            '-Dsources=%s-sources.jar' % basename,
        ]
        runCmd(mvnArgs)
        mvnArgs = [
            mvnCmd,
            '-f',
            '%s.pom' % os.path.join(distDir, basename),
            'org.sonatype.plugins:nexus-staging-maven-plugin:rc-list',
            '-DnexusUrl=https://oss.sonatype.org/',
            '-DserverId=ossrh',
        ]
        output = subprocess.check_output(mvnArgs)
        idPrefix = 'orgrelaxng'
        # The rest of this is a hack that parses the output from the command
        # mvn -f <file> org.sonatype.plugins:nexus-staging-maven-plugin:rc-list
        # to get the right stagingRepositoryId so that we can fully automate the
        # release from the command line rather than manually with the Web UI.
        for line in output.decode('utf-8').split('\n'):
            if idPrefix not in line:
                continue
            stagingRepositoryId = '%s-%s' % (idPrefix, line[18:22])
            mvnArgs = [
                mvnCmd,
                '-f',
                '%s.pom' % os.path.join(distDir, basename),
                'org.sonatype.plugins:nexus-staging-maven-plugin:rc-close',
                '-DnexusUrl=https://oss.sonatype.org/',
                '-DserverId=ossrh',
                '-DautoReleaseAfterClose=true',
                '-DstagingRepositoryId=' + stagingRepositoryId
            ]
            runCmd(mvnArgs)
            mvnArgs = [
                mvnCmd,
                '-f',
                '%s.pom' % os.path.join(distDir, basename),
                'org.sonatype.plugins:nexus-staging-maven-plugin:rc-release',
                '-DnexusUrl=https://oss.sonatype.org/',
                '-DserverId=ossrh',
                '-DautoReleaseAfterClose=true',
                '-DstagingRepositoryId=' + stagingRepositoryId
            ]
            runCmd(mvnArgs)

    def createOrUpdateGithubData(self):
        runCmd([gitCmd, 'tag', '-s', '-f',
                '-m', 'V%s' % self.version, 'V%s' % self.version])
        args = [
            '-u',
            'relaxng',
            '-r',
            'jing-trang',
            '-d',
            'Released on %s.' % self.releaseDate,
            '-t',
            'V%s' % self.version,
        ]
        devnull = open(os.devnull, 'wb')
        infoArgs = [ghRelCmd, 'info'] + args
        print(' '.join(infoArgs))
        if subprocess.call(infoArgs, stdout=devnull, stderr=subprocess.STDOUT):
            runCmd([ghRelCmd, 'release', '-p'] + args)
        else:
            runCmd([ghRelCmd, 'delete'] + args)
            runCmd([ghRelCmd, 'release', '-p'] + args)
        devnull.close()
        args.append('-n')
        args.append('V%s' % self.version)
        runCmd([ghRelCmd, 'edit', '-p'] + args)

    def uploadToGithub(self):
        self.sign()
        for filename in findFiles(distDir):
            if "zip" in filename:
                args = [
                    ghRelCmd,
                    'upload',
                    "-u",
                    "relaxng",
                    "-r",
                    "jing-trang",
                    "-t",
                    'V%s' % self.version,
                    "-n",
                    os.path.basename(filename),
                    "-f",
                    filename,
                ]
                runCmd(args)


if len(sys.argv) > 1:
    argv = sys.argv[1:]
    for arg in argv:
        if arg == 'snapshot':
            Release(snapshotsRepoUrl)
        elif arg == 'clean':
            clean()
        else:
            print('Unknown option %s.' % arg)
else:
    Release(stagingRepoUrl)
