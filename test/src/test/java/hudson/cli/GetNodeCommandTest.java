/*
 * The MIT License
 *
 * Copyright 2013 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package hudson.cli;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import hudson.security.Permission;
import jenkins.model.Jenkins;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class GetNodeCommandTest {

    private CLICommandInvoker command;

    @Rule public final JenkinsRule j = new JenkinsRule();

    @Before public void setUp() {

        command = new CLICommandInvoker(j, new GetNodeCommand());
    }

    @Test public void getNodeShouldFailWithoutAdministerPermision() throws Exception {

        j.createSlave("MySlave", null, null);

        final CLICommandInvoker.Result result = command
                .authorizedTo(Permission.READ)
                .invokeWithArgs("MySlave")
        ;

        assertThat(result.stderr(), containsString("user is missing the Overall/Administer permission"));
        assertThat("No output expected", result.stdout(), isEmptyString());
        assertThat("Command is expected to fail", result.returnCode(), equalTo(-1));
    }

    @Test public void getNodeShouldYieldConfigXml() throws Exception {

        j.createSlave("MySlave", null, null);

        final CLICommandInvoker.Result result = command
                .authorizedTo(Jenkins.ADMINISTER)
                .invokeWithArgs("MySlave")
        ;

        assertThat(result.stdout(), startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertThat(result.stdout(), containsString("<name>MySlave</name>"));
        assertThat("No error output expected", result.stderr(), isEmptyString());
        assertThat("Command is expected to succeed", result.returnCode(), equalTo(0));
    }

    @Test public void getNodeShouldFailIfNodeDoesNotExist() throws Exception {

        final CLICommandInvoker.Result result = command
                .authorizedTo(Jenkins.ADMINISTER)
                .invokeWithArgs("MySlave")
        ;

        assertThat(result.stderr(), containsString("No such node 'MySlave'"));
        assertThat("No output expected", result.stdout(), isEmptyString());
        assertThat("Command is expected to fail", result.returnCode(), equalTo(-1));
    }
}
