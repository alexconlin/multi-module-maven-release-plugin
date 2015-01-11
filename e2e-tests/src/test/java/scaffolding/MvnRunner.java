package scaffolding;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MvnRunner {
    public static void installReleasePluginToLocalRepo() throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setGoals(Collections.singletonList("install"));
        Properties props = new Properties();
        props.setProperty("skipTests", "true");
        request.setProperties(props);
        Invoker invoker = new DefaultInvoker();
        InvocationResult result = invoker.execute(request);
        assertThat("Exit code from running mvn install on this project", result.getExitCode(), is(0));
    }

    public static List<String> runReleaseOn(File projectDir, String releaseVersion) throws IOException, InterruptedException {
        String mvnPath = System.getenv("M2_HOME");
        if (StringUtils.isBlank(mvnPath)) {
            throw new RuntimeException("M2_HOME is not set");
        }
        File m2Bin = new File(mvnPath, "bin");
        File m2 = new File(m2Bin, SystemUtils.IS_OS_WINDOWS ? "mvn.bat" : "mvn");
        if (!m2.isFile()) {
            throw new RuntimeException("Could not locate the mvn executable. Looked at " + m2.getCanonicalPath());
        }

        CommandLine command = new CommandLine(m2.getCanonicalPath());
        command.addArgument("com.github.danielflower.mavenplugins:multi-module-release-plugin:1.0-SNAPSHOT:release");
        command.addArgument("-DreleaseVersion=" + releaseVersion);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(projectDir);

        CollectingLogOutputStream logCollector = new CollectingLogOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(logCollector);
        executor.setStreamHandler(streamHandler);

        ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
        executor.setWatchdog(watchdog);
        int exitCode = executor.execute(command);
        if (exitCode != 0) {
            throw new RuntimeException("Exit code is " + 0);
        }

        return logCollector.getLines();

    }
}