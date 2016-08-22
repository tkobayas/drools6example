#!/bin/bash

java -Dcommiter.count=1000 -cp bin:lib/* org.junit.runner.JUnitCore com.sample.ConcurrentJGitUtilTest > output.log 2>&1
